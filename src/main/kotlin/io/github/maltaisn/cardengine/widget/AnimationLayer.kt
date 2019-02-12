/*
 * Copyright 2019 Nicolas Maltais
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.maltaisn.cardengine.widget

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.*
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup
import com.oracle.util.Checksums.update
import io.github.maltaisn.cardengine.applyBounded
import io.github.maltaisn.cardengine.withinBounds
import ktx.math.div
import ktx.math.minus
import ktx.math.times


class AnimationLayer : WidgetGroup() {

    /** The list of all card containers on the stage.
     * It's update by the container themselves when they are added to the stage. */
    internal val containers = ArrayList<CardContainer>()

    private val delayedCardMoves = ArrayList<DelayedCardMove>()

    private var draggedCards: Array<CardActor>? = null

    private var cardsMoved = false
    private var animationPending = false
    private var animationTimeLeft = 0f

    var animationRunning = false
        private set

    init {
        touchable = Touchable.childrenOnly

        addListener(object : InputListener() {
            override fun keyUp(event: InputEvent, keycode: Int): Boolean {
                if (keycode == Input.Keys.ESCAPE) {
                    // Stop all animations with escape.
                    completeAnimation()
                    return true
                }
                return false
            }
        })
    }

    override fun act(delta: Float) {
        super.act(delta)

        // Do delayed card moves.
        for (i in delayedCardMoves.indices.reversed()) {
            val move = delayedCardMoves[i]
            move.timeLeft -= delta
            if (move.timeLeft < 0) {
                delayedCardMoves.removeAt(i)
                moveCard(move.src, move.dst, move.srcIndex, move.dstIndex)
                move.callback?.invoke()
            }
        }

        // Start or complete animation.
        if (animationPending) {
            startAnimation()
        } else if (animationRunning) {
            animationTimeLeft -= delta
            if (animationTimeLeft <= 0f) {
                completeAnimation()
            }
        }
    }

    /**
     * Move a card from a container to another container.
     * This can be animated later with [update].
     */
    fun moveCard(src: CardContainer, dst: CardContainer,
                 srcIndex: Int, dstIndex: Int) {
        assert(src !== dst)

        if (src.oldActors == null) {
            src.oldActors = ArrayList(src.actors)
        }
        if (dst.oldActors == null) {
            dst.oldActors = ArrayList(dst.actors)
        }

        // Move card
        dst.cards.add(dstIndex, src.cards.removeAt(srcIndex))

        // Move actor
        val actor = src.actors.removeAt(srcIndex)
        dst.actors.add(dstIndex, actor)

        cardsMoved = true
    }

    /**
     * Move a card after a [delay] in seconds.
     * The indexes are the ones at the moment of the move, not at the moment of this call.
     * @param callback Called when the card is moved.
     */
    fun moveCardDelayed(src: CardContainer, dst: CardContainer,
                        srcIndex: Int, dstIndex: Int, delay: Float,
                        callback: (() -> Unit)? = null) {
        if (delay <= 0f) {
            // No delay, call immediately.
            moveCard(src, dst, srcIndex, dstIndex)
            callback?.invoke()
            return
        }
        delayedCardMoves += DelayedCardMove(src, dst, srcIndex, dstIndex, delay, callback)
    }

    /**
     * Animate the dealing of [count] cards from a source container to a destination container.
     * @param callback Called after each card is passed.
     */
    fun deal(src: CardContainer, dst: CardContainer, count: Int,
             callback: (() -> Unit)? = null) {
        assert(src !== dst)
        assert(src.size >= count)

        val srcStartSize = src.size
        val dstStartSize = dst.size
        for (i in 0 until count) {
            moveCardDelayed(src, dst, srcStartSize - i - 1,
                    dstStartSize + i, i * DEAL_DELAY) {
                callback?.invoke()
                update()
            }
        }
    }

    /**
     * Start dragging a card actor. Returns a listener with methods to be called
     * when touch is dragged and on touch up.
     */
    fun dragCards(vararg cards: CardActor): CardDragListener? {
        if (draggedCards != null || cards.isEmpty()) return null

        require(!cardsMoved && !animationRunning) {
            "Cannot drag card while animation is running or pending."
        }

        val container = cards.first().parent as CardContainer
        require(cards.all { it.parent === container }) {
            "All cards dragged must be of the same container."
        }

        @Suppress("UNCHECKED_CAST")
        draggedCards = cards as Array<CardActor>

        // Add all actors of this container to the animation layer
        val actors = container.actors
        for (actor in actors) {
            val pos = actor.localToActorCoordinates(this, Vector2())
            addActor(actor)
            actor.x = pos.x
            actor.y = pos.y
            isVisible = true
        }

        if (container is CardStack) {
            // If container is a stack, only show the dragged and top cards.
            var topCard: CardActor? = null
            for (actor in actors) {
                if (actor !in cards) {
                    topCard = actor
                    actor.isVisible = false
                }
            }
            topCard?.isVisible = true
        }

        // Find the actor offset to the mouse position.
        val mousePos = stage.screenToStageCoordinates(Vector2(
                Gdx.input.x.toFloat(), Gdx.input.y.toFloat()))
        val offsets = Array(cards.size) { cards[it].stageToLocalCoordinates(mousePos.cpy()) }

        container.oldActors = ArrayList(container.actors)
        container.onAnimationStart()

        return CardDragListener(container, cards, offsets)
    }

    /**
     * Dispatch an animated update.
     * An update can be dispatched before last update ends, both animations will be merged.
     */
    fun update() {
        // Add pending animation for next frame.
        animationPending = true
        cardsMoved = false
    }

    /**
     * Animates the motion of the card actors on the stage. All actors of changed containers
     * are added to the animation layer and animated to their end position.
     */
    private fun startAnimation() {
        stage.keyboardFocus = this  // For ESC canceling

        animationPending = false
        animationRunning = true
        animationTimeLeft = 0f

        clearChildren()

        for (container in containers) {
            // Add the animated flag for all cards, even the ones not animated.
            // This temporily disable the actor so it can't be interacted with.
            for (actor in container.actors) {
                actor.animated = true
            }

            if (container.oldActors == null) continue

            container.onAnimationStart()

            // Find the start position of all actors of the container, in this group.
            val oldActors = container.oldActors!!
            val positions = Array(oldActors.size) { Vector2() }
            for (i in oldActors.indices) {
                val actor = oldActors[i]
                val pos = positions[i]
                if (actor.parent != null) {
                    // Find coordinates on the animation layer of actor in container.
                    actor.localToActorCoordinates(this, pos)
                } else {
                    // If parent is null, previous animation hasn't been completed so actor was
                    // still on the animation layer before children were cleared just above.
                    pos.set(actor.x, actor.y)
                }
            }
            container.clearChildren()

            // Add a marker actor to indicate where each container children start and end.
            addActor(MarkerActor(container))

            // Add all actors to the animation layer
            for (i in oldActors.indices) {
                val actor = oldActors[i]
                val pos = positions[i]
                addActor(actor)
                actor.src = container
                actor.x = pos.x
                actor.y = pos.y
            }
        }

        for (container in containers) {
            if (container.oldActors == null) continue

            // Find end position of all actors of the container, in the group.
            val endPositions = container.computeActorsPosition()

            // Add animation action to all actors.
            val newActors = container.actors
            var lastUnmovedActor: CardActor? = null
            for (i in newActors.indices) {
                val actor = newActors[i]
                actor.dst = container

                val containerEndPos = endPositions[i]
                val stageEndPos = container.localToActorCoordinates(this, Vector2(containerEndPos))
                val distance = Vector2(stageEndPos.x - actor.x, stageEndPos.y - actor.y)

                // For the card stack, only show the topmost card that doesn't move.
                // Hide others. This prevents useless overdrawing.
                if (container is CardStack && MathUtils.isEqual(distance.x, 0f)
                        && MathUtils.isEqual(distance.y, 0f)) {
                    actor.isVisible = false
                    lastUnmovedActor = actor
                } else {
                    actor.isVisible = true
                }

                var duration = UPDATE_DURATION

                // If actor was already moving and its animation still has some time left,
                // recycle the action by assigning a new duration.
                val prevAction = actor.actions.firstOrNull()
                if (prevAction is MoveCardAction) {
                    val remaining = prevAction.duration - prevAction.elapsed
                    val oldDistance = (prevAction.distance * remaining / prevAction.duration).len()
                    val newDistance = distance.len()
                    if (oldDistance != 0f && newDistance != 0f && remaining > 0.1f) {
                        // Extrapolate new duration from new to old distance ratio
                        duration = (newDistance / oldDistance * remaining)
                                .coerceIn(0.2f, UPDATE_DURATION)
                    }
                }

                val action = MoveCardAction(actor, distance, containerEndPos, duration)
                actor.clearActions()
                actor.addAction(action)

                if (duration > animationTimeLeft) {
                    animationTimeLeft = duration
                }
            }

            if (lastUnmovedActor != null) {
                // Show only the topmost card that doesn't move.
                lastUnmovedActor.isVisible = true
            }
        }
    }

    /**
     * Complete all animations and send the card actors to their final position and container.
     * If there was any delayed card moves, they are also completed.
     */
    fun completeAnimation() {
        if (!animationRunning) return

        // Complete all delayed card moves if there are any
        if (delayedCardMoves.isNotEmpty()) {
            for (move in delayedCardMoves) {
                moveCard(move.src, move.dst, move.srcIndex, move.dstIndex)
                move.callback?.invoke()
            }
            delayedCardMoves.clear()

            // Start and immediately stop animation
            startAnimation()
            completeAnimation()

            // Invalidate animated containers
            for (container in containers) {
                if (container.oldActors != null) {
                    container.invalidate()
                }
            }
            return
        }

        // Add animated actors to their container, keeping the same position on screen.
        for (container in containers) {
            // Remove the animated flag on all actors
            for (actor in container.actors) {
                actor.animated = false
            }

            if (container.oldActors == null) continue
            container.oldActors = null

            for (actor in container.actors) {
                assert(actor.actions.size == 1)
                assert(actor.parent === this)

                val action = actor.actions.first() as MoveCardAction
                container.addActor(actor)
                actor.src = null
                actor.dst = null
                actor.x = action.containerEndPos.x
                actor.y = action.containerEndPos.y
                actor.size = container.cardSize
                actor.clearActions()
            }
            container.invalidateHierarchy()

            container.onAnimationEnd()
        }

        assert(children.size == 0)

        animationPending = false
        animationRunning = false
        animationTimeLeft = 0f
    }

    private inner class MoveCardAction(
            private val cardActor: CardActor,
            val distance: Vector2,
            val containerEndPos: Vector2,
            val duration: Float) : Action() {

        var elapsed = 0f

        private var src = cardActor.src!!
        private val dst = cardActor.dst!!
        private val startX = cardActor.x
        private val startY = cardActor.y
        private val startSize = cardActor.size
        private val srcPos: Vector2
        private val containerDistance: Float

        init {
            // Compute the distance between the center of the src and dst containers
            if (src !== dst) {
                srcPos = src.localToActorCoordinates(this@AnimationLayer,
                        Vector2(src.width / 2, src.height / 2))
                val dstPos = dst.localToActorCoordinates(this@AnimationLayer,
                        Vector2(dst.width / 2, dst.height / 2))
                containerDistance = (dstPos - srcPos).len()
                changeLayer()
            } else {
                srcPos = Vector2.Zero
                containerDistance = 0f
            }
        }

        override fun act(delta: Float): Boolean {
            elapsed += delta

            // Change position and size
            val progress = Interpolation.smooth.applyBounded((elapsed / duration))
            cardActor.x = startX + progress * distance.x
            cardActor.y = startY + progress * distance.y
            cardActor.size = startSize + progress * (dst.cardSize - startSize)

            changeLayer()

            // Action never completes by itself, instead completeAnimation() removes it.
            return false
        }

        /**
         * Change the Z-index of the actor to the correct Z-index in its destination container,
         * if src and different from dst and if half the distance between the two was travelled.
         */
        fun changeLayer() {
            if (src === dst) return

            // Compute distance to source container
            val pos = cardActor.localToActorCoordinates(this@AnimationLayer,
                    Vector2(cardActor.width / 2, cardActor.height / 2))
            if ((pos - srcPos).len() < containerDistance / 2) {
                return
            }

            children.removeValue(cardActor, true)

            var newIndex = -1
            for (j in 0 until children.size) {
                val child = children[j]
                if (child is MarkerActor && child.container === dst) {
                    newIndex = j + 1
                    for (a in dst.actors) {
                        if (newIndex >= children.size) {
                            break
                        } else if (a === children[newIndex]) {
                            newIndex++
                        } else if (a === cardActor) {
                            break
                        }
                    }
                    break
                }
            }
            children.insert(newIndex, cardActor)

            src = dst  // Prevent changing layer in the future
        }
    }

    /**
     * Used to mark where the actors of a container start and end in the animation layer children.
     */
    private class MarkerActor(val container: CardContainer) : Actor() {
        init {
            isVisible = false
        }
    }

    /** A delayed card move between two containers. */
    private class DelayedCardMove(
            val src: CardContainer, val dst: CardContainer,
            val srcIndex: Int, val dstIndex: Int, var timeLeft: Float,
            val callback: (() -> Unit)?)


    inner class CardDragListener(
            private val container: CardContainer,
            private val cardActors: Array<CardActor>,
            private val offsets: Array<Vector2>) {

        private var dst = container

        fun touchDragged(stagePos: Vector2) {
            // Check if the container containing the mouse changed
            var newDst = container
            for (ctn in containers) {
                if (ctn.isVisible && ctn !== container && ctn.playListener != null) {
                    val pos = ctn.stageToLocalCoordinates(stagePos.cpy())
                    if (ctn.withinBounds(pos.x, pos.y) &&
                            ctn.playListener!!.canCardsBePlayed(cardActors, container)) {
                        // Mouse is in this container and card can be played.
                        newDst = ctn
                        break
                    }
                }
            }

            // Move actor to the mouse position with offset
            val layerPos = stageToLocalCoordinates(stagePos.cpy())
            for (i in cardActors.indices) {
                val cardActor = cardActors[i]
                val offset = offsets[i]
                cardActor.x = layerPos.x - offset.x
                cardActor.y = layerPos.y - offset.y

                if (newDst !== dst) {
                    // It changed, animate card size
                    cardActor.clearActions()
                    cardActor.addAction(object : Action() {
                        private var elapsed = 0f
                        private var startSize = cardActor.size
                        private var endSize = newDst.cardSize +
                                if (newDst === container) 0f else 15f

                        override fun act(delta: Float): Boolean {
                            elapsed += delta
                            val progress = Interpolation.smooth.applyBounded(elapsed / DRAG_SIZE_CHANGE_DURATION)
                            cardActor.size = startSize + progress * (endSize - startSize)
                            return elapsed >= DRAG_SIZE_CHANGE_DURATION
                        }
                    })
                }
            }

            dst = newDst
        }

        fun touchUp(stagePos: Vector2) {
            for (cardActor in cardActors) {
                cardActor.clearActions()
            }
            draggedCards = null

            // Call play listener of destination container
            if (dst !== container) {
                val dstPos = dst.stageToLocalCoordinates(stagePos)
                dst.playListener?.onCardsPlayed(cardActors, container, dstPos)
            }

            // Update the animation layer to put the card in its container.
            update()
        }
    }

    companion object {
        /** The duration of the update animation. */
        const val UPDATE_DURATION = 0.4f

        /** The duration between each card dealt in [deal].
         *  Values less than the update duration look worse. */
        const val DEAL_DELAY = 0.4f

        /** The duration of the card size animation when the dragged card hovers a container */
        const val DRAG_SIZE_CHANGE_DURATION = 0.25f
    }

}