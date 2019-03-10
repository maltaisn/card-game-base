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
import io.github.maltaisn.cardgame.Resources
import ktx.assets.file
import ktx.math.vec2


/**
 * Wrapper class around [Label] for rendering text with a distance field font.
 */
class SdfLabel(text: CharSequence?, private val skin: Skin, sdfStyle: SdfLabelStyle) :
        Label(text, createLabelStyle(skin, sdfStyle)) {

    /** The style of the distance field label, replaces [style]. */
    var sdfStyle: SdfLabelStyle = sdfStyle
        set(value) {
            field = value
            style = createLabelStyle(skin, value)
        }

    private val sdfShader: SdfShader

    private val tempColor = Color()


    init {
        sdfShader = skin.get(SHADER_NAME, SdfShader::class.java)
        setFontScale(sdfStyle.fontSize / 32f)
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        // Draw the text
        if (batch.shader === sdfShader) {
            // No need to change the shader
            super.draw(batch, parentAlpha)
        } else {
            tempColor.set(batch.color)
            batch.setColor(color.r, color.g, color.b, color.a * parentAlpha)

            batch.shader = sdfShader

            sdfShader.drawShadow = sdfStyle.drawShadow
            sdfShader.shadowColor = sdfStyle.shadowColor
            sdfShader.updateUniforms()

            super.draw(batch, parentAlpha)
            batch.shader = null
            batch.setColor(tempColor.r, tempColor.g, tempColor.b, tempColor.a)
        }
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

    class SdfLabelStyle {
        var bold = false
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

        private fun createLabelStyle(skin: Skin, style: SdfLabelStyle): LabelStyle {
            load(skin)
            val font = skin.getFont(if (style.bold) FONT_BOLD_NAME else FONT_NAME)
            return LabelStyle(font, style.fontColor)
        }

    }

}