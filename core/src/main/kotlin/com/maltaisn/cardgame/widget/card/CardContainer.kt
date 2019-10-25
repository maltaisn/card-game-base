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

import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.utils.Align
import com.maltaisn.cardgame.game.Card
import com.maltaisn.cardgame.widget.FboWidgetGroup
import com.maltaisn.cardgame.widget.action.ActionDelegate
import com.maltaisn.cardgame.widget.action.TimeAction
import ktx.actors.alpha
import ktx.collections.isNotEmpty
import ktx.math.minus
import ktx.math.vec2
import kotlin.math.min


/**
 * The base class for a widget group that contains card actors.
 * All card containers can support animations when registered to the [CardAnimationGroup].
 */
abstract class CardContainer(val cardStyle: CardActor.CardStyle) : FboWidgetGroup() {

    /** The actors for the cards. When an actor is moved, this list is immediately updated. */
    val actors: List<CardActor?>
        get() = _actors

    protected val _actors = mutableListOf<CardActor?>()

    /**
     * The cards in this container. When a card is moved, this list is immediately updated.
     * If reused, this value should be cached because a new list is created on every get call.
     */
    var cards: List<Card?>
        get() = actors.map { it?.card }
        set(value) {
            updateCards(value)
        }

    /** The actors that were in the container before any card was moved. Null if no card was moved. */
    internal var oldActors: MutableList<CardActor?>? = null

    /** The number of cards in this container, including `null` cards. */
    inline val size: Int
        get() = actors.size

    /** Whether the cards in this container are shown. */
    var visibility = Visibility.ALL

    /**
     * Whether the container is shown or not.
     * Like [isVisible] but with the correct value during a transition.
     * When changed directly, behaves like [setVisible].
     */
    var shown: Boolean
        get() = _shown
        set(value) {
            _shown = value
            _isVisible = value
            transitionAction?.end()
        }

    private var _shown = true

    private var _isVisible
        get() = isVisible
        set(value) {
            super.setVisible(value)
        }


    /**
     * Whether this card container is enabled. If disabled, card actors will be too,
     * drag listener and play listener will be disabled.
     */
    var enabled = true
        set(value) {
            field = value
            touchable = if (enabled) Touchable.enabled else Touchable.disabled
            for (actor in actors) {
                actor?.enabled = enabled
            }
        }

    /** The size of the card actors in the container. */
    var cardSize: Float = CardActor.SIZE_NORMAL
        set(value) {
            field = value
            for (actor in actors) {
                actor?.size = value
            }
        }

    /** Alignment of the container's content. */
    var align = Align.center

    /** Listener called when a card at an index is clicked, or `null` for none. */
    var clickListener: ((actor: CardActor, index: Int) -> Unit)? = null

    /** Listener called when a card at an index is long clicked, or `null` for none. */
    var longClickListener: ((actor: CardActor, index: Int) -> Unit)? = null
        set(value) {
            if (field == null || value == null) {
                // Set or remove container listener. This is necessary since long click may
                // block normal click so if container's long click is set to null, card actor's should be too.
                for (actor in actors) {
                    actor?.longClickListener = if (value == null) null else ::onCardLongClicked
                }
            }
            field = value
        }

    /**
     * Listener called when a card is dragged, or `null` for if not draggable.
     * Can return an input listener provided by [CardAnimationGroup.dragCards] to
     * drag the card, or can return `null` to not drag the card.
     * Not called if container is disabled.
     */
    var dragListener: ((CardActor) -> CardAnimationGroup.CardDragger?)? = null
        set(value) {
            if (field != null && value == null) {
                for (actor in actors) {
                    actor?.removeListener(cardInputListener)
                }
            } else if (field == null && value != null) {
                for (actor in actors) {
                    actor?.addListener(cardInputListener)
                }
            }
            field = value
        }

    /** Listener called when a card is played on this container, or `null` if not playable. */
    var playListener: PlayListener? = null

    // Size properties
    protected var sizeInvalid = true
    protected var computedWidth = 0f
    protected var computedHeight = 0f
    protected var cardWidth = 0f
    protected var cardHeight = 0f
    protected var cardScale = 0f

    private val translate = vec2()

