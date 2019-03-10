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

package io.github.maltaisn.cardgame.widget.card

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Widget
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.scenes.scene2d.utils.TransformDrawable
import io.github.maltaisn.cardgame.applyBounded
import io.github.maltaisn.cardgame.core.Card
import io.github.maltaisn.cardgame.widget.GameLayer
import io.github.maltaisn.cardgame.withinBounds


/**
 * An actor that draws a card.
 * @property coreStyle Core game style.
 * @property cardStyle Card actor style, must match card type.
 * @property card Card shown by the actor.
 */
class CardActor(val coreStyle: GameLayer.CoreStyle, val cardStyle: CardStyle, var card: Card) : Widget() {

    /**
     * Whether the card value is displayed.
     * If not shown, the back of the card is displayed instead.
     */
    var shown = true

    /**
     * Whether the card can be selected, hovered and clicked.
     * Usually a card is enabled when it's involved in a valid move.
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
    var size = SIZE_NORMAL
        set(value) {
            field = value
            setSize(prefWidth, prefHeight)
        }

    /**
     * Click listener, called when the actor is clicked. Clicks must end within the bounds.
     * The listener is not called when the actor is disabled or animated.
     */
    var clickListener: ClickListener? = null

    /**
     * Click listener, called when the actor is long clicked.
     * The listener is not called when the actor is disabled or animated.
     */
    var longClickListener: LongClickListener? = null
    private var lastTouchDownTime = 0L
    private var longClicked = false

    // Hover and selection status.
    private var selected = false
    private var hovered = false
    private var selectionElapsed = 0f
    private var hoverElapsed = 0f
    private var selectionAlpha = 0f
    private var hoverAlpha = 0f

    private val tempColor = Color()

    /**
     * Internal flag used by the animation layer to indicate when a card is being animated.
     * An animated card doesn't fire click and long click events.
     */
    internal var animated = false

    // Used when animating to set the source and destination containers on a moved card.
    internal var src: CardContainer? = null
    internal var dst: CardContainer? = null


    constructor(skin: Skin, card: Card,
                coreStyleName: String = "default",
                cardStyleName: String = "default") :
            this(skin.get(coreStyleName, GameLayer.CoreStyle::class.java),
                    skin.get(cardStyleName, CardStyle::class.java), card)


