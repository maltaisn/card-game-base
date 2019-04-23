package com.maltaisn.cardgame.widget

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.maltaisn.cardgame.Resources
import ktx.assets.file
import ktx.math.vec2
import ktx.style.get


/**
 * The shader used to draw signed distance field font text.
 * Normal and bold text can be drawn with optional shadow.
 */
internal class SdfShader private constructor() :
        ShaderProgram(file(Resources.FONT_SHADER_VERT), file(Resources.FONT_SHADER_FRAG)) {

    /** Smoothing factor, between 0 and 1. Higher values result in smoother text. */
    var smoothing = 0.5f

    /** Shadow smoothing factor, between 0 and 1. Higher values result in smoother shadow. */
    var shadowSmoothing = 0.2f

    /** Whether to draw a drop shadow under the text. */
    private var drawShadow = false

    /** Drop shadow color when drawn. */
    private var shadowColor: Color = Color.BLACK


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

    inline fun use(batch: Batch, fontStyle: FontStyle, block: () -> Unit) {
        batch.shader = this

        // Update parameters
        drawShadow = fontStyle.drawShadow
        shadowColor = fontStyle.shadowColor
        updateUniforms()

        block()

        batch.shader = null
    }


    companion object {
        private const val FONT_NAME = "font"
        private const val FONT_BOLD_NAME = "font-bold"

        const val FONT_GLYPH_SIZE = 32f

        /** Create the shader, load bitmap fonts and put it in the [skin]. */
        fun load(skin: Skin): SdfShader {
            if (skin.has("default", SdfShader::class.java)) {
                // Shader was already loaded.
                return skin.get(SdfShader::class.java)
            }

            skin.add(FONT_NAME, loadFont(Resources.FONT_NAME))
            skin.add(FONT_BOLD_NAME, loadFont(Resources.FONT_BOLD_NAME))

            val shader = SdfShader()
            skin.add("default", shader)
            return shader
        }

        /** Get the bitmap font from a [skin] and load them if needed. */
        fun getFont(skin: Skin, bold: Boolean): BitmapFont {
            load(skin)
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