    private var transitionAction by ActionDelegate<TimeAction>()


    override fun draw(batch: Batch, parentAlpha: Float) {
        x += translate.x
        y += translate.y
        super.draw(batch, parentAlpha)
        x -= translate.x
        y -= translate.y
    }

    override fun childrenChanged() {
        // Overriden to prevent invalidation.
    }

    override fun invalidate() {
        super.invalidate()
        sizeInvalid = true
    }

    override fun clearActions() {
        super.clearActions()
        transitionAction?.end()
    }

    override fun setVisible(visible: Boolean) {
        shown = visible
    }

    /**
     * Re-add all actors to the container, with the correct Z-index
     */
    protected fun update() {
        clearChildren()
        for (actor in actors) {
            if (actor != null) {
                actor.size = cardSize
                actor.enabled = enabled
                if (visibility == Visibility.ALL) {
                    actor.shown = true
                } else if (visibility == Visibility.NONE) {
                    actor.shown = false
                }
                addActor(actor)
            }
        }
    }

    ////////// LISTENERS //////////
    protected open fun onCardClicked(actor: CardActor) {
        if (clickListener != null) {
            val index = actors.indexOf(actor)
            clickListener?.invoke(actor, index)
        }
    }

    protected open fun onCardLongClicked(actor: CardActor) {
        if (longClickListener != null) {
            val index = actors.indexOf(actor)
            longClickListener?.invoke(actor, index)
        }
    }


    /** The input listener set on all actors. */
    private val cardInputListener = object : InputListener() {
        private var cardDragger: CardAnimationGroup.CardDragger? = null
        private var startPos = vec2()

        override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
            startPos = vec2(event.stageX, event.stageY)
            return dragListener != null && pointer == Input.Buttons.LEFT
        }

        override fun touchDragged(event: InputEvent, x: Float, y: Float, pointer: Int) {
            val pos = vec2(event.stageX, event.stageY)
            if (pointer == Input.Buttons.LEFT && (cardDragger != null ||
                            dragListener != null && (pos - startPos).len() > MIN_DRAG_DISTANCE)) {
                // Start dragging only when touch has been dragged for a minimum distance
                if (cardDragger == null) {
                    cardDragger = dragListener!!(event.listenerActor as CardActor)
                }
                cardDragger?.touchDragged(pos)
            }
        }

