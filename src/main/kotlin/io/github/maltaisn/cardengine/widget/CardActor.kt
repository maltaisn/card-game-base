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
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.FrameBuffer
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
import ktx.actors.alpha
import kotlin.math.roundToInt


/**
 * An actor that draws a card.
 * @property coreStyle Engine core style.
 * @property cardStyle Card actor style, must match card type.
 * @param card Card to display.
 */
class CardActor(val coreStyle: GameLayer.CoreStyle, val cardStyle: CardStyle, card: Card) : Widget() {

    /** Card shown by the actor. */
    var card = card
        set(value) {
            field = value
            needsRendering = true
        }

    /**
     * Whether the card value is displayed.
     * If not shown, the back of the card is displayed instead.
     */
    var shown = true
        set(value) {
            field = value
            needsRendering = true
        }

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
                needsRendering = true
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

    // The card is drawn with many sprites. They are drawn on a frame buffer then drawn to the screen batch.
    // The card is only rendered to the frame buffer when needed.
    private var needsRendering = true
    private val fbo: FrameBuffer
    private val fboRegion: TextureRegion
    private val renderBatch: SpriteBatch

    /**
     * Internal flag used by the animation layer to indicate when a card is being animated.
     * An animated card doesn't fire click and long click events.
     */
    internal var animated = false

    // Used when animating to set the source and destination containers on a moved card.
    internal var src: CardContainer? = null
    internal var dst: CardContainer? = null


    constructor(skin: Skin, card: Card) :
            this(skin.get(GameLayer.CoreStyle::class.java),
                    skin.get(CardStyle::class.java), card)

    constructor(skin: Skin, coreStyleName: String, cardStyleName: String, card: Card) :
            this(skin.get(coreStyleName, GameLayer.CoreStyle::class.java),
                    skin.get(cardStyleName, CardStyle::class.java), card)


    init {
        val fboWidth = cardStyle.cardWidth + coreStyle.cardBackground.horizontalWidth
        val fboHeight = cardStyle.cardHeight + coreStyle.cardBackground.verticalHeight
        fbo = FrameBuffer(Pixmap.Format.RGBA8888, fboWidth.roundToInt(), fboHeight.roundToInt(), false)
        fboRegion = TextureRegion(fbo.colorBufferTexture)
        fboRegion.flip(false, true)

        val camera = OrthographicCamera(fboWidth, fboHeight)
        camera.translate(fboWidth / 2, fboHeight / 2)
        camera.update()
        renderBatch = SpriteBatch(8)
        renderBatch.enableBlending()
        renderBatch.setBlendFunctionSeparate(GL20.GL_SRC_ALPHA,
                GL20.GL_ONE_MINUS_SRC_ALPHA, GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA)
        renderBatch.projectionMatrix = camera.combined

        setSize(prefWidth, prefHeight)

        addListener(object : InputListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                if (enabled) {
                    selected = true
                    selectionElapsed = 0f
                    lastTouchDownTime = System.currentTimeMillis()
                    longClicked = false
                    needsRendering = true
                }
                return true
            }

            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                if (enabled) {
                    selected = false
                    selectionElapsed = Animation.SELECTION_FADE_DURATION * selectionAlpha
                    lastTouchDownTime = 0
                    needsRendering = true

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
                    needsRendering = true
                }
            }

            override fun exit(event: InputEvent, x: Float, y: Float, pointer: Int, toActor: Actor?) {
                if (enabled && pointer == -1) {
                    hovered = false
                    hoverElapsed = Animation.HOVER_FADE_DURATION * hoverAlpha
                    needsRendering = true
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
            needsRendering = true
        } else if (!selected && selectionElapsed > 0f) {
            selectionElapsed -= delta
            selectionAlpha = Animation.SELECTION_OUT_INTERPOLATION.applyBounded(selectionElapsed / Animation.SELECTION_FADE_DURATION)
            needsRendering = true
        }

        // Update hover alpha
        if (hovered && hoverElapsed < Animation.HOVER_FADE_DURATION) {
            hoverElapsed += delta
            hoverAlpha = Animation.HOVER_IN_INTERPOLATION.applyBounded(hoverElapsed / Animation.HOVER_FADE_DURATION)
            needsRendering = true
        } else if (!hovered && hoverElapsed > 0f) {
            hoverElapsed -= delta
            hoverAlpha = Animation.HOVER_OUT_INTERPOLATION.applyBounded(hoverElapsed / Animation.HOVER_FADE_DURATION)
            needsRendering = true
        }
    }


    override fun draw(batch: Batch, parentAlpha: Float) {
        val background = coreStyle.cardBackground

        if (needsRendering) {
            // Draw the sprites of the card on a frame buffer first
            batch.end()
            fbo.begin()
            renderBatch.begin()
            renderBatch.setColor(1f, 1f, 1f, 1f)
            Gdx.gl.glClearColor(0f, 0f, 0f, 0f)
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

            // Draw background
            val w = cardStyle.cardWidth + background.horizontalWidth
            val h = cardStyle.cardHeight + background.verticalHeight
            background.draw(renderBatch, 0f, 0f, w, h)

            // Draw card
            val card = if (shown) cardStyle.cards[card.value] else cardStyle.back
            card.draw(renderBatch, (w - card.minWidth) / 2, (h - card.minHeight) / 2,
                    card.minWidth, card.minHeight)

            // Draw hover
            if (hoverAlpha != 0f) {
                renderBatch.setColor(1f, 1f, 1f, hoverAlpha)
                coreStyle.cardHover.draw(renderBatch, 0f, 0f, w, h)
            }

            // Draw selection
            if (selectionAlpha != 0f) {
                renderBatch.setColor(1f, 1f, 1f, selectionAlpha)
                coreStyle.cardSelection.draw(renderBatch, background.leftWidth,
                        background.bottomHeight, cardStyle.cardWidth, cardStyle.cardHeight)
            }

            renderBatch.end()
            fbo.end()
            batch.begin()

            needsRendering = false
        }

        // Draw the frame buffer texture to the screen
        val scale = size / cardStyle.cardWidth
        val colorBefore = batch.color.cpy()
        // FBO blending is a nightmare: https://stackoverflow.com/a/18497511/5288316
        batch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA)
        batch.setColor(1f, 1f, 1f, parentAlpha * alpha)
        batch.draw(fboRegion, x - background.leftWidth * scale, y - background.bottomHeight * scale,
                0f, 0f, width / scale + background.horizontalWidth,
                height / scale + background.verticalHeight, scale, scale, 0f)
        batch.color = colorBefore
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
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
    }


    private inline val Drawable.horizontalWidth: Float
        get() = this.leftWidth + this.rightWidth

    private inline val Drawable.verticalHeight: Float
        get() = this.bottomHeight + this.topHeight

}