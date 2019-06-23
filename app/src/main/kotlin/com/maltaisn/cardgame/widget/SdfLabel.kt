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

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.StringBuilder
import ktx.actors.alpha


/**
 * Wrapper class around [Label] for rendering text with a distance field font.
 */
open class SdfLabel(private val skin: Skin, fontStyle: FontStyle, text: CharSequence? = null) :
        Label(null, createStyle(skin, fontStyle)) {

    /**
     * The font style used by this label.
     * Font styles are mutable but they must be set again to be updated effectively.
     */
    var fontStyle = fontStyle
        set(value) {
            style = createStyle(skin, value)
            setFontScale(value.fontSize / SdfShader.FONT_GLYPH_SIZE)

            val text = text
            field = value
            setText(text)
        }

    /** Whether the label is enabled or not. If disabled, it's drawn with 50% alpha. */
    var enabled = true

    private val _text = StringBuilder()

    private val shader = SdfShader.getShader(skin)
    private val tempColor = Color()

    init {
        this.fontStyle = fontStyle
        setText(text)
    }

    override fun setText(newText: CharSequence?) {
        if (fontStyle.allCaps) {
            if (newText === _text) {
                super.setText(newText.toString().toUpperCase())
            } else {
                _text.setLength(0)
                if (newText != null) {
                    _text.append(newText)
                    super.setText(newText.toString().toUpperCase())
                } else {
                    super.setText(null)
                }
            }
        } else {
            super.setText(newText)
        }
    }

    override fun getText(): StringBuilder = if (fontStyle.allCaps) _text else super.getText()

    override fun draw(batch: Batch, parentAlpha: Float) {
        val alphaBefore = alpha
        if (!enabled) alpha *= 0.5f

        batch.shader = shader

        // Update shader parameters
        shader.drawShadow = fontStyle.drawShadow
        if (fontStyle.drawShadow) {
            // Adjust shadow alpha to font color alpha and label alpha
            val sc = fontStyle.shadowColor
            tempColor.set(sc.r, sc.g, sc.b, sc.a * fontStyle.fontColor.a * alpha)
            shader.shadowColor = tempColor
        }
        shader.updateUniforms()

        // Draw the text
        super.draw(batch, parentAlpha)

        batch.shader = null

        alpha = alphaBefore
    }

    companion object {
        fun createStyle(skin: Skin, fontStyle: FontStyle) =
                LabelStyle(SdfShader.getFont(skin, fontStyle.bold), fontStyle.fontColor)
    }


}