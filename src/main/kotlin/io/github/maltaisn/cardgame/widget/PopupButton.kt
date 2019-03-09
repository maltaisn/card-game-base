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
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.scenes.scene2d.utils.TransformDrawable
import io.github.maltaisn.cardgame.applyBounded
import io.github.maltaisn.cardgame.withinBounds
import kotlin.math.max


/**
 * A button for a popup. Unlike [Button], hover and selection states are animated,
 * but there are no checked or focused states. Button can be disabled to act like a label.
 * A popup button has a label by default but it can be replaced or more actors can be added.
 */
class PopupButton(skin: Skin, text: CharSequence? = null) : Table(skin) {

    val style: PopupButtonStyle = skin.get(PopupButtonStyle::class.java)

    /** The button label showing its text. */
    val label: SdfLabel

    /** The button text. */
    var text: CharSequence?
        set(value) {
            label.setText(value)
        }
        get() = label.text

    /** Whether the button can be selected, hovered and clicked */
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
     * Click listener, called when the button is clicked. Clicks must end within the bounds.
     * The listener is not called when the button is disabled.
     */
    var clickListener: ClickListener? = null


    // Hover and selection status.
    private var selected = false
    private var hovered = false
    private var selectionElapsed = 0f
    private var hoverElapsed = 0f
    private var selectionAlpha = 0f
    private var hoverAlpha = 0f

    init {
        addListener(object : InputListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                if (enabled) {
                    selected = true
                    selectionElapsed = 0f
                }
                return true
            }

            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                if (enabled) {
                    selected = false
                    selectionElapsed = SELECTION_FADE_DURATION * selectionAlpha

                    if (clickListener != null && withinBounds(x, y)) {
                        clickListener?.onButtonClicked(this@PopupButton)
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

        // Apply the background padding to the table
        val background = style.background
        pad(background.topHeight, background.leftWidth, background.bottomHeight, background.rightWidth)

        // Add the button label
        label = SdfLabel(text, skin, style.fontStyle)
        add(label).fill().pad(0f, 15f, 0f, 15f)
    }

    override fun act(delta: Float) {
        super.act(delta)

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

    override fun drawChildren(batch: Batch, parentAlpha: Float) {
        val colorBefore = batch.color.cpy()
        val scale = style.backgroundScale

        // Draw background
        batch.setColor(1f, 1f, 1f, 1f)
        (style.background as TransformDrawable).draw(batch, x, y, 0f, 0f,
                width / scale, height / scale, scale, scale, 0f)


        // Draw button content
        super.drawChildren(batch, parentAlpha)

        // Draw hover
        if (hoverAlpha != 0f) {
            batch.setColor(hoverAlpha, hoverAlpha, hoverAlpha, hoverAlpha)
            (style.hover as TransformDrawable).draw(batch, x, y, 0f, 0f,
                    width / scale, height / scale, scale, scale, 0f)
        }

        // Draw selection
        if (selectionAlpha != 0f) {
            batch.setColor(selectionAlpha, selectionAlpha, selectionAlpha, selectionAlpha)
            (style.selection as TransformDrawable).draw(batch, x, y, 0f, 0f,
                    width / scale, height / scale, scale, scale, 0f)
        }

        batch.color = colorBefore
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
        lateinit var selection: Drawable
        lateinit var fontStyle: SdfLabel.SdfLabelStyle
        var backgroundScale = 0f
    }

    interface ClickListener {
        fun onButtonClicked(button: PopupButton)
    }

    companion object {
        /** The duration of the hover fade. */
        private const val HOVER_FADE_DURATION = 0.3f

        /** The duration of the selection fade. */
        private const val SELECTION_FADE_DURATION = 0.3f

        private val SELECTION_IN_INTERPOLATION: Interpolation = Interpolation.smooth
        private val SELECTION_OUT_INTERPOLATION: Interpolation = Interpolation.smooth
        private val HOVER_IN_INTERPOLATION: Interpolation = Interpolation.pow2Out
        private val HOVER_OUT_INTERPOLATION: Interpolation = Interpolation.smooth
    }

}