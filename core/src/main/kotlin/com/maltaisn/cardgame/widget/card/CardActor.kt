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
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.scenes.scene2d.utils.TransformDrawable
import com.maltaisn.cardgame.game.Card
import com.maltaisn.cardgame.utils.postDelayed
import com.maltaisn.cardgame.utils.withinBounds
import com.maltaisn.cardgame.widget.SelectableWidget
import com.maltaisn.cardgame.widget.action.ActionDelegate
import com.maltaisn.cardgame.widget.action.TimeAction
import ktx.actors.alpha
import ktx.math.vec2


/**
 * An actor that draws a card.* @property style Card actor style, must match card type.
 * @property card Card shown by the actor.
 */
class CardActor(private val style: CardStyle,
                var card: Card? = null) : SelectableWidget() {

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
            invalidateHierarchy()
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
    private var longClickAction by ActionDelegate<TimeAction>()

    /**
     * Internal flag used by the animation group to indicate when a card is being animated.
     * An animated card doesn't fire click and long click events.
     */
    internal var animated = false

    // Used when animating to set the source and destination containers on a moved card.
    internal var src: CardContainer? = null
    internal var dst: CardContainer? = null

    // Used internally by CardAnimationLayer
    internal var moveAction by ActionDelegate<CardAnimationGroup.MoveCardAction>()

    // Used internally by CardHand
    internal var highlightAction by ActionDelegate<CardHand.HighlightAction>()


    init {
        addListener(SelectionListener())

        addListener(object : InputListener() {
            private val lastPos = vec2()
            private var longClicked = false

            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                if (isTouchValid(x, y, button) && longClickListener != null) {
                    // Trigger long click after a delay if touch still valid.
                    lastPos.set(x, y)
                    longClicked = false
                    longClickAction = postDelayed(LONG_CLICK_DELAY) {
                        if (isTouchValid(lastPos.x, lastPos.y, button) && longClickListener != null) {
                            longClickListener?.invoke(this@CardActor)
                            longClicked = true
                        }
                    }
                }
                return true
            }

            override fun touchDragged(event: InputEvent, x: Float, y: Float, pointer: Int) {
                if (pointer == Input.Buttons.LEFT) {
                    lastPos.set(x, y)
                }
            }

            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                if (isTouchValid(x, y, button) && !longClicked) {
                    clickListener?.invoke(this@CardActor)
                }
                longClicked = false
                longClickAction = null
            }

            private fun isTouchValid(x: Float, y: Float, button: Int) =
                    enabled && !animated && button == Input.Buttons.LEFT && withinBounds(x, y)
        })
    }


    override fun drawChildren(batch: Batch, parentAlpha: Float) {
        if (card != null) {
            batch.setColor(color.r, color.g, color.b, alpha * parentAlpha)

            // Draw background
            (style.background as TransformDrawable).drawCentered(batch)

            // Draw cardstyle
            val scale = size / style.cardWidth
            val card = (if (shown) style.cards[card!!.value] else style.back)
            card.draw(batch, x + (width - card.minWidth * scale) / 2,
                    y + (height - card.minHeight * scale) / 2,
                    card.minWidth * scale, card.minHeight * scale)

            // Draw hover
            if (hoverAlpha != 0f) {
                batch.setColor(color.r, color.g, color.b, alpha * parentAlpha * hoverAlpha)
                (style.hover as TransformDrawable).drawCentered(batch)
            }

            // Draw press
            if (pressAlpha != 0f) {
                batch.setColor(color.r, color.g, color.b, alpha * parentAlpha * pressAlpha)
                (style.selection as TransformDrawable).drawCentered(batch)
            }
        }

        super.drawChildren(batch, parentAlpha)
    }

    /** Draw a drawable to fit around the actor when considering padding. */
    private fun TransformDrawable.drawCentered(batch: Batch) {
        val scale = size / style.cardWidth
        val imageWidth = style.cardWidth + leftWidth + rightWidth
        val imageHeight = style.cardHeight + bottomHeight + topHeight
        draw(batch, x + (width - imageWidth * scale) / 2,
                y + (height - imageHeight * scale) / 2, 0f, 0f,
                imageWidth, imageHeight, scale, scale, 0f)
    }

    override fun clearActions() {
        super.clearActions()
        moveAction = null
        highlightAction = null
    }

    override fun getPrefWidth() = size

    override fun getPrefHeight() = size / style.cardWidth * style.cardHeight

    override fun toString() = "[card: $card, ${if (shown) "shown" else "hidden"}" +
            "${if (highlighted) ", highlighted" else ""}]"

    /**
     * The style for cards drawn with a [CardActor], must match the [card] type.
     */
    open class CardStyle(
            /** Array of card drawables indexed by card value. */
            val cards: List<Drawable>,
            /** Drawable for the back face of a card. */
            val back: Drawable,
            /** Drawable for the card background, including shadow. */
            val background: Drawable,
            /** Drawable drawn on top of the card when hovered. */
            val hover: Drawable,
            /** Drawable drawn on top of the card when pressed. */
            val selection: Drawable,
            /** Drawable than can be drawn around a [CardStack] */
            val slot: Drawable,
            /** Width of a card, excluding shadow. */
            val cardWidth: Float,
            /** Height of a card, excluding shadow */
            val cardHeight: Float)


    companion object {
        // Card sizes presets
        const val SIZE_TINY = 160f
        const val SIZE_SMALL = 220f
        const val SIZE_NORMAL = 280f
        const val SIZE_BIG = 340f
        const val SIZE_HUGE = 400f

        /** The delay before long click is triggered. */
        private const val LONG_CLICK_DELAY = 0.5f
    }

}
