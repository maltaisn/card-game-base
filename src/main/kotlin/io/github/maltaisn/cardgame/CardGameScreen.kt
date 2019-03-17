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

package io.github.maltaisn.cardgame

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.SkinLoader
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.I18NBundle
import com.badlogic.gdx.utils.viewport.ExtendViewport
import io.github.maltaisn.cardgame.widget.GameLayer
import io.github.maltaisn.cardgame.widget.GameMenu
import io.github.maltaisn.cardgame.widget.PopupGroup
import io.github.maltaisn.cardgame.widget.SdfLabel
import io.github.maltaisn.cardgame.widget.card.CardAnimationLayer
import ktx.actors.plusAssign
import ktx.assets.getAsset
import ktx.assets.load


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

    /** The game menu. */
    var gameMenu: GameMenu? = null
        set(value) {
            check(field == null) { "Game menu can only be set once." }
            if (value != null) {
                field = value
                rootContainer += value
            }
        }

    // This frame buffer is used to draw sprites offscreen, not on the screen batch.
    // The buffer can then be rendered to screen, allowing uniform transparency for example.
    lateinit var offscreenFbo: FrameBuffer
    lateinit var offscreenFboRegion: TextureRegion

    private val rootContainer: CardGameContainer

    init {
        @Suppress("LibGDXLogLevel")
        Gdx.app.logLevel = Application.LOG_DEBUG

        updateOffscreenFrameBuffer()
        actionsRequestRendering = true

        assetManager.apply {
            load<TextureAtlas>(Resources.CORE_SKIN_ATLAS)
            load<Skin>(Resources.CORE_SKIN, SkinLoader.SkinParameter(Resources.CORE_SKIN_ATLAS))
            load<I18NBundle>(Resources.CORE_STRINGS_FILE)
            finishLoading()

            coreSkin = assetManager.getAsset(Resources.CORE_SKIN)
            coreSkin.add(Resources.CORE_STRINGS_NAME, assetManager.getAsset<I18NBundle>(Resources.CORE_STRINGS_FILE))

            SdfLabel.load(coreSkin)
        }

        // Create the layout
        gameLayer = GameLayer(coreSkin)
        cardAnimationLayer = CardAnimationLayer()
        popupGroup = PopupGroup()

        rootContainer = CardGameContainer(gameLayer, cardAnimationLayer, popupGroup)
        @Suppress("LeakingThis")
        addActor(rootContainer)
    }

    override fun show() {
        Gdx.input.inputProcessor = this
        Gdx.input.isCatchBackKey = true
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

    /**
     * Load the PCard skin, containing standard playing cards sprites. Doesn't finish loading.
     */
    protected fun loadPCardSkin() {
        assetManager.apply {
            load<TextureAtlas>(Resources.PCARD_SKIN_ATLAS)
            load<Skin>(Resources.PCARD_SKIN, SkinLoader.SkinParameter(Resources.PCARD_SKIN_ATLAS))
        }
    }

    private fun updateOffscreenFrameBuffer() {
        if (::offscreenFbo.isInitialized) {
            offscreenFbo.dispose()
        }
        offscreenFbo = FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.width, Gdx.graphics.height, false)
        offscreenFboRegion = TextureRegion(offscreenFbo.colorBufferTexture)
        offscreenFboRegion.flip(false, true)
    }

}