        override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
            if (cardDragger != null && pointer == Input.Buttons.LEFT) {
                cardDragger?.touchUp(vec2(event.stageX, event.stageY))
                cardDragger = null
            }
        }
    }

    interface PlayListener {
        /**
         * When a card is dragged over this container, returns whether or not it can be played in it.
         * Not called if container is disabled.
         */
        fun canCardsBePlayed(actors: List<CardActor>, src: CardContainer, pos: Vector2): Boolean

        /**
         * Called when cards from another container are dragged to this container at
         * a position in the container coordinates, and [canCardsBePlayed] returned true.
         * The animation group is updated automatically afterwards.
         * Not called if container is disabled.
         */
        fun onCardsPlayed(actors: List<CardActor>, src: CardContainer, pos: Vector2)
    }

    ////////// CARDS //////////
    /**
     * Change the cards in this container to new cards.
     */
    protected open fun updateCards(newCards: List<Card?>) {
        // Bind existing actors to new cards
        val oldActors = mutableListOf<CardActor>()
        for (actor in actors) {
            if (actor != null) {
                oldActors += actor
            }
        }
        _actors.clear()

        for (card in newCards) {
            if (card != null) {
                if (oldActors.isEmpty()) {
                    break
                }
                val actor = oldActors.removeAt(oldActors.lastIndex)
                actor.card = card
                actor.highlightable = true
                actor.highlighted = false
                _actors += actor
            } else {
                _actors.add(null)
            }
        }

        // Remove listeners from unused old actors.
        for (actor in oldActors) {
            removeCardActorListeners(actor)
        }

        // Add new actors if there aren't enough.
        while (actors.size < newCards.size) {
            val card = newCards[actors.size]
            if (card != null) {
                val actor = CardActor(cardStyle, card)
                addCardActorListeners(actor)
                _actors += actor
            } else {
                _actors.add(null)
            }
        }

        update()

        invalidateHierarchy()
    }

    private fun addCardActorListeners(actor: CardActor) {
        actor.clickListener = ::onCardClicked
        actor.longClickListener = if (longClickListener == null) null else ::onCardLongClicked
        if (dragListener != null) actor.listeners.add(cardInputListener)
    }

    private fun removeCardActorListeners(actor: CardActor) {
        actor.clickListener = null
        actor.longClickListener = null
        if (dragListener != null) actor.listeners.removeValue(cardInputListener, true)
    }

    enum class Visibility {
        /** Automatically show all cards. */
        ALL,
        /** Automatically hide all cards. */
        NONE,
        /** Must manually set cards shown state. */
        MIXED
    }

    ////////// LAYOUT //////////
    override fun layout() {
        if (children.isNotEmpty()) {
            val positions = computeActorsPosition()
            for (i in actors.indices) {
                val pos = positions[i]
                actors[i]?.setPosition(pos.x, pos.y)
            }
        }
    }

    override fun getPrefWidth(): Float {
        computeSize()
        return computedWidth
    }

    override fun getPrefHeight(): Float {
        computeSize()
        return computedHeight
    }

    /**
     * Returns a list of the positions of the actors in this container.
     * The list is indexed like the actors list. Should return positions for null cards too.
     */
    internal abstract fun computeActorsPosition(): List<Vector2>

    /** Returns the index of the card nearest container coordinates, ([x], [y]), in the range `0..size-1`. */
    abstract fun findCardPositionForCoordinates(x: Float, y: Float): Int

    /** Returns the index at which a card should be inserted from container coordinates, ([x], [y]), in the range `0..size`. */
    abstract fun findInsertPositionForCoordinates(x: Float, y: Float): Int

    /**
     * Compute the minimum size this container can be.
     */
    internal open fun computeSize() {
        if (!sizeInvalid) return
        sizeInvalid = false

        cardScale = cardSize / cardStyle.cardWidth
        cardWidth = cardStyle.cardWidth * cardScale
        cardHeight = cardStyle.cardHeight * cardScale
    }

    override fun sizeChanged() {
        super.sizeChanged()

        // Set origin to center for good rotation.
        // Rotation is not supported by animations though.
        originX = width / 2
        originY = height / 2
    }

    /**
     * Returns a vector of the offset needed to respect
     * the align and the padding, given a required size.
     */
    protected fun computeAlignOffset(requiredWidth: Float, requiredHeight: Float): Vector2 {
        val offsetX = when {
            Align.isCenterHorizontal(align) || align == Align.center -> (width - requiredWidth) / 2
            Align.isLeft(align) -> 0f
            Align.isRight(align) -> width - requiredWidth
            else -> 0f
        }
        val offsetY = when {
            Align.isCenterVertical(align) || align == Align.center -> (height - requiredHeight) / 2
            Align.isTop(align) -> height - requiredHeight
            Align.isBottom(align) -> 0f
            else -> 0f
        }

        return vec2(offsetX, offsetY)
    }


    ////////// TRANSITIONS //////////
    /**
     * Animate a visibility change by fading in or out.
     * A fade can be performed during another transition, the previous one will be canceled.
     * @param shown New visibility.
     */
    fun fade(shown: Boolean) {
        if (this.shown == shown) return
        _shown = shown

        if (transitionAction !is FadeTransitionAction) {
            transitionAction = FadeTransitionAction()
        }
    }

    /**
     * Animate a visibility change by sliding in or out in a [direction].
     * A slide can be performed during another transition, the previous one will be canceled.
     * @param shown New visibility.
     */
    fun slide(shown: Boolean, direction: Direction) {
        if (this.shown == shown) return
        _shown = shown

        val currentAction = transitionAction
        if (currentAction is SlideTransitionAction) {
            currentAction.start(direction)
        } else {
            val action = SlideTransitionAction()
            action.start(direction)
            transitionAction = action
        }
    }

    private inner class FadeTransitionAction :
            TimeAction(TRANSITION_DURATION, TRANSITION_INTERPOLATION, reversed = !shown) {

        init {
            _isVisible = true
            renderToFrameBuffer = true
            alpha = if (shown) 0f else 1f
            translate.setZero()
        }

        override fun update(progress: Float) {
            reversed = !shown
            alpha = progress
        }

        override fun end() {
            _isVisible = shown
            alpha = 1f
            renderToFrameBuffer = false
            transitionAction = null
        }
    }

    private inner class SlideTransitionAction :
            TimeAction(TRANSITION_DURATION, TRANSITION_INTERPOLATION) {

        private val startOffset = vec2()
        private val endOffset = vec2()

        fun start(direction: Direction) {
            elapsed = 0f

            _isVisible = false
            renderToFrameBuffer = false
            alpha = 1f
            translate.setZero()

            startOffset.setZero()
            endOffset.setZero()
            val offset = if (shown) startOffset else endOffset
            when (direction) {
                Direction.LEFT -> offset.x -= width
                Direction.RIGHT -> offset.x += width
                Direction.UP -> offset.y += height
                Direction.DOWN -> offset.y -= height
            }
        }

        override fun update(progress: Float) {
            _isVisible = true
            translate.x = startOffset.x + (endOffset.x - startOffset.x) * progress
            translate.y = startOffset.y + (endOffset.y - startOffset.y) * progress
        }

        override fun end() {
            _isVisible = shown
            translate.setZero()
            transitionAction = null
        }
    }

    enum class Direction {
        LEFT, RIGHT, UP, DOWN
    }

    ////////// ANIMATION //////////
    /**
     * Make this container changed, so that it will be
     * animated when [CardAnimationGroup.update] is called.
     */
    fun requestUpdate() {
        if (oldActors == null) {
            oldActors = actors.toMutableList()
        }
    }

    internal fun moveCardTo(dst: CardContainer, srcIndex: Int, dstIndex: Int,
                            replaceSrc: Boolean = false, replaceDst: Boolean = false) {
        requestUpdate()
        dst.requestUpdate()

        // Move card and actor
        val actor = _actors.removeAt(srcIndex)
        if (replaceSrc) {
            _actors.add(srcIndex, null)
        }
        if (replaceDst) {
            val replaced = dst.actors[dstIndex]
            require(replaced == null) {
                "Card must replace a null card in destination, found '$replaced' instead."
            }
            dst._actors[dstIndex] = actor
        } else {
            dst._actors.add(dstIndex, actor)
        }
    }

    /**
     * Move the smallest sublist of [actors] containing all of [cardActors] to a new index.
     */
    internal fun moveCards(cardActors: MutableList<CardActor>, toIndex: Int) {
        if (cardActors.size == size) return  // If moving all actors, cannot change anything.

        var low = size
        var high = -1
        for (actor in cardActors) {
            val index = actors.indexOf(actor)
            if (index < low) low = index
            if (index > high) high = index
        }

        if (low == toIndex) return  // No move necessary

        val moved = _actors.subList(low, high + 1)
        val copy = moved.toMutableList()
        moved.clear()
        _actors.addAll(min(actors.size, toIndex), copy)
    }

    internal open fun onAnimationStart() {
        transitionAction?.end()
        alpha = 1f

        // Reset old actors
        for (actor in oldActors!!) {
            actor?.let {
                removeCardActorListeners(it)
                it.enabled = true
                it.highlightable = true
            }
        }

        // Apply this container visibility and enabled state to the new actors
        for (actor in actors) {
            actor ?: continue
            if (visibility == Visibility.ALL) {
                actor.shown = true
            } else if (visibility == Visibility.NONE) {
                actor.shown = false
            }
            actor.enabled = enabled
        }
    }

    internal open fun onAnimationEnd() {
        for (actor in actors) {
            if (actor != null) {
                addCardActorListeners(actor)
            }
        }
    }


    override fun toString() = "[cards: $cards, visibility: ${visibility.toString().toLowerCase()}]"


    companion object {
        /** The duration of the transitions. */
        const val TRANSITION_DURATION = 0.5f

        /** The minimum dragging distance in pixels for a card to be effectively dragged. */
        private const val MIN_DRAG_DISTANCE = 20f

        private val TRANSITION_INTERPOLATION: Interpolation = Interpolation.smooth
    }

}
