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

import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.scenes.scene2d.utils.TransformDrawable
import io.github.maltaisn.cardgame.core.Card
import io.github.maltaisn.cardgame.widget.GameLayer
import io.github.maltaisn.cardgame.widget.SelectableWidget
import io.github.maltaisn.cardgame.withinBounds
import ktx.actors.alpha
import ktx.style.get


/**
 * An actor that draws a card.
 * @property coreStyle Core game style.
 * @property cardStyle Card actor style, must match card type.
 * @property card Card shown by the actor.
 */
class CardActor(val coreStyle: GameLayer.CoreStyle,
                val cardStyle: CardStyle, var card: Card) : SelectableWidget() {

    /**
     * Whether the card value is displayed.
     * If not shown, the back of the card is displayed instead.
     */
    var shown = true

    /**
     * Whether the card is highlighted or not.
     * To update this state with animation, [CardContainer.requestUpdate] must be called before updating.
     */
    var highlighted = false

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
    var clickListener: ((CardActor) -> Unit)? = null

    /**
     * Click listener, called when the actor is long clicked.
     * The listener is not called when the actor is disabled or animated.
     */
    var longClickListener: ((CardActor) -> Unit)? = null
    private var lastTouchDownTime = 0L
    private var longClicked = false

    /**
     * Internal flag used by the animation layer to indicate when a card is being animated.
     * An animated card doesn't fire click and long click events.
     */
    internal var animated = false

    // Used when animating to set the source and destination containers on a moved card.
    internal var src: CardContainer? = null
    internal var dst: CardContainer? = null

    // Used internally by CardAnimationLayer
    internal var moveAction: CardAnimationLayer.MoveCardAction? = null
        set(value) {
            if (field != null) removeAction(field)
            field = value
            if (value != null) addAction(value)
        }

    // Used internally by CardHand
    internal var highlightAction: CardHand.HighlightAction? = null
        set(value) {
            if (field != null) removeAction(field)
            field = value
            if (value != null) addAction(value)
        }


    constructor(skin: Skin, card: Card,
                coreStyleName: String = "default",
                cardStyleName: String = "default") :
            this(skin[coreStyleName], skin[cardStyleName], card)


    init {
        addListener(SelectionListener())

        addListener(object : InputListener() {
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                if (enabled && button == Input.Buttons.LEFT) {
                    lastTouchDownTime = System.currentTimeMillis()
                    longClicked = false
                }
                return true
            }

            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                if (enabled && button == Input.Buttons.LEFT) {
                    lastTouchDownTime = 0

                    if (clickListener != null && !animated && !longClicked && withinBounds(x, y)) {
                        // Click ended in actor, call listener.
                        clickListener!!(this@CardActor)
                    }
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
            longClickListener!!(this@CardActor)
        }
    }


    override fun draw(batch: Batch, parentAlpha: Float) {
        batch.setColor(color.r, color.g, color.b, alpha * parentAlpha)

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
            batch.setColor(color.r, color.g, color.b, alpha * parentAlpha * hoverAlpha)
            drawCenteredDrawable(batch, coreStyle.cardHover as TransformDrawable)
        }

        // Draw press
        if (pressAlpha != 0f) {
            batch.setColor(color.r, color.g, color.b, alpha * parentAlpha * pressAlpha)
            drawCenteredDrawable(batch, coreStyle.cardSelection as TransformDrawable)
        }
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

    override fun clearActions() {
        super.clearActions()
        moveAction = null
        highlightAction = null
    }

    override fun getPrefWidth() = size

    override fun getPrefHeight() = size / cardStyle.cardWidth * cardStyle.cardHeight

    override fun toString() = "[card: $card, ${if (shown) "shown" else "hidden"}" +
            "${if (highlighted) ", highlighted" else ""}]"

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

        /** The delay before long click is triggered. */
        private const val LONG_CLICK_DELAY = 0.5f
    }

}