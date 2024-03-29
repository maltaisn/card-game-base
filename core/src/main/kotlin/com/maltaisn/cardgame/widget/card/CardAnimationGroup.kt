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

package com.maltaisn.cardgame.widget.card

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.*
import com.maltaisn.cardgame.utils.withinBounds
import com.maltaisn.cardgame.widget.action.TimeAction
import com.maltaisn.cardgame.widget.action.applyBounded
import ktx.actors.setKeyboardFocus
import ktx.math.div
import ktx.math.minus
import ktx.math.times
import ktx.math.vec2


/**
 * The group in which the card actors of [CardContainer] go when they
 * are animated. Animation can happen within a container (eg: sorting) or
 * between two containers.
 */
class CardAnimationGroup : Group() {

    /**
     * The list of all card containers on the stage.
     * It's update by the container themselves when they are added to the stage.
     */
    private val containers = mutableListOf<CardContainer>()

    private val delayedCardMoves = mutableListOf<DelayedCardMove>()

    private var draggedCards: List<CardActor>? = null

    private var cardsMoved = false
    private var animationPending = false
    private var animationDuration = 0f
    private var animationTimeLeft = 0f

    var animationRunning = false
        private set

    init {
        touchable = Touchable.childrenOnly

        addListener(object : InputListener() {
            override fun keyUp(event: InputEvent, keycode: Int): Boolean {
                if (keycode == Input.Keys.ESCAPE) {
                    // Stop all animations with escape.
                    dispatchDelayedMoves()
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
            move.timeLeft -= delta * TimeAction.SPEED_MULTIPLIER
            if (move.timeLeft < 0) {
                delayedCardMoves.removeAt(i)
                move.doMove(this)
            }
        }

        // Start or complete 
        if (animationPending) {
            startAnimation()
        } else if (animationRunning) {
            animationTimeLeft -= delta * TimeAction.SPEED_MULTIPLIER
            if (animationTimeLeft <= 0f) {
                completeAnimation()
            }
        }

        if (delayedCardMoves.isNotEmpty()) {
            // If any moves are delayed, must keep rendering so act() is called.
            Gdx.graphics.requestRendering()
        }
    }

    /**
     * Register card [containers] to allow them to animate card moves.
     * The order of registration matters for Z-index, the first containers will be put below.
     */
    fun register(vararg containers: CardContainer) {
        for (container in containers) {
            if (container !in this.containers) {
                this.containers += container
            }
        }
    }

    /**
     * Move a card from a container to another container.
     * This can be animated later with [update].
     * @param replaceSrc If true, the card in source at [srcIndex] will be replaced with null.
     * @param replaceDst If true, the null card in destination at [dstIndex] will be replaced.
     */
    fun moveCard(src: CardContainer, dst: CardContainer, srcIndex: Int, dstIndex: Int,
                 replaceSrc: Boolean = false, replaceDst: Boolean = false) {
        require(src !== dst) {
            "Cannot move card within the same card container."
        }
        require(src in containers || dst in containers) {
            "Cannot perform move on an unregistered card container."
        }
        require((replaceSrc || src !is CardTrick) && (replaceDst || dst !is CardTrick)) {
            "Cards cannot be inserted or removed from a card trick, they must be replaced."
        }

        src.moveCardTo(dst, srcIndex, dstIndex, replaceSrc, replaceDst)

        cardsMoved = true
    }

    /**
     * Move a card after a [delay] in seconds.
     * @param srcIndex Index of the card in the [src] container at the moment of the call.
     * If the position cards in the container change, the correct card will still be moved.
     * @param dstIndex Index in the [dst] container at which to insert the card. This is the
     * index at the moment of the move, not at the moment this method is called unline [srcIndex].
     * @param callback Called when the card is moved.
     * @see moveCard
     */
    fun moveCardDelayed(src: CardContainer, dst: CardContainer, srcIndex: Int, dstIndex: Int,
                        replaceSrc: Boolean = false, replaceDst: Boolean = false,
                        delay: Float, callback: (() -> Unit)? = null) {
        val move = DelayedCardMove(src, dst, srcIndex, dstIndex,
                replaceSrc, replaceDst, delay, callback)
        delayedCardMoves += move
    }

    /**
     * Animate the dealing of [count] cards from a source container to a destination container.
     * @param replaceSrc If true, cards removed from source will be replaced with null.
     * @param replaceDst If true, cards will replace null cards in destination.
     * @param fromLast If true, cards will be taken from the last indexes of [src]. If false,
     * they will be taken from the first indexes.
     * @param toLast Same as [fromLast] for the [dst] container.
     * @param callback Called after each card is passed.
     * @param delay The delay between each card deal.
     * @param updateDuration The duration of the animation for each card. If duration is smaller than
     * [delay], each card animation will end before next one begins, otherwise animations are merged.
     */
    fun deal(src: CardContainer, dst: CardContainer, count: Int,
             replaceSrc: Boolean = false, replaceDst: Boolean = false,
             fromLast: Boolean = true, toLast: Boolean = true,
             delay: Float = DEFAULT_UPDATE_DURATION,
             updateDuration: Float = delay,
             callback: (() -> Unit)? = null) {
        require(src.size >= count) {
            "Not enough cards in source container to perform deal."
        }

        for (i in 0 until count) {
            val srcIndex = if (fromLast) src.size - i - 1 else i
            val dstIndex = if (replaceDst) {
                if (toLast) dst.size - i - 1 else i
            } else {
                if (toLast) dst.size + i else 0
            }
            moveCardDelayed(src, dst, srcIndex, dstIndex,
                    replaceSrc, replaceDst, i * delay) {
                callback?.invoke()
                update(updateDuration)
            }
        }
    }

    /**
     * Start dragging a card actor. Returns a listener with methods to be called
     * when touch is dragged and on touch up.
     */
    fun dragCards(vararg cards: CardActor): CardDragger? {
        if (draggedCards != null || cards.isEmpty()
                || cardsMoved || animationRunning) {
            // Can't drag card if animation is running or pending
            // or if cards are already being dragged.
            return null
        }

        val container = cards.first().parent as CardContainer
        require(cards.all { it.parent === container }) {
            "All cards dragged must be of the same container."
        }

        draggedCards = cards.toList()

        // Add all actors of this container to the animation group
        val actors = container.actors
        for (actor in actors) {
            if (actor != null) {
                val pos = actor.localToActorCoordinates(this, vec2())
                addActor(actor)
                actor.x = pos.x
                actor.y = pos.y
                actor.isVisible = true
            }
        }

        if (container is CardStack) {
            // If container is a stack, only show the dragged and top cards.
            var topCard: CardActor? = null
            for (actor in actors) {
                if (actor != null && actor !in cards) {
                    topCard = actor
                    actor.isVisible = false
                }
            }
            topCard?.isVisible = true
        }

        // Find the actor offset to the mouse position.
        val mousePos = stage.screenToStageCoordinates(
                vec2(Gdx.input.x.toFloat(), Gdx.input.y.toFloat()))
        val offsets = List(cards.size) { cards[it].stageToLocalCoordinates(mousePos.cpy()) }

        container.oldActors = container.actors.toMutableList()
        container.onAnimationStart()

        return CardDragger(container, draggedCards!!, offsets)
    }

    /**
     * Dispatch an animated update of the changed card containers on the stage.
     * An update can be dispatched before last update ends, both animations will be merged.
     * A card container is changed when any of card was added, moved or removed since last update.
     * To also update a container that wasn't changed call [CardContainer.requestUpdate] before.
     * The update will finish after a [duration].
     */
    fun update(duration: Float = DEFAULT_UPDATE_DURATION) {
        // Add pending animation for next frame.
        animationDuration = duration
        animationPending = true
        cardsMoved = false
    }

    /**
     * Animates the motion of the card actors on the stage. All actors of changed containers
     * are added to the animation group and animated to their end position.
     */
    private fun startAnimation() {
        setKeyboardFocus(true)  // For ESC instant complete

        animationPending = false
        animationRunning = true
        animationTimeLeft = 0f

        clearChildren()

        for (container in containers) {
            // Add the animated flag for all cards, even the ones not animated.
            // This temporily disable the actor so it can't be interacted with.
            for (actor in container.actors) {
                actor?.animated = true
            }

            if (container.oldActors == null) {
                // Container cards didn't change and no animation was requested.
                continue
            }

            container.onAnimationStart()

            // Find the start position of all actors of the container, in this group.
            val oldActors = container.oldActors!!
            val positions = List(oldActors.size) { vec2() }
            for (i in oldActors.indices) {
                val actor = oldActors[i] ?: continue
                val pos = positions[i]
                if (actor.parent != null) {
                    // Find coordinates on the animation group of actor in container.
                    actor.localToActorCoordinates(this, pos)
                } else {
                    // If parent is null, previous animation hasn't been completed so actor was
                    // still on the animation group before children were cleared just above.
                    pos.set(actor.x, actor.y)
                }
            }
            container.clearChildren()

            // Add a marker actor to indicate where each container children start and end.
            addActor(MarkerActor(container))

            // Add all actors to the animation group
            for (i in oldActors.indices) {
                val actor = oldActors[i] ?: continue
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
                val actor = newActors[i] ?: continue
                actor.dst = container
                actor.enabled = container.enabled

                val containerEndPos = endPositions[i]
                val stageEndPos = container.localToActorCoordinates(this, containerEndPos.cpy())
                val distance = vec2(stageEndPos.x - actor.x, stageEndPos.y - actor.y)

                // For the card stack, only show the topmost card that doesn't move.
                // Hide others. This prevents useless overdrawing.
                if (container is CardStack && MathUtils.isEqual(distance.x, 0f)
                        && MathUtils.isEqual(distance.y, 0f)) {
                    actor.isVisible = false
                    lastUnmovedActor = actor
                } else {
                    actor.isVisible = true
                }

                var duration = animationDuration

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
                                .coerceIn(animationDuration / 2, animationDuration)
                    }
                }

                actor.moveAction = MoveCardAction(actor, distance, containerEndPos, duration)

                if (duration > animationTimeLeft) {
                    animationTimeLeft = duration
                }
            }

            // Show only the topmost card that doesn't move.
            lastUnmovedActor?.isVisible = true
        }
    }

    /**
     * Complete all animations and send the card actors to their final position and container.
     */
    fun completeAnimation() {
        if (!animationRunning) return

        // Add animated actors to their container, keeping the same position on screen.
        for (container in containers) {
            // Remove the animated flag on all actors
            for (actor in container.actors) {
                actor?.animated = false
            }

            if (container.oldActors == null) continue
            container.oldActors = null

            assert(container.children.isEmpty)
            for (actor in container.actors) {
                actor?.apply {
//                    assert(actions.size == 1)
                    assert(parent === this@CardAnimationGroup)

                    val action = moveAction!!  // FIXME NPE here when called just before animation ends?
                    container.addActor(this)
                    src = null
                    dst = null
                    x = action.containerEndPos.x
                    y = action.containerEndPos.y
                    size = container.cardSize
                    moveAction = null
                }
            }

            container.invalidateHierarchy()
            container.onAnimationEnd()
        }

        clearChildren()  // Removes marker actors

        animationPending = false
        animationRunning = false
        animationTimeLeft = 0f
    }

    /**
     * Dispatch all remaining delayed moves, without completing them.
     */
    fun dispatchDelayedMoves() {
        if (delayedCardMoves.isNotEmpty()) {
            for (move in delayedCardMoves) {
                move.doMove(this)
            }
            delayedCardMoves.clear()

            startAnimation()
        }
    }

    /**
     * CLear all delayed moves so they won't be performed.
     */
    fun clearDelayedMoves() {
        delayedCardMoves.clear()
    }

    internal inner class MoveCardAction(
            private val cardActor: CardActor,
            val distance: Vector2,
            val containerEndPos: Vector2,
            val duration: Float) : Action() {

        var elapsed = 0f
        private val startX = cardActor.x
        private val startY = cardActor.y
        private val startSize = cardActor.size

        private val src = cardActor.src!!
        private val dst = cardActor.dst!!
        private var containerRect: Rectangle? = null

        init {
            // Compute the dst container rectangle bounds.
            if (src !== dst) {
                val start = dst.localToActorCoordinates(this@CardAnimationGroup, vec2())
                val end = dst.localToActorCoordinates(this@CardAnimationGroup, vec2(dst.width, dst.height))
                containerRect = Rectangle(start.x, start.y, end.x - start.x, end.y - start.y)
                changeLayer()
            } // else, card container wasn't changed so initial Z-index stays correct.
        }

        override fun act(delta: Float): Boolean {
            elapsed += delta

            // Change position and size
            val progress = Interpolation.smooth.applyBounded(
                    elapsed * TimeAction.SPEED_MULTIPLIER / duration)
            cardActor.x = startX + progress * distance.x
            cardActor.y = startY + progress * distance.y
            cardActor.size = startSize + progress * (dst.cardSize - startSize)

            changeLayer()

            // Action never completes by itself, instead completeAnimation() removes it.
            return false
        }

        /**
         * Change the Z-index of the actor to the correct Z-index in its destination container.
         */
        private fun changeLayer() {
            if (containerRect == null) return

            // Check if the card actor's center is within the destination container rectangle bounds.
            val cardCenter = cardActor.localToActorCoordinates(this@CardAnimationGroup,
                    vec2(cardActor.width / 2, cardActor.height / 2))
            if (cardCenter !in containerRect!!) {
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

            containerRect = null
        }
    }

    /**
     * Used to mark where the actors of a container start and end in the animation group children.
     */
    private class MarkerActor(val container: CardContainer) : Actor() {
        init {
            isVisible = false
        }
    }

    /** A delayed card move between two containers. */
    private class DelayedCardMove(
            private val src: CardContainer,
            private val dst: CardContainer,
            srcIndex: Int,
            private val dstIndex: Int,
            private val replaceSrc: Boolean = false,
            private val replaceDst: Boolean = false,
            var timeLeft: Float,
            private val callback: (() -> Unit)?) {

        private val srcActor = src.actors[srcIndex]

        fun doMove(cardAnimationGroup: CardAnimationGroup) {
            val index = src.actors.indexOf(srcActor)
            check(index >= 0) { "Delayed card move can't be done, card isn't in source container." }
            cardAnimationGroup.moveCard(src, dst, index, dstIndex, replaceSrc, replaceDst)
            callback?.invoke()
        }
    }

    /** A class that manages the dragging of one or multiple cards in the animation group. */
    inner class CardDragger(
            private val container: CardContainer,
            private val cardActors: List<CardActor>,
            private val offsets: List<Vector2>) {

        /**
         * Whether the cards dragged can be rearranged in the source container.
         * If many cards are being dragged, the smallest chunk of cards containing them all will be moved.
         * This is not applicable if the container is a card stack.
         */
        var rearrangeable = false
            set(value) {
                require(container !is CardStack) { "A card stack cannot be rearranged by dragging." }
                field = value
            }

        private var dst = container

        fun touchDragged(stagePos: Vector2) {
            // Check if the container containing the mouse changed
            var newDst = container
            for (ctn in containers) {
                if (ctn.isVisible && ctn.enabled && ctn !== container && ctn.playListener != null) {
                    val pos = ctn.stageToLocalCoordinates(stagePos.cpy())
                    if (ctn.withinBounds(pos.x, pos.y) &&
                            ctn.playListener!!.canCardsBePlayed(cardActors, container, pos)) {
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
                    cardActor.moveAction = null
                    cardActor.addAction(object : TimeAction(0.25f, Interpolation.smooth) {
                        private var startSize = cardActor.size
                        private var endSize = newDst.cardSize +
                                if (newDst === container) 0f else 15f

                        override fun update(progress: Float) {
                            cardActor.size = startSize + progress * (endSize - startSize)
                        }
                    })
                }
            }

            // Rearrange cards in container if needed
            if (rearrangeable) {
                val containerPos = container.stageToLocalCoordinates(stagePos.cpy())
                val newPos = container.findCardPositionForCoordinates(containerPos.x, containerPos.y)
                val oldPos = container.actors.indexOf(cardActors.first())
                if (newPos != oldPos) {
                    val actors = container.actors

                    // Rearrange actors and cards lists
                    container.moveCards(cardActors.toMutableList(), newPos)

                    // Re-add all actors to fix the Z-index
                    clearChildren()
                    for (actor in actors) {
                        if (actor != null) {
                            addActor(actor)
                            // Actor position is persisted through the re-add
                        }
                    }

                    // Find the new coordinates of each card on the stage
                    val cardPositions = container.computeActorsPosition()
                    for (pos in cardPositions) {
                        container.localToActorCoordinates(this@CardAnimationGroup, pos)
                    }

                    // Animate undragged actors to their new position
                    for (i in actors.indices) {
                        val actor = actors[i]
                        if (actor != null && actor !in cardActors) {
                            val startPos = vec2(actor.x, actor.y)
                            val distance = cardPositions[i] - startPos
                            actor.moveAction = null
                            actor.addAction(object : TimeAction(0.3f, Interpolation.pow2Out) {
                                override fun update(progress: Float) {
                                    actor.x = startPos.x + progress * distance.x
                                    actor.y = startPos.y + progress * distance.y
                                }
                            })
                        }
                    }
                }
            }

            dst = newDst
        }

        fun touchUp(stagePos: Vector2) {
            for (cardActor in cardActors) {
                cardActor.moveAction = null
            }
            draggedCards = null

            if (rearrangeable && dst === container) {
                // Set old actors, again. It was set when dragCards was called, but order of the
                // cards may have changed and correct order is needed for correct Z-index.
                container.oldActors = container.actors.toMutableList()
            }

            // Call play listener of destination container
            if (dst !== container && dst.enabled) {
                val dstPos = dst.stageToLocalCoordinates(stagePos)
                dst.playListener?.onCardsPlayed(cardActors, container, dstPos)
            }

            // Update the animation group to put the card in its container.
            update()
        }
    }

    companion object {
        /** The default duration of the update  */
        const val DEFAULT_UPDATE_DURATION = 0.4f
    }

}