    init {
        setSize(prefWidth, prefHeight)

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
                    selectionElapsed = SELECTION_FADE_DURATION * selectionAlpha
                    lastTouchDownTime = 0

                    if (clickListener != null && !animated && !longClicked && withinBounds(x, y)) {
                        // Click ended in actor, call listener.
                        clickListener?.onCardActorClicked(this@CardActor)
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
                    hoverElapsed = HOVER_FADE_DURATION * hoverAlpha
                }
            }
        })
    }

    override fun act(delta: Float) {
        super.act(delta)

        // Trigger long click if held long enough
        val heldDuration = (System.currentTimeMillis() - lastTouchDownTime) / 1000f
        if (longClickListener != null && lastTouchDownTime != 0L
                && heldDuration > LONG_CLICK_DELAY && enabled && !animated) {
            longClicked = true
            longClickListener?.onCardActorLongClicked(this@CardActor)
        }

        var renderingNeeded = false

        // Update selection alpha
        if (selected && selectionElapsed < SELECTION_FADE_DURATION) {
            selectionElapsed += delta
            selectionAlpha = SELECTION_IN_INTERPOLATION.applyBounded(selectionElapsed / SELECTION_FADE_DURATION)
            renderingNeeded = true
        } else if (!selected && selectionElapsed > 0f) {
            selectionElapsed -= delta
            selectionAlpha = SELECTION_OUT_INTERPOLATION.applyBounded(selectionElapsed / SELECTION_FADE_DURATION)
            renderingNeeded = true
        }

        // Update hover alpha
        if (hovered && hoverElapsed < HOVER_FADE_DURATION) {
            hoverElapsed += delta
            hoverAlpha = HOVER_IN_INTERPOLATION.applyBounded(hoverElapsed / HOVER_FADE_DURATION)
            renderingNeeded = true
        } else if (!hovered && hoverElapsed > 0f) {
            hoverElapsed -= delta
            hoverAlpha = HOVER_OUT_INTERPOLATION.applyBounded(hoverElapsed / HOVER_FADE_DURATION)
            renderingNeeded = true
        }

        if (renderingNeeded) {
            Gdx.graphics.requestRendering()
        }
    }


    override fun draw(batch: Batch, parentAlpha: Float) {
        tempColor.set(batch.color)
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha)

        // Draw background
        drawCenteredDrawable(batch, coreStyle.cardBackground as TransformDrawable)

        // Draw card
        val scale = size / cardStyle.cardWidth
        val card = (if (shown) cardStyle.cards[card.value] else cardStyle.back) as TransformDrawable
        card.draw(batch, x + (width - card.minWidth * scale) / 2,
                y + (height - card.minHeight * scale) / 2, 0f, 0f,
                card.minWidth, card.minHeight, scale, scale, 0f)

        // Draw hover
        if (hoverAlpha != 0f) {
            batch.setColor(color.r, color.g, color.b, color.a * parentAlpha * hoverAlpha)
            drawCenteredDrawable(batch, coreStyle.cardHover as TransformDrawable)
        }

        // Draw selection
        if (selectionAlpha != 0f) {
            batch.setColor(color.r, color.g, color.b, color.a * parentAlpha * selectionAlpha)
            drawCenteredDrawable(batch, coreStyle.cardSelection as TransformDrawable)
        }

        batch.setColor(tempColor.r, tempColor.g, tempColor.b, tempColor.a)
    }

    /** Draw a drawable to fit around the actor when considering padding. */
    private fun drawCenteredDrawable(batch: Batch, drawable: TransformDrawable) {
        val scale = size / cardStyle.cardWidth
        drawable.draw(batch, x - drawable.leftWidth * scale,
                y - drawable.bottomHeight * scale, 0f, 0f,
                width / scale + drawable.leftWidth + drawable.rightWidth,
                height / scale + drawable.bottomHeight + drawable.topHeight,
                scale, scale, 0f)
    }

    override fun getPrefWidth() = size

    override fun getPrefHeight() = size / cardStyle.cardWidth * cardStyle.cardHeight

    override fun toString() = "[card: $card, ${if (shown) "shown" else "hidden"}]"

    interface ClickListener {
        fun onCardActorClicked(actor: CardActor)
    }

    interface LongClickListener {
        fun onCardActorLongClicked(actor: CardActor)
    }

    /**
     * The style for cards drawn with a [CardActor], must match the [card] type.
     */
    class CardStyle {
        /** Array of card drawables indexed by card value. */
        lateinit var cards: Array<Drawable>
        /** Drawable for the back face of a card. */
        lateinit var back: Drawable

        /** Width of a card, excluding shadow. */
        var cardWidth = 0f
        /** Height of a card, excluding shadow */
        var cardHeight = 0f
    }

    companion object {
        // Card sizes presets
        const val SIZE_TINY = 80f
        const val SIZE_SMALL = 100f
        const val SIZE_NORMAL = 120f
        const val SIZE_BIG = 150f
        const val SIZE_HUGE = 200f

        /** The duration of the hover fade. */
        private const val HOVER_FADE_DURATION = 0.3f

        /** The duration of the selection fade. */
        private const val SELECTION_FADE_DURATION = 0.3f

        /** The delay before long click is triggered. */
        private const val LONG_CLICK_DELAY = 0.5f

        private val SELECTION_IN_INTERPOLATION: Interpolation = Interpolation.smooth
        private val SELECTION_OUT_INTERPOLATION: Interpolation = Interpolation.smooth
        private val HOVER_IN_INTERPOLATION: Interpolation = Interpolation.pow2Out
        private val HOVER_OUT_INTERPOLATION: Interpolation = Interpolation.smooth
    }

}