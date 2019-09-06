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
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.maltaisn.msdfgdx.FontStyle
import com.maltaisn.msdfgdx.MsdfFont
import com.maltaisn.msdfgdx.MsdfShader
import ktx.actors.onKeyDownEvent
import ktx.actors.setKeyboardFocus
import ktx.style.get


/**
 * Wrapper class around [TextField] for rendering text with a distance field font.
 * Doesn't support [FontStyle.allCaps].
 * FIXME Different font sizes not supported, see https://github.com/libgdx/libgdx/issues/5719
 */
class MsdfTextField(skin: Skin,
                    fieldStyle: TextFieldStyle,
                    val fontStyle: FontStyle,
                    val messageFontStyle: FontStyle? = null,
                    text: String? = null) :
        TextField(text, createStyle(skin, fieldStyle, fontStyle, messageFontStyle)) {


    private val shader: MsdfShader = skin.get()

    private val font: MsdfFont = skin[fontStyle.fontName]
    private val messageFont: MsdfFont? = messageFontStyle?.let { skin[it.fontName] }


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
            drawTextWithShader(batch, this.font, fontStyle) {
                super.drawText(batch, font, x, y)
            }

    override fun drawMessageText(batch: Batch, font: BitmapFont, x: Float, y: Float, maxWidth: Float) =
            drawTextWithShader(batch, messageFont!!, messageFontStyle!!) {
                super.drawMessageText(batch, font, x, y, maxWidth)
            }

    private inline fun drawTextWithShader(batch: Batch, font: MsdfFont,
                                          fontStyle: FontStyle, draw: () -> Unit) {
        val alphaBefore = font.font.color.a
        font.font.color.a *= if (isDisabled) 0.5f else 1f

        batch.shader = shader
        shader.updateForFont(font, fontStyle)

        draw()

        batch.shader = null
        font.font.color.a = alphaBefore
    }

    companion object {
        private fun createStyle(skin: Skin, fieldStyle: TextFieldStyle,
                                fontStyle: FontStyle, messageFontStyle: FontStyle?): TextFieldStyle {
            val style = TextFieldStyle(fieldStyle)

            // Font
            val font: MsdfFont = skin[fontStyle.fontName]
            style.font = font.font
            style.font.data.setScale(fontStyle.size / font.glyphSize)
            style.fontColor = Color.WHITE

            // Message font
            if (messageFontStyle != null) {
                val messageFont: MsdfFont = skin[messageFontStyle.fontName]
                style.font = messageFont.font
                style.font.data.setScale(messageFontStyle.size / messageFont.glyphSize)
                style.messageFontColor = Color.WHITE
            }

            return style
        }
    }

}
