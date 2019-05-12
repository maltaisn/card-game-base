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

package com.maltaisn.cardgame

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Screen
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.SkinLoader
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.utils.I18NBundle
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.maltaisn.cardgame.core.CardGame
import com.maltaisn.cardgame.core.PCard
import com.maltaisn.cardgame.markdown.MdLoader
import com.maltaisn.cardgame.prefs.GamePrefs
import com.maltaisn.cardgame.prefs.GamePrefsLoader
import com.maltaisn.cardgame.widget.SdfShader
import com.maltaisn.cardgame.widget.menu.GameMenu
import ktx.assets.getAsset
import ktx.assets.load
import ktx.assets.setLoader


abstract class CardGameScreen : Stage(ExtendViewport(960f, 540f)), Screen {

    val assetManager = AssetManager()

    /** Skin containing core UI assets. */
    val coreSkin: Skin

    var game: CardGame? = null

    /** The game layout. */
    var gameLayout: CardGameLayout? = null
        set(value) {
            check(field == null) { "Game layout can only be set once." }
            if (value != null) {
                field = value
                if (gameMenu != null) {
                    value.addActor(gameMenu)
                }
                addActor(value)
            }
        }

    /** The game menu. */
    var gameMenu: GameMenu? = null
        set(value) {
            check(field == null) { "Game menu can only be set once." }
            if (value != null) {
                field = value
                if (gameLayout != null) {
                    gameLayout?.addActor(value)
                }
                value.shown = true
            }
        }

    /** List of game prefs to save on pause. */
    val prefs = mutableListOf<GamePrefs>()

    // This frame buffer is used to draw sprites offscreen, not on the screen batch.
    // The buffer can then be rendered to screen, allowing uniform transparency for example.
    lateinit var offscreenFbo: FrameBuffer
        private set
    lateinit var offscreenFboRegion: TextureRegion
        private set

    private var started = false


    init {
        @Suppress("LibGDXLogLevel")

        updateOffscreenFrameBuffer()
        actionsRequestRendering = true

        // Register asset loaders
        val fileResolver = InternalFileHandleResolver()
        assetManager.setLoader(GamePrefsLoader(fileResolver))
        assetManager.setLoader(MdLoader(fileResolver))

        // Load core skin
        loadCoreSkin()
        coreSkin = assetManager.get(Resources.CORE_SKIN)

        // Listener to unfocus text field when clicked outside
        root.addCaptureListener(object : InputListener() {
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                if (event.target !is TextField && keyboardFocus is TextField) {
                    Gdx.input.setOnscreenKeyboardVisible(false)
                    keyboardFocus = null
                }
                return false
            }
        })

        load()
    }


    /** Called when the game is created to load resources asynchronously. */
    open fun load() {
        assetManager.load<I18NBundle>(Resources.CORE_STRINGS_FILE)
    }

    /** Called when the asset manager is done loading. */
    open fun start() {
        coreSkin.add(Resources.CORE_STRINGS_NAME, assetManager.getAsset<I18NBundle>(Resources.CORE_STRINGS_FILE))
        SdfShader.load(coreSkin)
    }

    /** Load the [PCard] skin, containing standard playing cards sprites. */
    protected fun loadPCardSkin() {
        assetManager.load<Skin>(Resources.PCARD_SKIN, SkinLoader.SkinParameter(Resources.PCARD_SKIN_ATLAS))
    }

    private fun loadCoreSkin() {
        assetManager.load<Skin>(Resources.CORE_SKIN, SkinLoader.SkinParameter(Resources.CORE_SKIN_ATLAS))
        assetManager.finishLoading()
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

        if (!started) {
            if (assetManager.update()) {
                started = true
                start()
            }
            Gdx.graphics.requestRendering()
        }
    }

    override fun pause() {
        // Save all preferences when game is paused
        for (pref in prefs) {
            pref.save()
        }

        // Save game if started
        game?.save(Gdx.files.local(SAVED_GAME))
    }

    override fun resume() {

    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
        updateOffscreenFrameBuffer()
    }

    /** Hide menu, initialize game and start it. */
    protected fun initGame(game: CardGame) {
        this.game = game

        gameMenu?.shown = false

        gameLayout?.initGame(game)
        game.start()
    }

    override fun dispose() {
        super.dispose()
        assetManager.dispose()
        offscreenFbo.dispose()
        gameLayout?.dispose()
    }

    override fun keyDown(keyCode: Int): Boolean {
        if (keyCode == Input.Keys.BACK && gameMenu?.shown != false && gameMenu?.mainMenuShown != false) {
            // Close app on back key press if main menu is shown
            Gdx.app.exit()
        }

        return super.keyDown(keyCode)
    }

    private fun updateOffscreenFrameBuffer() {
        if (::offscreenFbo.isInitialized) {
            offscreenFbo.dispose()
        }
        offscreenFbo = FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.width, Gdx.graphics.height, false)
        offscreenFboRegion = TextureRegion(offscreenFbo.colorBufferTexture)
        offscreenFboRegion.flip(false, true)
    }


    companion object {
        const val SAVED_GAME = "saved-game.json"
    }

}
