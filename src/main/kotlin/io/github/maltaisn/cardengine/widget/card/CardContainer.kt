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

package io.github.maltaisn.cardengine.widget.card

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Action
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup
import com.badlogic.gdx.utils.Align
import io.github.maltaisn.cardengine.Animation
import io.github.maltaisn.cardengine.CardGameScreen
import io.github.maltaisn.cardengine.applyBounded
import io.github.maltaisn.cardengine.core.Card
import io.github.maltaisn.cardengine.widget.GameLayer
import ktx.actors.alpha
import ktx.actors.plusAssign
import ktx.collections.isNotEmpty
import ktx.math.minus
import ktx.math.vec2
import java.util.*
import kotlin.math.min


/**
 * The base class for a widget group that contains card actors.
 * All card containers support animations with the [CardAnimationLayer],
 * however the container must be in a [CardGameScreen] stage to support animations.
 */
abstract class CardContainer(val coreStyle: GameLayer.CoreStyle,
                             val cardStyle: CardActor.CardStyle) : WidgetGroup() {

    private val _actors = mutableListOf<CardActor?>()

    /** The actors for the cards. When an actor is moved, this list is immediately updated. */
    val actors: List<CardActor?>
        get() = _actors

    /** The cards in this container. When a card is moved, this list is immediately updated. */
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

    /** Whether the container is shown or not. Like [isVisible] but with the correct value during a transition. */
    var shown = true

    /**
     * Whether this card container is enabled. If disabled, card actors will be too,
     * drag listener and play listener will be disabled.
     */
    var enabled = true
        set(value) {
            field = value
            for (actor in actors) {
                actor?.enabled = value
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
    var alignment = Align.center

    /** Listener called when a card is clicked, or `null` for none. */
    private val clickListeners = mutableListOf<ClickListener>()

    /** Listener called when a card is long clicked, or `null` for none. */
    private val longClickListeners = mutableListOf<LongClickListener>()

    /** Listener called when a card is dragged, or `null` for if not draggable. */
    private var dragListener: DragListener? = null

    /** Listener called when a card is played on this container, or `null` if not playable. */
    internal var playListener: PlayListener? = null

    // Size properties
    protected var sizeInvalid = true
    protected var computedWidth = 0f
    protected var computedHeight = 0f
    protected var cardWidth = 0f
    protected var cardHeight = 0f
    protected var cardScale = 0f

    private var renderToFrameBuffer = false


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
        private var cardDragger: CardAnimationLayer.CardDragger? = null
        private var startPos = vec2()

        override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
            startPos = vec2(event.stageX, event.stageY)
            return enabled && dragListener != null && pointer == Input.Buttons.LEFT
        }

        override fun touchDragged(event: InputEvent, x: Float, y: Float, pointer: Int) {
            val pos = vec2(event.stageX, event.stageY)
            if (enabled && pointer == Input.Buttons.LEFT && (cardDragger != null ||
                            dragListener != null && (pos - startPos).len() > Animation.MIN_DRAG_DISTANCE)) {
                // Start dragging only when touch has been dragged for a minimum distance
                if (cardDragger == null) {
                    cardDragger = dragListener!!.onCardDragged(event.listenerActor as CardActor)
                }
                cardDragger?.touchDragged(pos)
            }
        }

        override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
            if (enabled && cardDragger != null && pointer == Input.Buttons.LEFT) {
                cardDragger?.touchUp(vec2(event.stageX, event.stageY))
                cardDragger = null
            }
        }
    }


    constructor(coreSkin: Skin, cardSkin: Skin) :
            this(coreSkin.get(GameLayer.CoreStyle::class.java),
                    cardSkin.get(CardActor.CardStyle::class.java))

    override fun setStage(stage: Stage?) {
        require(stage == null || stage is CardGameScreen) {
            "CardContainer must be added to a CardGameScreen stage."
        }

        if (stage != null) {
            (stage as CardGameScreen).cardAnimationLayer.containers += this
        } else if (super.getStage() != null) {
            (super.getStage() as CardGameScreen).cardAnimationLayer.containers -= this
        }
        super.setStage(stage)
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        if (renderToFrameBuffer) {
            val stage = stage as CardGameScreen
            val fbo = stage.offscreenFbo

            // Change blending function to avoid blending twice: when drawn to FBO and when FBO is drawn to screen
            // https://gist.github.com/mattdesl/4393861
            batch.enableBlending()
            batch.setBlendFunctionSeparate(GL20.GL_SRC_ALPHA,
                    GL20.GL_ONE_MINUS_SRC_ALPHA, GL20.GL_ONE, GL20.GL_ONE)

            fbo.begin()

            // Clear the frame buffer
            Gdx.gl.glClearColor(0f, 0f, 0f, 0f)
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

            // Draw the table content
            super.draw(batch, parentAlpha)

            fbo.end()

            // Draw the frame buffer to the screen batch
            val oldColor = batch.color.cpy()
            val a = alpha * parentAlpha
            batch.setColor(a, a, a, a)
            batch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA)  // Premultiplied alpha blending mode
            batch.draw(stage.offscreenFboRegion, 0f, 0f, stage.width, stage.height)
            batch.color = oldColor
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)

        } else {
            super.draw(batch, parentAlpha)
        }
    }

    override fun childrenChanged() {
        // Overriden to prevent invalidation.
    }

    override fun invalidate() {
        super.invalidate()
        sizeInvalid = true
    }

    /**
     * Re-add all actors to the container, with the correct Z-index
     */
    protected fun update() {
        clearChildren()
        for (actor in actors) {
            if (actor != null) {
                actor.size = cardSize
                if (visibility == Visibility.ALL) {
                    actor.shown = true
                } else if (visibility == Visibility.NONE) {
                    actor.shown = false
                }
                this += actor
            }
        }
    }

    ////////// LISTENERS //////////
    fun addClickListener(listener: ClickListener) {
        if (clickListeners.isEmpty()) {
            for (actor in actors) {
                actor?.clickListeners?.add(internalClickListener)
            }
        }
        clickListeners += listener
    }

    fun removeClickListener(listener: ClickListener) {
        clickListeners -= listener
        if (clickListeners.isEmpty()) {
            for (actor in actors) {
                actor?.clickListeners?.remove(internalClickListener)
            }
        }
    }

    fun addLongClickListener(listener: LongClickListener) {
        if (longClickListeners.isEmpty()) {
            for (actor in actors) {
                actor?.longClickListeners?.add(internalLongClickListener)
            }
        }
        longClickListeners += listener
    }

    fun removeLongClickListener(listener: LongClickListener) {
        longClickListeners -= listener
        if (longClickListeners.isEmpty()) {
            for (actor in actors) {
                actor?.longClickListeners?.remove(internalLongClickListener)
            }
        }
    }

    fun setDragListener(listener: DragListener?) {
        if (dragListener != null && listener == null) {
            for (actor in actors) {
                actor?.removeListener(internalInputListener)
            }
        } else if (dragListener == null && listener != null) {
            for (actor in actors) {
                actor?.addListener(internalInputListener)
            }
        }
        dragListener = listener
    }

    fun setPlayListener(listener: PlayListener?) {
        playListener = listener
    }

    interface ClickListener {
        /**
         * Called when a card is clicked. Not called if container is disabled.
         * @param index index of card in container.
         */
        fun onCardClicked(actor: CardActor, index: Int)
    }

    interface LongClickListener {
        /**
         * Called when a card is long clicked. Not called if container is disabled.
         * @param index index of card in container.
         */
        fun onCardLongClicked(actor: CardActor, index: Int)
    }

    interface DragListener {
        /**
         * Called when a card in this container is dragged.
         * Can return an input listener provided by [CardAnimationLayer.dragCards] to
         * drag the card, or can return `null` to not drag the card.
         * Not called if container is disabled.
         */
        fun onCardDragged(actor: CardActor): CardAnimationLayer.CardDragger?
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
         * The animation layer is updated automatically afterwards.
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
                actor.enabled = enabled
                actor.highlightable = true
                actor.highlighted = false
            } else {
                _actors.add(null)
            }
        }

        // Remove listeners from unused old actors.
        for (actor in oldActors) {
            if (clickListeners.isNotEmpty()) actor.clickListeners -= internalClickListener
            if (longClickListeners.isNotEmpty()) actor.longClickListeners -= internalLongClickListener
            if (dragListener != null) actor.listeners.removeValue(internalInputListener, true)
        }

        // Add new actors if there aren't enough.
        while (actors.size < newCards.size) {
            val card = newCards[actors.size]
            if (card != null) {
                val actor = CardActor(coreStyle, cardStyle, card)
                actor.enabled = enabled
                if (clickListeners.isNotEmpty()) actor.clickListeners += internalClickListener
                if (longClickListeners.isNotEmpty()) actor.longClickListeners += internalLongClickListener
                if (dragListener != null) actor.listeners.add(internalInputListener)
                _actors += actor
            } else {
                _actors.add(null)
            }
        }

        update()
    }

    protected fun sortWith(comparator: Comparator<CardActor?>) {
        _actors.sortWith(comparator)
    }

    /** Apply an [action] on all card actors. */
    fun applyOnAllCards(action: (CardActor) -> Unit) {
        for (actor in actors) {
            if (actor != null) {
                action(actor)
            }
        }
    }

    /**
     * Apply an [action] on the actors of a list of [cards].
     * If multiple card actors have the same card, the first one found is used.
     */
    fun applyOnCards(vararg cards: Card, action: (CardActor) -> Unit) {
        for (card in cards) {
            for (actor in actors) {
                if (actor != null && actor.card == card) {
                    action(actor)
                    break
                }
            }
        }
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

        return vec2(offsetX, offsetY)
    }

    override fun toString() = "[cards: $cards, visibility: ${visibility.toString().toLowerCase()}}]"


    ////////// TRANSITIONS //////////
    /**
     * Animate a visibility change by fading in or out.
     * @param shown New visibility.
     */
    fun fade(shown: Boolean) {
        if (this.shown == shown) return

        completeSlideTransition()

        this.shown = shown
        if (actions.isEmpty) {
            this += FadeTransitionAction()
        }
    }

    /**
     * Animate a visibility change by sliding in or out in a [direction].
     * @param shown New visibility.
     */
    fun slide(shown: Boolean, direction: Direction) {
        if (this.shown == shown) return

        completeSlideTransition()
        clearActions()

        this.shown = shown
        this += SlideTransitionAction(direction)
    }

    private inner class FadeTransitionAction : Action() {
        private var elapsed = if (shown) 0f else Animation.CONTAINER_TRANSITION_DURATION

        init {
            isVisible = true
            renderToFrameBuffer = true
            alpha = if (shown) 1f else 0f
        }

        override fun act(delta: Float): Boolean {
            elapsed += if (shown) delta else -delta
            val progress = Animation.CONTAINER_TRANSITION_INTERPOLATION.applyBounded(
                    elapsed / Animation.CONTAINER_TRANSITION_DURATION)
            alpha = progress

            if (shown && progress >= 1 || !shown && progress <= 0) {
                isVisible = shown
                renderToFrameBuffer = false
                return true
            }
            return false
        }
    }

    private fun completeSlideTransition() {
        // Complete previous slide transition if there was one.
        val action = actions.firstOrNull()
        if (action is SlideTransitionAction) {
            action.complete()
            clearActions()
        }
    }

    private inner class SlideTransitionAction(direction: Direction) : Action() {
        private var elapsed = 0f
        private var completed = false

        private var startX: Float = x
        private var startY: Float = y
        private var endX: Float = x
        private var endY: Float = y

        init {
            isVisible = true
            renderToFrameBuffer = false
            alpha = 1f

            when (direction) {
                Direction.LEFT -> startX -= width
                Direction.RIGHT -> startX += width
                Direction.UP -> startY += height
                Direction.DOWN -> startY -= height
            }
            if (!shown) {
                val tempX = endX
                val tempY = endY
                endX = startX
                endY = startY
                startX = tempX
                startY = tempY
            }
            setPosition(startX, startY)
        }

        override fun act(delta: Float): Boolean {
            if (completed) return true

            elapsed += delta
            val progress = Animation.CONTAINER_TRANSITION_INTERPOLATION
                    .applyBounded(elapsed / Animation.CONTAINER_TRANSITION_DURATION)
            setPosition(startX + (endX - startX) * progress, startY + (endY - startY) * progress)

            if (elapsed >= Animation.CONTAINER_TRANSITION_DURATION) {
                complete()
                return true
            }
            return false
        }

        fun complete() {
            if (shown) {
                setPosition(endX, endY)
            } else {
                isVisible = false
                setPosition(startX, startY)
            }
            completed = true
        }
    }

    enum class Direction {
        LEFT, RIGHT, UP, DOWN
    }

    ////////// ANIMATION //////////
    internal fun moveCardTo(dst: CardContainer, srcIndex: Int, dstIndex: Int,
                            replaceSrc: Boolean = false, replaceDst: Boolean = false) {
        if (oldActors == null) {
            oldActors = actors.toMutableList()
        }
        if (dst.oldActors == null) {
            dst.oldActors = dst.actors.toMutableList()
        }

        // Move card and actor
        val actor = _actors.removeAt(srcIndex)
        if (replaceSrc) {
            _actors.add(srcIndex, null)
        }
        if (replaceDst) {
            val replaced = dst.actors[dstIndex]
            require(replaced == null) {
                "Card must replaced a null card in destination, found '$replaced' instead."
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
        clearActions()
        alpha = 1f

        // Reset old actors
        for (actor in oldActors!!) {
            actor?.apply {
                clickListeners.clear()
                longClickListeners.clear()
                listeners.removeValue(internalInputListener, true)
                enabled = true
                highlighted = false
                highlightable = true
            }
        }

        // Apply this container visibility to the new actors
        for (actor in actors) {
            if (visibility == Visibility.ALL) {
                actor?.shown = true
            } else if (visibility == Visibility.NONE) {
                actor?.shown = false
            }
        }
    }

    internal open fun onAnimationEnd() {
        for (actor in actors) {
            if (actor != null) {
                if (clickListeners.isNotEmpty()) actor.clickListeners += internalClickListener
                if (longClickListeners.isNotEmpty()) actor.longClickListeners += internalLongClickListener
                if (dragListener != null) actor.listeners.add(internalInputListener)
            }
        }
    }

}