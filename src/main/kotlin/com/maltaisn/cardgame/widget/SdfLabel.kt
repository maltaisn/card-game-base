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
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.StringBuilder
import ktx.actors.alpha


/**
 * Wrapper class around [Label] for rendering text with a distance field font.
 */
open class SdfLabel(skin: Skin, val fontStyle: FontStyle, text: CharSequence? = null) :
        Label(text, LabelStyle(SdfShader.getFont(skin, fontStyle.bold), fontStyle.fontColor)) {

    /** Whether the label is enabled or not. If disabled, it's drawn with 50% alpha. */
    var enabled = true

    private val _text = StringBuilder()

    private val shader = SdfShader.load(skin)

    init {
        setFontScale(fontStyle.fontSize / SdfShader.FONT_GLYPH_SIZE)
    }

    override fun setText(newText: CharSequence?) {
        if (fontStyle.allCaps) {
            _text.setLength(0)
            if (newText != null) {
                _text.append(newText)
                super.setText(newText.toString().toUpperCase())
            } else {
                super.setText(null)
            }
        } else {
            super.setText(newText)
        }
    }

    override fun getText(): StringBuilder = if (fontStyle.allCaps) _text else super.getText()

    override fun draw(batch: Batch, parentAlpha: Float) {
        shader.use(batch, fontStyle) {
            val alphaBefore = alpha
            if (!enabled) alpha *= 0.5f
            super.draw(batch, parentAlpha)
            alpha = alphaBefore
        }
    }


}