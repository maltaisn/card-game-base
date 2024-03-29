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
import com.badlogic.gdx.assets.loaders.I18NBundleLoader.I18NBundleParameter
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver
import com.badlogic.gdx.audio.Sound
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
import com.maltaisn.cardgame.widget.MsdfTextField
import com.maltaisn.msdfgdx.MsdfFont
import com.maltaisn.msdfgdx.MsdfFontLoader
import com.maltaisn.msdfgdx.MsdfShader
import ktx.assets.load
import ktx.assets.loadOnDemand
import ktx.assets.setLoader
import ktx.style.add
import java.util.*


/**
 * The stage and screen for a card game.
 * @property locale The game locale, used to load the core strings.
 */
open class CardGameScreen<T : CardGameListener>(val locale: Locale, val listener: T) :
        Stage(ExtendViewport(1920f, 1080f)), Screen {

    val assetManager = AssetManager()

    /**
     * The skin containing all core styles.
     * Additional skins can be added with [addSkin].
     */
    val skin: Skin

    // This frame buffer is used to draw sprites offscreen, not on the screen batch.
    // The buffer can then be rendered to screen, allowing uniform transparency for example.
    lateinit var offscreenFbo: FrameBuffer
        private set
    lateinit var offscreenFboRegion: TextureRegion
        private set

    protected var started = false


    init {
        actionsRequestRendering = true
        Gdx.graphics.isContinuousRendering = false

        // Register asset loaders
        val fileResolver = InternalFileHandleResolver()
        assetManager.apply {
            setLoader(MdLoader(fileResolver))
            setLoader(MsdfFontLoader(fileResolver))
        }

        // Load core skin and strings
        skin = buildCoreSkin(assetManager.loadOnDemand<TextureAtlas>(CoreRes.SKIN_ATLAS).asset)
        skin.add(CoreRes.CORE_STRINGS_NAME, assetManager.loadOnDemand<I18NBundle>(
                CoreRes.CORE_STRINGS_FILE, I18NBundleParameter(locale)).asset)

        // Add font and shader immediately.
        skin.add(MsdfShader())
        skin.add(assetManager.loadOnDemand<MsdfFont>(CoreRes.FONT).asset)

        // Load other assets
        load()

        // Add listener to unfocus text field when clicked outside
        root.addCaptureListener(object : InputListener() {
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                if (event.target !is TextField && keyboardFocus is TextField) {
                    Gdx.input.setOnscreenKeyboardVisible(false)
                    keyboardFocus = null
                }
                return false
            }
        })
    }


    /**
     * Called when the game is created.
     * Good place to load the resources asynchronously with the asset manager.
     */
    open fun load() {
        // Load sounds
        for (file in CoreRes.SOUNDS.values) {
            assetManager.load<Sound>(file)
        }
    }

    /**
     * Called when the asset manager is done loading.
     */
    open fun start() {
        // Add sounds to skin
        for ((sound, file) in CoreRes.SOUNDS) {
            skin.add(sound, assetManager.get<Sound>(file), Sound::class.java)
        }
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
                start()
            }
            Gdx.graphics.requestRendering()
        }
    }

    override fun pause() = Unit

    override fun resume() = Unit

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
     * Called when a [MsdfTextField] is focused.
     * Backend-dependent behavior is delegated to game listener.
     */
    fun onTextInput(textField: MsdfTextField) {
        if (listener.isTextInputDelegated) {
            listener.onTextInput(textField.text, textField.inputTitle) { text ->
                // Filter the text and set it on text field.
                textField.text = text
                keyboardFocus = null
                Gdx.graphics.requestRendering()
            }
        }
    }

    private fun updateOffscreenFrameBuffer() {
        val width = Gdx.graphics.width
        val height = Gdx.graphics.height

        // Width and height will be 0 on desktop when minimizing the window.
        if (Gdx.graphics.width == 0) return

        if (::offscreenFbo.isInitialized) {
            offscreenFbo.dispose()
        }
        offscreenFbo = try {
            FrameBuffer(Pixmap.Format.RGBA8888, width, height, false)
        } catch (e: Exception) {
            FrameBuffer(Pixmap.Format.RGBA4444, width, height, false)
        }

        offscreenFboRegion = TextureRegion(offscreenFbo.colorBufferTexture)
        offscreenFboRegion.flip(false, true)
    }

}
