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

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.StringBuilder
import io.github.maltaisn.cardgame.Resources
import ktx.actors.alpha
import ktx.assets.file
import ktx.math.vec2
import ktx.style.get


/**
 * Wrapper class around [Label] for rendering text with a distance field font.
 */
open class SdfLabel(text: CharSequence?, private val skin: Skin, sdfStyle: FontStyle) :
        Label(text, createLabelStyle(skin, sdfStyle)) {

    /** The font style of the distance field label, replaces [Label.style]. */
    var fontStyle: FontStyle = sdfStyle
        set(value) {
            field = value
            style = createLabelStyle(skin, value)
        }

    /** Whether the label is enabled or not. If disabled, it's drawn with 50% alpha. */
    var enabled = true

    private var _text = StringBuilder()

    private val sdfShader: SdfShader


    init {
        sdfShader = skin[SHADER_NAME]

        // The font size is divided by 32 because that's the glyph size in the texture
        setFontScale(sdfStyle.fontSize / 32f)
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
        val alphaBefore = alpha
        if (!enabled) alpha *= 0.5f
        batch.shader = sdfShader

        sdfShader.drawShadow = fontStyle.drawShadow
        sdfShader.shadowColor = fontStyle.shadowColor
        sdfShader.updateUniforms()

        super.draw(batch, parentAlpha)

        alpha = alphaBefore
        batch.shader = null
    }

    private class SdfShader : ShaderProgram(
            file(Resources.FONT_SHADER_VERT),
            file(Resources.FONT_SHADER_FRAG)) {

        var smoothing = 0.5f

        var drawShadow = false
        var shadowSmoothing = 0.2f
        var shadowColor: Color = Color.BLACK

        init {
            check(isCompiled) { "Distance field font shader compilation failed: $log" }
        }

        fun updateUniforms() {
            setUniformf("u_smoothing", 0.5f * smoothing)
            setUniformf("u_drawShadow", if (drawShadow) 1f else 0f)
            if (drawShadow) {
                setUniformf("u_shadowOffset", SHADOW_OFFSET)
                setUniformf("u_shadowSmoothing", shadowSmoothing)
                setUniformf("u_shadowColor", shadowColor)
            }
        }
    }

    class FontStyle {
        var bold = false
        var allCaps = false
        var fontSize = 24f
        var fontColor: Color = Color.WHITE
        var drawShadow = false
        var shadowColor: Color = Color.BLACK
    }


    companion object {
        private const val SHADER_NAME = "sdf-shader"
        private const val FONT_NAME = "sdf-font"
        private const val FONT_BOLD_NAME = "sdf-font-bold"

        private val SHADOW_OFFSET = vec2(0.004f, 0.004f)

        /** Load the shader and bitmap font needed for all labels and add them to a [skin]. */
        fun load(skin: Skin) {
            if (skin.has(SHADER_NAME, SdfShader::class.java)) return

            skin.add(SHADER_NAME, SdfShader())

            val fontTexture = Texture(file(Resources.FONT_TEXTURE), true)
            fontTexture.setFilter(Texture.TextureFilter.MipMapLinearNearest, Texture.TextureFilter.Linear)
            val font = BitmapFont(file(Resources.FONT_FILE), TextureRegion(fontTexture), false)
            skin.add(FONT_NAME, font)

            val fontBoldTexture = Texture(file(Resources.FONT_BOLD_TEXTURE), true)
            fontBoldTexture.setFilter(Texture.TextureFilter.MipMapLinearNearest, Texture.TextureFilter.Linear)
            val fontBold = BitmapFont(file(Resources.FONT_BOLD_FILE), TextureRegion(fontBoldTexture), false)
            skin.add(FONT_BOLD_NAME, fontBold)
        }

        private fun createLabelStyle(skin: Skin, style: FontStyle): LabelStyle {
            load(skin)
            val font: BitmapFont = skin[if (style.bold) FONT_BOLD_NAME else FONT_NAME]
            return LabelStyle(font, style.fontColor)
        }

    }

}