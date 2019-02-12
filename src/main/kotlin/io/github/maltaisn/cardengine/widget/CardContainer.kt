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

import com.badlogic.gdx.Input
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Action
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup
import com.badlogic.gdx.utils.Align
import io.github.maltaisn.cardengine.CardGameScreen
import io.github.maltaisn.cardengine.CardSpriteLoader
import io.github.maltaisn.cardengine.applyBounded
import io.github.maltaisn.cardengine.core.Card
import io.github.maltaisn.cardengine.core.Deck
import ktx.math.minus


/**
 * The base class for a widget group that contains card actors.
 * All card containers support animations with the [AnimationLayer],
 * however the container must be in a [CardGameScreen] stage to support animations.
 */
abstract class CardContainer(protected val cardLoader: CardSpriteLoader) : WidgetGroup() {

    /** The cards in this container. When a card is moved, this list is immediately updated. */
    internal val cards = Deck<Card>()

    /** The actors for the cards. When a card is moved, this list is immediately updated. */
    internal val actors = ArrayList<CardActor>()

    /** The actors that were in the container before any card was moved. Null if no card was moved. */
    internal var oldActors: ArrayList<CardActor>? = null

    /** The number of cards in this container. */
    val size: Int
        get() = cards.size

    /** Whether the cards in this container are shown. */
    var visibility = Visibility.ALL

    /** The size of the card actors in the container. */
    var cardSize: Float = CardActor.CARD_SIZE_NORMAL

    /** Alignment of the container's content. */
    var alignment = Align.center
    protected var computedWidth = 0f
    protected var computedHeight = 0f
    private var sizeInvalid = true

    // Card metrics
    protected var cardWidth = 0f
    protected var cardHeight = 0f
    protected var cardScale = 0f

    /** Listener called when a card is clicked, or `null` for none. */
    private val clickListeners = ArrayList<ClickListener>()

    /** Listener called when a card is long clicked, or `null` for none. */
    private val longClickListeners = ArrayList<LongClickListener>()

    /** Listener called when a card is dragged, or `null` for if not draggable. */
    private var dragListener: DragListener? = null

    /** Listener called when a card is played on this container, or `null` if not playable. */
    internal var playListener: PlayListener? = null


    private val internalClickListener = object : CardActor.ClickListener {
        override fun onCardActorClicked(actor: CardActor) {
            val index = actors.indexOf(actor)
            for (listener in clickListeners) {
                listener.onCardClicked(actor, index)
            }
        }
    }

    private val internalLongClickListener = object : CardActor.LongClickListener {
        override fun onCardActorLongClicked(actor: CardActor) {
            val index = actors.indexOf(actor)
            for (listener in longClickListeners) {
                listener.onCardLongClicked(actor, index)
            }
        }
    }

    /** The input listener set on all actors in this container */
    private val internalInputListener = object : InputListener() {
        private var cardDragListener: AnimationLayer.CardDragListener? = null
        private var startPos = Vector2()

        override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
            startPos = Vector2(event.stageX, event.stageY)
            return dragListener != null && pointer == Input.Buttons.LEFT
        }

        override fun touchDragged(event: InputEvent, x: Float, y: Float, pointer: Int) {
            val pos = Vector2(event.stageX, event.stageY)
            if (pointer == Input.Buttons.LEFT && (cardDragListener != null ||
                            dragListener != null && (pos - startPos).len() > MIN_DRAG_DISTANCE)) {
                // Start dragging only when touch has been dragged for a minimum distance
                if (cardDragListener == null) {
                    cardDragListener = dragListener!!.onCardDragged(event.listenerActor as CardActor)
                }
                cardDragListener?.touchDragged(pos)
            }
        }

