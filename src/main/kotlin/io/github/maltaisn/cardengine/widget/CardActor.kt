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

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import io.github.maltaisn.cardengine.*
import io.github.maltaisn.cardengine.core.Card


/**
 * An actor that draws a card.
 * @property cardLoader card sprite loader for loading the card texture.
 */
class CardActor(private val cardLoader: CardSpriteLoader, var card: Card) : Actor() {

    /**
     * Whether the card value is displayed.
     * If not shown, the back of the card is displayed instead.
     */
    var shown = true

    /**
     * Whether the card can be selected and hovered.
     * Usually a card is enabled when it can be played.
     */
    var enabled = true
        set(value) {
            field = value
            if (!value) {
                selected = false
                hovered = false
                selectionAlpha = 0f
                hoverAlpha = 0f
            }
        }

    /**
     * Whether the card is highlighted or not.
     * This is a flag used for [CardHand].
     */
    var highlighted = false
        internal set

    /**
     * Whether this card can be highlighted by clicking on it.
     * This is a flag used for [CardHand].
     */
    var highlightable = true


    /** The card's width, in pixels. */
    var size = CARD_SIZE_NORMAL
        set(value) {
            field = value
            updateSize()
        }

    /**
     * Internal flag used by the animation layer to indicate when a card is being animated.
     * An animated card doesn't fire click and long click events.
     */
    internal var animated = false

    // Used when animating to set the source and destination containers on a moved card.
    internal var src: CardContainer? = null
    internal var dst: CardContainer? = null

    /**
     * Click listeners, called when the actor is clicked. Clicks must end within the bounds.
     * The listeners are not called when the actor is disabled or animated.
     */
    internal val clickListeners = ArrayList<ClickListener>()

    /**
     * Click listeners, called when the actor is long clicked.
     * The listeners are not called when the actor is disabled or animated.
     */
    internal val longClickListeners = ArrayList<LongClickListener>()
    private var lastTouchDownTime = 0L
    private var longClicked = false

    // Used for hover and selection interpolation.
    private var selected = false
    private var hovered = false
    private var selectionElapsed = 0f
    private var hoverElapsed = 0f
    private var selectionAlpha = 0f
    private var hoverAlpha = 0f


    init {
        updateSize()

        addListener(object : InputListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                if (enabled) {
                    selected = true
                    selectionElapsed = 0f
                    lastTouchDownTime = System.currentTimeMillis()
                    longClicked = false
                }
                return true
            }

            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                if (enabled) {
                    selected = false
                    selectionElapsed = Animation.SELECTION_FADE_DURATION * selectionAlpha
                    lastTouchDownTime = 0

                    if (!animated && !longClicked && withinBounds(x, y)) {
                        // Click ended in actor, call listeners.
                        // The list is copied so changes to it don't lead to concurrent modification errors.
                        for (listener in ArrayList(clickListeners)) {
                            listener.onCardActorClicked(this@CardActor)
                        }
                    }
                }
            }

            override fun enter(event: InputEvent, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
                // Pointer must be -1 because hovering can only happen on desktop.
                // Also when on touch down/up, an enter/exit event is fired, but that shouldn't stop hovering.
                if (enabled && pointer == -1) {
                    hovered = true
                    hoverElapsed = 0f
                }
            }

            override fun exit(event: InputEvent, x: Float, y: Float, pointer: Int, toActor: Actor?) {
                if (enabled && pointer == -1) {
                    hovered = false
                    hoverElapsed = Animation.HOVER_FADE_DURATION * hoverAlpha
                }
            }
        })
    }

    override fun act(delta: Float) {
        super.act(delta)

        // Trigger long click if held long enough
        val heldDuration = (System.currentTimeMillis() - lastTouchDownTime) / 1000f
        if (longClickListeners.isNotEmpty() && lastTouchDownTime != 0L
                && heldDuration > Animation.LONG_CLICK_DELAY && enabled && !animated) {
            longClicked = true
            for (listener in ArrayList(longClickListeners)) {
                listener.onCardActorLongClicked(this@CardActor)
            }
        }

        // Update selection alpha
        if (selected && selectionElapsed < Animation.SELECTION_FADE_DURATION) {
            selectionElapsed += delta
            selectionAlpha = Animation.SELECTION_IN_INTERPOLATION.applyBounded(selectionElapsed / Animation.SELECTION_FADE_DURATION)
        } else if (!selected && selectionElapsed > 0f) {
            selectionElapsed -= delta
            selectionAlpha = Animation.SELECTION_OUT_INTERPOLATION.applyBounded(selectionElapsed / Animation.SELECTION_FADE_DURATION)
        }

        // Update hover alpha
        if (hovered && hoverElapsed < Animation.SELECTION_FADE_DURATION) {
            hoverElapsed += delta
            hoverAlpha = Animation.HOVER_IN_INTERPOLATION.applyBounded(hoverElapsed / Animation.HOVER_FADE_DURATION)
        } else if (!hovered && hoverElapsed > 0f) {
            hoverElapsed -= delta
            hoverAlpha = Animation.HOVER_OUT_INTERPOLATION.applyBounded(hoverElapsed / Animation.HOVER_FADE_DURATION)
        }
    }


    override fun draw(batch: Batch, parentAlpha: Float) {
        // Get the card sprite and scale
        val cardSprite = if (shown) {
            cardLoader.getSprite(card.value)
        } else {
            cardLoader.getSprite(CardSpriteLoader.BACK)
        }
        val scale = size / cardSprite.width

        // Draw shadow
        drawSprite(batch, cardLoader.getSprite(CardSpriteLoader.SHADOW),
                scale, cardLoader.shadowOffset, parentAlpha)

        // Draw card
        drawSprite(batch, cardSprite, scale, 0f, parentAlpha)

        // Draw shadow
        if (hoverAlpha != 0f) {
            drawSprite(batch, cardLoader.getSprite(CardSpriteLoader.HOVER),
                    scale, cardLoader.hoverOffset, hoverAlpha * parentAlpha)
        }

        // Draw selection
        if (selectionAlpha != 0f) {
            drawSprite(batch, cardLoader.getSprite(CardSpriteLoader.SELECTION),
                    scale, 0f, selectionAlpha * parentAlpha)
        }
    }

    private fun updateSize() {
        val width = cardLoader.getCardWidth()
        val height = cardLoader.getCardHeight()
        val scale = size / width
        setSize(width * scale, height * scale)
    }

    override fun toString() = "[card: $card, ${if (shown) "shown" else "hidden"}]"


    interface ClickListener {
        fun onCardActorClicked(actor: CardActor)
    }

    interface LongClickListener {
        fun onCardActorLongClicked(actor: CardActor)
    }

    companion object {
        // Card sizes presets
        const val CARD_SIZE_TINY = 80f
        const val CARD_SIZE_SMALL = 100f
        const val CARD_SIZE_NORMAL = 120f
        const val CARD_SIZE_BIG = 150f
        const val CARD_SIZE_HUGE = 200f
    }

}