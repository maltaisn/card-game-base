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
import com.maltaisn.cardgame.widget.text.FontStyle
import com.maltaisn.cardgame.widget.text.SdfLabel
import ktx.actors.alpha
import ktx.style.get


/**
 * A button widget. Unlike [Button], hover and press states are animated.
 */
class Button(skin: Skin, val style: ButtonStyle, text: String? = null) : SelectableWidget() {

    /** The button label showing its text. */
    val label: SdfLabel

    /** The button text. */
    var text: CharSequence?
        get() = label.text
        set(value) {
            label.setText(value)
        }

    init {
        background = style.background
        if (style.background == null) {
            pad(20f)
        }

        // Add the button label
        label = SdfLabel(skin, style.fontStyle, text)
        label.touchable = Touchable.disabled
        add(label).expand().pad(0f, 30f, 0f, 30f)

        addListener(SelectionListener())
    }


    constructor(skin: Skin, text: String? = null, styleName: String = "default") :
            this(skin, skin[styleName], text)


    override fun drawChildren(batch: Batch, parentAlpha: Float) {
        // Draw button content
        super.drawChildren(batch, alpha * parentAlpha *
                if (enabled) 1f else style.disabledAlpha)

        // Draw hover and selection overlay
        batch.setColor(color.r, color.g, color.b, alpha * parentAlpha *
                (hoverAlpha * style.hoverOverlayAlpha +
                        pressAlpha * style.pressOverlayAlpha +
                        if (enabled) 0f else style.disabledOverlayAlpha))
        style.selectionOverlay.draw(batch, x, y, width, height)
    }

    override fun getMinHeight() = style.background?.minHeight ?: 0f

    override fun getMinWidth() = style.background?.minWidth ?: 0f


    class ButtonStyle {
        var background: Drawable? = null
        lateinit var selectionOverlay: Drawable
        lateinit var fontStyle: FontStyle

        var disabledAlpha = 0f
        var hoverOverlayAlpha = 0f
        var pressOverlayAlpha = 0f
        var disabledOverlayAlpha = 0f
    }

}
