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
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Widget
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import io.github.maltaisn.cardengine.Animation
import io.github.maltaisn.cardengine.applyBounded
import io.github.maltaisn.cardengine.core.Card
import io.github.maltaisn.cardengine.withinBounds


/**
 * An actor that draws a card.
 * @property card Card shown by the actor.
 */
class CardActor(val style: CardStyle, var card: Card) : Widget() {

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
    var size = SIZE_NORMAL
        set(value) {
            field = value
            setSize(prefWidth, prefHeight)
        }

    /**
     * Click listeners, called when the actor is clicked. Clicks must end within the bounds.
     * The listeners are not called when the actor is disabled or animated.
     */
    val clickListeners = mutableListOf<ClickListener>()

    /**
     * Click listeners, called when the actor is long clicked.
     * The listeners are not called when the actor is disabled or animated.
     */
    val longClickListeners = mutableListOf<LongClickListener>()
    private var lastTouchDownTime = 0L
    private var longClicked = false

    // Used for hover and selection interpolation.
    private var selected = false
    private var hovered = false
    private var selectionElapsed = 0f
    private var hoverElapsed = 0f
    private var selectionAlpha = 0f
    private var hoverAlpha = 0f

    /**
     * Internal flag used by the animation layer to indicate when a card is being animated.
     * An animated card doesn't fire click and long click events.
     */
    internal var animated = false

    // Used when animating to set the source and destination containers on a moved card.
    internal var src: CardContainer? = null
    internal var dst: CardContainer? = null


    constructor(skin: Skin, card: Card) :
            this(skin.get(CardStyle::class.java), card)

    constructor(skin: Skin, styleName: String, card: Card) :
            this(skin.get(styleName, CardStyle::class.java), card)


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
                    selectionElapsed = Animation.SELECTION_FADE_DURATION * selectionAlpha
                    lastTouchDownTime = 0

                    if (!animated && !longClicked && withinBounds(x, y)) {
                        // Click ended in actor, call listeners.
                        // The list is copied so changes to it don't lead to concurrent modification errors.
                        for (listener in clickListeners.toMutableList()) {
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
            for (listener in longClickListeners.toMutableList()) {
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
        val colorBefore = batch.color.cpy()

        val scale = size / style.cardWidth
        batch.setColor(1f, 1f, 1f, parentAlpha)

        // Draw shadow
        val shadow = style.shadow
        shadow.draw(batch, x + style.shadowOffsetX * scale, y + style.shadowOffsetY * scale,
                shadow.minWidth * scale, shadow.minHeight * scale)

        // Draw card
        val card = if (shown) style.cards[card.value] else style.back
        card.draw(batch, x, y, card.minWidth * scale, card.minHeight * scale)

        // Draw hover
        if (hoverAlpha != 0f) {
            val hover = style.hover
            batch.setColor(1f, 1f, 1f, parentAlpha * hoverAlpha)
            hover.draw(batch, x + style.hoverOffsetX * scale, y + style.hoverOffsetY * scale,
                    hover.minWidth * scale, hover.minHeight * scale)
        }

        // Draw selection
        if (selectionAlpha != 0f) {
            val selection = style.selection
            batch.setColor(1f, 1f, 1f, parentAlpha * selectionAlpha)
            selection.draw(batch, x, y, selection.minWidth * scale, selection.minHeight * scale)
        }

        batch.color = colorBefore
    }

    override fun getPrefWidth() = size

    override fun getPrefHeight() = size / style.cardWidth * style.cardHeight

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
        lateinit var cards: Array<Drawable>
        lateinit var back: Drawable
        lateinit var shadow: Drawable
        lateinit var hover: Drawable
        lateinit var selection: Drawable
        lateinit var slot: Drawable
        var cardWidth = 0f
        var cardHeight = 0f
        var shadowOffsetX = 0f
        var shadowOffsetY = 0f
        var hoverOffsetX = 0f
        var hoverOffsetY = 0f
    }

    companion object {
        // Card sizes presets
        const val SIZE_TINY = 80f
        const val SIZE_SMALL = 100f
        const val SIZE_NORMAL = 120f
        const val SIZE_BIG = 150f
        const val SIZE_HUGE = 200f
    }

}