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

package com.maltaisn.cardgame.widget

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.scenes.scene2d.utils.TransformDrawable
import ktx.actors.alpha
import ktx.style.get
import kotlin.math.max


/**
 * A button for a popup. Unlike [Button], hover and press states are animated,
 * but there are no checked or focused states. Button can be disabled to act like a label.
 * A popup button has a label by default but it can be replaced or more actors can be added.
 */
class PopupButton(skin: Skin, text: String? = null) : SelectableWidget() {

    val style: PopupButtonStyle = skin.get()

    /** The button label showing its text. */
    val label: SdfLabel

    /** The button text. */
    var text: CharSequence?
        get() = label.text
        set(value) {
            label.setText(value)
        }

    init {
        // Apply the background padding to the table
        val background = style.background
        pad(background.topHeight, background.leftWidth, background.bottomHeight, background.rightWidth)

        // Add the button label
        label = SdfLabel(skin, style.fontStyle, text)
        label.touchable = Touchable.disabled
        add(label).expand().pad(0f, 15f, 0f, 15f)

        addListener(SelectionListener())
    }

    override fun drawChildren(batch: Batch, parentAlpha: Float) {
        val scale = style.backgroundScale

        // Draw background
        batch.setColor(color.r, color.g, color.b, alpha * parentAlpha)
        (style.background as TransformDrawable).draw(batch, x, y, 0f, 0f,
                width / scale, height / scale, scale, scale, 0f)

        // Draw button content
        super.drawChildren(batch, parentAlpha)

        // Draw hover and selection overlay
        batch.setColor(color.r, color.g, color.b, alpha * parentAlpha *
                (hoverAlpha * 0.2f + pressAlpha * 0.2f + if (enabled) 0f else 0.2f))
        (style.selectionOverlay as TransformDrawable).draw(batch, x, y, 0f, 0f,
                    width / scale, height / scale, scale, scale, 0f)
    }

    override fun getPrefWidth() = max(style.background.minWidth *
            style.backgroundScale, super.getPrefWidth())

    override fun getPrefHeight() = max(style.background.minHeight *
            style.backgroundScale, super.getPrefHeight())

    class PopupButtonStyle {
        lateinit var background: Drawable
        lateinit var selectionOverlay: Drawable
        lateinit var fontStyle: FontStyle
        var backgroundScale = 0f
    }

}