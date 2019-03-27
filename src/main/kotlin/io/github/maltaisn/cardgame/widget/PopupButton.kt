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

package io.github.maltaisn.cardgame.widget

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.scenes.scene2d.utils.TransformDrawable
import io.github.maltaisn.cardgame.applyBounded
import io.github.maltaisn.cardgame.withinBounds
import ktx.actors.alpha
import kotlin.math.max


/**
 * A button for a popup. Unlike [Button], hover and press states are animated,
 * but there are no checked or focused states. Button can be disabled to act like a label.
 * A popup button has a label by default but it can be replaced or more actors can be added.
 */
class PopupButton(skin: Skin, text: CharSequence? = null) : Table(skin) {

    val style: PopupButtonStyle = skin[PopupButtonStyle::class.java]

    /** The button label showing its text. */
    val label: SdfLabel

    /** The button text. */
    var text: CharSequence?
        set(value) {
            label.setText(value)
        }
        get() = label.text

    /** Whether the button can be pressed, hovered and clicked */
    var enabled = true
        set(value) {
            field = value
            if (!value) {
                pressed = false
                hovered = false
                pressAlpha = 0f
                hoverAlpha = 0f
            }
        }

    /**
     * Click listener, called when the button is clicked. Clicks must end within the bounds.
     * The listener is not called when the button is disabled.
     */
    var clickListener: ((PopupButton) -> Unit)? = null


    private var pressed = false
    private var pressElapsed = 0f
    private var pressAlpha = 0f

    private var hovered = false
    private var hoverElapsed = 0f
    private var hoverAlpha = 0f


    init {
        touchable = Touchable.enabled

        addListener(object : InputListener() {
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                if (enabled && button == Input.Buttons.LEFT) {
                    pressed = true
                    pressElapsed = 0f
                }
                return true
            }

            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                if (enabled && button == Input.Buttons.LEFT) {
                    pressed = false
                    pressElapsed = PRESS_FADE_DURATION * pressAlpha

                    if (clickListener != null && withinBounds(x, y)) {
                        clickListener!!(this@PopupButton)
                    }
                }
            }

            override fun enter(event: InputEvent, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
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

        // Apply the background padding to the table
        val background = style.background
        pad(background.topHeight, background.leftWidth, background.bottomHeight, background.rightWidth)

        // Add the button label
        label = SdfLabel(text, skin, style.fontStyle)
        add(label).expand().pad(0f, 15f, 0f, 15f)
    }

    override fun act(delta: Float) {
        super.act(delta)

        var renderingNeeded = false

        // Update press alpha
        if (pressed && pressElapsed < PRESS_FADE_DURATION) {
            pressElapsed += delta
            pressAlpha = PRESS_IN_INTERPOLATION.applyBounded(pressElapsed / PRESS_FADE_DURATION)
            renderingNeeded = true
        } else if (!pressed && pressElapsed > 0f) {
            pressElapsed -= delta
            pressAlpha = PRESS_OUT_INTERPOLATION.applyBounded(pressElapsed / PRESS_FADE_DURATION)
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

    override fun drawChildren(batch: Batch, parentAlpha: Float) {
        val scale = style.backgroundScale

        // Draw background
        batch.setColor(color.r, color.g, color.b, alpha * parentAlpha)
        (style.background as TransformDrawable).draw(batch, x, y, 0f, 0f,
                width / scale, height / scale, scale, scale, 0f)

        // Draw button content
        super.drawChildren(batch, parentAlpha)

        // Draw hover
        if (hoverAlpha != 0f) {
            batch.setColor(color.r, color.g, color.b, alpha * parentAlpha * hoverAlpha)
            (style.hover as TransformDrawable).draw(batch, x, y, 0f, 0f,
                    width / scale, height / scale, scale, scale, 0f)
        }

        // Draw press
        if (pressAlpha != 0f) {
            batch.setColor(color.r, color.g, color.b, alpha * parentAlpha * pressAlpha)
            (style.press as TransformDrawable).draw(batch, x, y, 0f, 0f,
                    width / scale, height / scale, scale, scale, 0f)
        }
    }

    override fun getPrefWidth(): Float {
        val prefWidth = super.getPrefWidth()
        return max(style.background.minWidth * style.backgroundScale, prefWidth)
    }

    override fun getPrefHeight(): Float {
        val prefHeight = super.getPrefHeight()
        return max(style.background.minHeight * style.backgroundScale, prefHeight)
    }

    class PopupButtonStyle {
        lateinit var background: Drawable
        lateinit var hover: Drawable
        lateinit var press: Drawable
        lateinit var fontStyle: SdfLabel.FontStyle
        var backgroundScale = 0f
    }

    companion object {
        /** The duration of the hover fade. */
        private const val HOVER_FADE_DURATION = 0.3f

        /** The duration of the press fade. */
        private const val PRESS_FADE_DURATION = 0.3f

        private val PRESS_IN_INTERPOLATION: Interpolation = Interpolation.smooth
        private val PRESS_OUT_INTERPOLATION: Interpolation = Interpolation.smooth
        private val HOVER_IN_INTERPOLATION: Interpolation = Interpolation.pow2Out
        private val HOVER_OUT_INTERPOLATION: Interpolation = Interpolation.smooth
    }

}