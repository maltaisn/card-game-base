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

package io.github.maltaisn.cardengine

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.SkinLoader
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.viewport.ExtendViewport
import io.github.maltaisn.cardengine.widget.GameLayer
import io.github.maltaisn.cardengine.widget.PopupGroup
import io.github.maltaisn.cardengine.widget.card.CardAnimationLayer
import ktx.actors.plusAssign
import ktx.assets.getAsset
import ktx.assets.load
import ktx.freetype.generateFont
import ktx.freetype.registerFreeTypeFontLoaders
import kotlin.math.roundToInt


abstract class CardGameScreen(val game: CardGame) :
        Stage(ExtendViewport(960f, 540f)), Screen {

    val assetManager = AssetManager()
    val coreSkin: Skin

    /** Layer where the game takes place, contains the card containers. */
    val gameLayer: GameLayer

    /** Layer where the card are placed when animated. */
    val cardAnimationLayer: CardAnimationLayer

    /** Group where popups are shown. */
    val popupGroup: PopupGroup

    // This frame buffer is used to draw sprites offscreen, not on the screen batch.
    // The buffer can then be rendered to screen, allowing uniform transparency for example.
    lateinit var offscreenFbo: FrameBuffer
    lateinit var offscreenFboRegion: TextureRegion

    init {
        Gdx.app.logLevel = Application.LOG_DEBUG

        assetManager.apply {
            registerFreeTypeFontLoaders()
            load<TextureAtlas>(Resources.CORE_SKIN_ATLAS)
            load<Skin>(Resources.CORE_SKIN, SkinLoader.SkinParameter(Resources.CORE_SKIN_ATLAS))
            load<FreeTypeFontGenerator>(Resources.FONT_REGULAR)
            load<FreeTypeFontGenerator>(Resources.FONT_BOLD)
            finishLoading()

            coreSkin = assetManager.getAsset(Resources.CORE_SKIN)

            // Generate fonts
            val fontGen = getAsset<FreeTypeFontGenerator>(Resources.FONT_REGULAR)
            val fontBoldGen = getAsset<FreeTypeFontGenerator>(Resources.FONT_BOLD)

            createFont(fontGen, Resources.FONT_16_SHADOW, 16, true)
            createFont(fontGen, Resources.FONT_22, 22, false)
            createFont(fontBoldGen, Resources.FONT_22_BOLD, 22, false)
            createFont(fontGen, Resources.FONT_24, 24, false)
            createFont(fontBoldGen, Resources.FONT_24_BOLD_SHADOW, 24, true)
            createFont(fontBoldGen, Resources.FONT_32_BOLD, 32, false)
            createFont(fontGen, Resources.FONT_40_SHADOW, 40, true)

            fontGen.dispose()
            fontBoldGen.dispose()
        }

        gameLayer = GameLayer(coreSkin)
        cardAnimationLayer = CardAnimationLayer()
        popupGroup = PopupGroup()

        this += CardGameContainer(gameLayer, cardAnimationLayer, popupGroup)

        updateOffscreenFrameBuffer()

        actionsRequestRendering = true
    }

    private fun createFont(generator: FreeTypeFontGenerator, name: String, size: Int, hasShadow: Boolean) {
        coreSkin.add(name, generator.generateFont {
            val scale = Gdx.graphics.width / width
            this.size = (size * scale).roundToInt()
            if (hasShadow) {
                shadowOffsetY = (size / 12f * scale).roundToInt()
            }
        })
    }

    final override fun addActor(actor: Actor?) = super.addActor(actor)

    override fun show() {
        Gdx.input.inputProcessor = this
    }

    override fun hide() {
        Gdx.input.inputProcessor = null
    }

    override fun render(delta: Float) {
        act()

        // Clear with black
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        draw()
    }

    override fun pause() {

    }

    override fun resume() {

    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
        updateOffscreenFrameBuffer()
    }

    override fun dispose() {
        super.dispose()
        assetManager.dispose()
        offscreenFbo.dispose()
    }

    private fun updateOffscreenFrameBuffer() {
        offscreenFbo = FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.width, Gdx.graphics.height, false)
        offscreenFboRegion = TextureRegion(offscreenFbo.colorBufferTexture)
        offscreenFboRegion.flip(false, true)
    }

}