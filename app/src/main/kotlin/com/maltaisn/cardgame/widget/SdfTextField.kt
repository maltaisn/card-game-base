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

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import ktx.actors.alpha
import ktx.actors.onKeyDownEvent
import ktx.actors.setKeyboardFocus
import ktx.style.get


/**
 * Wrapper class around [Label] for rendering text with a distance field font.
 * Doesn't support [FontStyle.allCaps] and [messageText].
 * FIXME Different font sizes not supported, see https://github.com/libgdx/libgdx/issues/5719
 */
class SdfTextField(skin: Skin,
                   fieldStyle: TextFieldStyle,
                   val fontStyle: FontStyle,
                   val messageFontStyle: FontStyle? = null,
                   text: String? = null) :
        TextField(text, createStyle(skin, fieldStyle, fontStyle, messageFontStyle)) {

    private val shader = SdfShader.getShader(skin)
    private val tempColor = Color()


    constructor(skin: Skin, fontStyle: FontStyle,
                messageFontStyle: FontStyle? = null, text: String? = null) :
            this(skin, skin.get(), fontStyle, messageFontStyle, text)


    init {
        onKeyDownEvent { event, _, keycode ->
            if (keycode == Input.Keys.BACK || keycode == Input.Keys.ESCAPE || keycode == Input.Keys.ENTER) {
                // Unfocus and hide the keyboard
                Gdx.input.setOnscreenKeyboardVisible(false)
                setKeyboardFocus(false)
                event.stop()
            }
        }
    }

    override fun drawText(batch: Batch, font: BitmapFont, x: Float, y: Float) =
            drawTextWithShader(batch, font) { super.drawText(batch, font, x, y) }

    override fun drawMessageText(batch: Batch, font: BitmapFont, x: Float, y: Float, maxWidth: Float) =
            drawTextWithShader(batch, font) { super.drawMessageText(batch, font, x, y, maxWidth) }

    private inline fun drawTextWithShader(batch: Batch, font: BitmapFont, draw: () -> Unit) {
        font.color.a *= if (isDisabled) 0.5f else 1f
        batch.shader = shader
        updateFontShader()
        draw()
        batch.shader = null
    }

    private fun updateFontShader() {
        // Update shader parameters
        shader.drawShadow = fontStyle.drawShadow
        if (fontStyle.drawShadow) {
            // Adjust shadow alpha to font color alpha and label alpha
            val sc = fontStyle.shadowColor
            tempColor.set(sc.r, sc.g, sc.b, sc.a * fontStyle.fontColor.a * alpha)
            shader.shadowColor = tempColor
        }
        shader.updateUniforms()
    }

    companion object {
        private fun createStyle(skin: Skin, fieldStyle: TextFieldStyle,
                                fontStyle: FontStyle, messageFontStyle: FontStyle?): TextFieldStyle {
            val style = TextFieldStyle(fieldStyle)

            style.font = SdfShader.getFont(skin, fontStyle.bold)
            style.font.data.setScale(fontStyle.fontSize / SdfShader.FONT_GLYPH_SIZE)

            if (messageFontStyle != null) {
                style.messageFont = SdfShader.getFont(skin, messageFontStyle.bold)
                style.messageFont.data.setScale(fontStyle.fontSize / SdfShader.FONT_GLYPH_SIZE)
            }

            val color = fontStyle.fontColor
            style.fontColor = color
            style.messageFontColor = messageFontStyle?.fontColor

            return style
        }
    }

}
