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

package com.maltaisn.cardgame.widget.text

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.maltaisn.cardgame.CoreRes
import ktx.assets.file
import ktx.math.vec2
import ktx.style.get


/**
 * The shader used to draw signed distance field font text.
 * Normal and bold text can be drawn with optional shadow.
 */
internal class SdfShader private constructor() :
        ShaderProgram(file(CoreRes.FONT_SHADER_VERT), file(CoreRes.FONT_SHADER_FRAG)) {

    /** Smoothing factor, between 0 and 1. Higher values result in smoother text. */
    var smoothing = 0.5f

    /** Shadow smoothing factor, between 0 and 1. Higher values result in smoother shadow. */
    var shadowSmoothing = 0.2f

    /** Whether to draw a drop shadow under the text. */
    var drawShadow = false

    /** Drop shadow color when drawn. */
    var shadowColor: Color = Color.BLACK


    init {
        check(isCompiled) { "Distance field font shader compilation failed: $log" }
    }


    /** Used to update shader parameters before drawing. */
    fun updateUniforms() {
        setUniformf("u_smoothing", 0.5f * smoothing)
        setUniformf("u_drawShadow", if (drawShadow) 1f else 0f)
        if (drawShadow) {
            setUniformf("u_shadowOffset", vec2(0.004f, 0.004f))
            setUniformf("u_shadowSmoothing", shadowSmoothing)
            setUniformf("u_shadowColor", shadowColor)
        }
    }


    companion object {
        private const val FONT_NAME = "font"
        private const val FONT_BOLD_NAME = "font-bold"

        const val FONT_GLYPH_SIZE = 32f

        /** Get the shader, creating it first if needed. */
        fun getShader(skin: Skin): SdfShader {
            if (!skin.has("default", SdfShader::class.java)) {
                // Create shader and load fonts, add them to skin.
                skin.add(FONT_NAME, loadFont(CoreRes.FONT_NAME))
                skin.add(FONT_BOLD_NAME, loadFont(CoreRes.FONT_BOLD_NAME))
                skin.add("default", SdfShader())
            }
            return skin.get()
        }

        /** Get the bitmap font from a [skin], loading them first if needed. */
        fun getFont(skin: Skin, bold: Boolean): BitmapFont {
            getShader(skin)
            return skin[if (bold) {
                FONT_BOLD_NAME
            } else {
                FONT_NAME
            }]
        }

        private fun loadFont(name: String): BitmapFont {
            val texture = Texture(file("$name.png"), true)
            texture.setFilter(Texture.TextureFilter.MipMapLinearNearest, Texture.TextureFilter.Linear)
            return BitmapFont(file("$name.fnt"), TextureRegion(texture), false)
        }
    }

}
