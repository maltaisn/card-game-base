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
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.utils.I18NBundle
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.maltaisn.cardgame.markdown.MdLoader
import com.maltaisn.cardgame.prefs.GamePrefs
import com.maltaisn.cardgame.prefs.GamePrefsLoader
import com.maltaisn.cardgame.widget.text.SdfShader
import ktx.assets.file
import ktx.assets.getAsset
import ktx.assets.load
import ktx.assets.setLoader


open class CardGameScreen : Stage(ExtendViewport(1920f, 1080f)), Screen {

    val assetManager = AssetManager()

    /**
     * The skin containing all core styles.
     * Additional skins can be added with [addSkin].
     */
    val skin: Skin

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
        assetManager.load<Skin>(CoreRes.CORE_SKIN, SkinLoader.SkinParameter(CoreRes.CORE_SKIN_ATLAS))
        assetManager.finishLoading()
        skin = assetManager.get(CoreRes.CORE_SKIN)

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

        assetManager.load<I18NBundle>(CoreRes.CORE_STRINGS_FILE)
        load()
    }


    /**
     * Called when the game is created.
     * Good place to load the resources asynchronously with the asset manager.
     */
    open fun load() = Unit

    /**
     * Called when the asset manager is done loading.
     */
    open fun start() = Unit

    /**
     * Add the styles and regions of a skin to the core skin.
     * This must be called during [start] and `assetManager.load<TextureAtlas>(atlasFile)`
     * must have been called before hand if the skin has an atlas.
     */
    protected fun addSkin(skinFile: String, atlasFile: String? = null) {
        if (atlasFile != null) {
            val atlas: TextureAtlas = assetManager.get(atlasFile)
            skin.addRegions(atlas)
        }
        skin.load(file(skinFile))
    }

    override fun show() {
        Gdx.input.inputProcessor = this
        Gdx.input.setCatchKey(Input.Keys.BACK, true)
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

                skin.add(CoreRes.CORE_STRINGS_NAME,
                        assetManager.getAsset<I18NBundle>(CoreRes.CORE_STRINGS_FILE))
                SdfShader.getShader(skin)
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
    }

    override fun resume() = Unit

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
        updateOffscreenFrameBuffer()
    }

    override fun dispose() {
        super.dispose()
        assetManager.dispose()
        offscreenFbo.dispose()

        // Clear all remaining preference listeners.
        // This is usually done by then but just to make sure.
        for (gamePrefs in prefs) {
            gamePrefs.clearAllListeners()
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