        override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
            if (cardDragListener != null && pointer == Input.Buttons.LEFT) {
                cardDragListener?.touchUp(Vector2(event.stageX, event.stageY))
                cardDragListener = null
            }
        }
    }

    override fun setStage(stage: Stage?) {
        if (stage != null) {
            if (stage is CardGameScreen) {
                stage.animationLayer.containers += this
            }
        } else if (super.getStage() != null) {
            val prevStage = super.getStage()
            if (prevStage is CardGameScreen) {
                prevStage.animationLayer.containers -= this
            }
        }
        super.setStage(stage)
    }

    override fun childrenChanged() {
        // Overriden to prevent invalidation.
    }

    override fun invalidate() {
        super.invalidate()
        sizeInvalid = true
    }

    override fun layout() {
        // Clear and re-add all actors to have the correct Z-index
        clearChildren()
        for (actor in actors) {
            actor.size = cardSize
            if (visibility == Visibility.ALL) {
                actor.shown = true
            } else if (visibility == Visibility.NONE) {
                actor.shown = false
            }
            addActor(actor)
        }

        val positions = computeActorsPosition()
        for (i in actors.indices) {
            val pos = positions[i]
            actors[i].setPosition(pos.x, pos.y)
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

    fun addClickListener(listener: ClickListener) {
        if (clickListeners.isEmpty()) {
            for (actor in actors) {
                actor.clickListeners += internalClickListener
            }
        }
        clickListeners += listener
    }

    fun removeClickListener(listener: ClickListener) {
        clickListeners -= listener
        if (clickListeners.isEmpty()) {
            for (actor in actors) {
                actor.clickListeners -= internalClickListener
            }
        }
    }

    fun addLongClickListener(listener: LongClickListener) {
        if (longClickListeners.isEmpty()) {
            for (actor in actors) {
                actor.longClickListeners += internalLongClickListener
            }
        }
        longClickListeners += listener
    }

    fun removeLongClickListener(listener: LongClickListener) {
        longClickListeners -= listener
        if (longClickListeners.isEmpty()) {
            for (actor in actors) {
                actor.longClickListeners -= internalLongClickListener
            }
        }
    }

    fun setDragListener(listener: DragListener?) {
        if (dragListener != null && listener == null) {
            for (actor in actors) {
                actor.removeListener(internalInputListener)
            }
        } else if (dragListener == null && listener != null) {
            for (actor in actors) {
                actor.addListener(internalInputListener)
            }
        }
        dragListener = listener
    }

    fun setPlayListener(listener: PlayListener?) {
        playListener = listener
    }

    /**
     * Change the cards in this container to new cards.
     * To update from this change, [invalidate] must be called.
     */
    open fun setCards(newCards: List<Card>) {
        cards.clear()
        cards += newCards

        // Bind existing actors to new cards
        for (i in actors.indices) {
            val actor = actors[i]
            actor.card = cards[i]
            actor.highlighted = false
        }

        // Remove actors if there are too many.
        while (actors.size > size) {
            val actor = actors.removeAt(actors.lastIndex)
            if (clickListeners.isNotEmpty()) actor.clickListeners -= internalClickListener
            if (longClickListeners.isNotEmpty()) actor.longClickListeners -= internalLongClickListener
            if (dragListener != null) actor.listeners.removeValue(internalInputListener, true)
        }

        // Add new actors if there aren't enough.
        while (actors.size < size) {
            val actor = CardActor(cardLoader, cards[actors.size])
            if (clickListeners.isNotEmpty()) actor.clickListeners += internalClickListener
            if (longClickListeners.isNotEmpty()) actor.longClickListeners += internalLongClickListener
            if (dragListener != null) actor.listeners.add(internalInputListener)
            actors.add(actor)
        }
    }

    /** Clones and returns the list of the cards in this container. */
    fun getCards(): Deck<out Card> = cards.clone()

    /** Returns the card at an [index]. */
    fun getCardAt(index: Int) = cards[index]

    /** Returns the card actor at an [index]. */
    fun getCardActorAt(index: Int) = actors[index]

    /** Returns the index of a card actor in the container, `-1` if not found. */
    fun findIndexOfCardActor(actor: CardActor) = actors.indexOf(actor)

    /** Apply an [action] on all card actors. */
    fun applyOnAllCards(action: (CardActor) -> Unit) {
        for (actor in actors) {
            action(actor)
        }
    }

    /** Apply an [action] on the actors of a list of [cards]. */
    fun applyOnCards(vararg cards: Card, action: (CardActor) -> Unit) {
        for (card in cards) {
            for (actor in actors) {
                if (actor.card == card) {
                    action(actor)
                    break
                }
            }
        }
    }

    /**
     * Show or hide the container by fading it.
     * @param visible New visibility.
     */
    fun fade(visible: Boolean) {
        isVisible = true

        var startOpacity = 0f
        var endOpacity = 1f
        if (!visible) {
            startOpacity = 1f
            endOpacity = 0f
        }
        setColor(1f, 1f, 1f, startOpacity)

        addAction(object : Action() {
            private var elapsed = 0f
            override fun act(delta: Float): Boolean {
                elapsed += delta
                val progress = Interpolation.smooth.applyBounded(elapsed / TRANSITION_DURATION)
                setColor(1f, 1f, 1f, startOpacity + (endOpacity - startOpacity) * progress)

                val done = elapsed >= TRANSITION_DURATION
                if (!visible && done) {
                    isVisible = false
                }
                return done
            }
        })
    }

    /**
     * Show or hide the container by sliding it to or from a [side].
     * @param visible New visibility.
     */
    fun slide(visible: Boolean, side: Side) {
        isVisible = true

        var endX = x
        var endY = y
        var startX = endX
        var startY = endY
        when (side) {
            Side.LEFT -> startX -= width
            Side.RIGHT -> startX += width
            Side.TOP -> startY += height
            Side.BOTTOM -> startY -= height
        }
        if (!visible) {
            val tempX = endX
            val tempY = endY
            endX = startX
            endY = startY
            startX = tempX
            startY = tempY
        }
        setPosition(startX, startY)

        addAction(object : Action() {
            private var elapsed = 0f

            override fun act(delta: Float): Boolean {
                elapsed += delta
                val progress = Interpolation.smooth.applyBounded(elapsed / TRANSITION_DURATION)
                setPosition(startX + (endX - startX) * progress,
                        startY + (endY - startY) * progress)

                val done = elapsed >= TRANSITION_DURATION
                if (!visible && done) {
                    isVisible = false
                    setPosition(startX, startY)
                }
                return done
            }
        })
    }

    internal open fun onAnimationStart() {
        clearActions()
        setColor(1f, 1f, 1f, 1f)

        // Reset old actors
        for (actor in oldActors!!) {
            actor.clickListeners.clear()
            actor.longClickListeners.clear()
            actor.listeners.removeValue(internalInputListener, true)
            actor.enabled = true
            actor.highlighted = false
            actor.highlightable = true
        }

        // Apply this container visibility to the new actors
        for (actor in actors) {
            if (visibility == Visibility.ALL) {
                actor.shown = true
            } else if (visibility == Visibility.NONE) {
                actor.shown = false
            }
        }
    }

    internal open fun onAnimationEnd() {
        for (actor in actors) {
            if (clickListeners.isNotEmpty()) actor.clickListeners += internalClickListener
            if (longClickListeners.isNotEmpty()) actor.longClickListeners += internalLongClickListener
            actor.listeners.add(internalInputListener)
        }
    }

    /**
     * Returns an array of the positions of the actors in this container.
     * The array is indexed like the cards and actors list.
     */
    internal abstract fun computeActorsPosition(): Array<Vector2>

    /**
     * Compute the minimum size this container can be.
     */
    internal open fun computeSize() {
        if (!sizeInvalid) return
        sizeInvalid = false

        val width = cardLoader.getCardWidth()
        val height = cardLoader.getCardHeight()
        cardScale = cardSize / width
        cardWidth = width * cardScale
        cardHeight = height * cardScale
    }

    /**
     * Returns a vector of the offset needed to respect
     * the alignment and the padding, given a required size.
     */
    protected fun computeAlignmentOffset(requiredWidth: Float, requiredHeight: Float): Vector2 {
        val offsetX = when {
            Align.isCenterHorizontal(alignment) || alignment == Align.center -> (width - requiredWidth) / 2
            Align.isLeft(alignment) -> 0f
            Align.isRight(alignment) -> width - requiredWidth
            else -> 0f
        }
        val offsetY = when {
            Align.isCenterVertical(alignment) || alignment == Align.center -> (height - requiredHeight) / 2
            Align.isTop(alignment) -> height - requiredHeight
            Align.isBottom(alignment) -> 0f
            else -> 0f
        }

        return Vector2(offsetX, offsetY)
    }

    override fun toString() = "[cards: $cards, visibility: ${visibility.toString().toLowerCase()}}]"

    interface ClickListener {
        /**
         * Called when a card is clicked.
         * @param index index of card in container.
         */
        fun onCardClicked(actor: CardActor, index: Int)
    }

    interface LongClickListener {
        /**
         * Called when a card is long clicked.
         * @param index index of card in container.
         */
        fun onCardLongClicked(actor: CardActor, index: Int)
    }

    interface DragListener {
        /**
         * Called when a card in this container is dragged.
         * Can return an input listener provided by [AnimationLayer.dragCards] to
         * drag the card, or can return `null` to not drag the card.
         */
        fun onCardDragged(actor: CardActor): AnimationLayer.CardDragListener?
    }

    interface PlayListener {
        /**
         * When a card is dragged over this container, returns whether or not it can be played in it.
         */
        fun canCardsBePlayed(actors: Array<CardActor>, src: CardContainer): Boolean

        /**
         * Called when cards from another container are dragged to this container at
         * a position in the container coordinates, and [canCardsBePlayed] returned true.
         * The animation layer is updated automatically afterwards.
         */
        fun onCardsPlayed(actors: Array<CardActor>, src: CardContainer, pos: Vector2)
    }

    enum class Visibility {
        /** Automatically show all cards. */
        ALL,
        /** Automatically hide all cards. */
        NONE,
        /** Must manually set cards shown state. */
        MIXED
    }

    enum class Side {
        LEFT, RIGHT, TOP, BOTTOM
    }

    companion object {
        private const val TRANSITION_DURATION = 0.5f
        private const val MIN_DRAG_DISTANCE = 10f
    }

}