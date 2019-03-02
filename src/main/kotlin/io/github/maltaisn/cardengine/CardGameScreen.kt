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

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.SkinLoader
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.viewport.ExtendViewport
import io.github.maltaisn.cardengine.widget.AnimationLayer
import io.github.maltaisn.cardengine.widget.GameLayer
import io.github.maltaisn.cardengine.widget.PopupLayer
import ktx.actors.plusAssign
import ktx.assets.getAsset
import ktx.assets.load


abstract class CardGameScreen(game: CardGame) :
        Stage(ExtendViewport(960f, 540f)), Screen {

    val assetManager = AssetManager()
    val coreSkin: Skin

    /** Layer where the game takes place, contains the card containers. */
    val gameLayer: GameLayer
    /** Layer where the card are placed when animated. */
    val animationLayer: AnimationLayer
    /** Layer where popups are shown. */
    val popupLayer: PopupLayer

    // This frame buffer is used to draw sprites offscreen, not on the screen batch.
    // The buffer can then be rendered to screen, allowing uniform transparency for example.
    lateinit var offscreenFbo: FrameBuffer
    lateinit var offscreenFboRegion: TextureRegion

    init {
        assetManager.load<TextureAtlas>(Resources.CORE_SKIN_ATLAS)
        assetManager.load<Skin>(Resources.CORE_SKIN, SkinLoader.SkinParameter(Resources.CORE_SKIN_ATLAS))
        assetManager.finishLoading()

        coreSkin = assetManager.getAsset(Resources.CORE_SKIN)
        gameLayer = GameLayer(coreSkin)
        animationLayer = AnimationLayer()
        popupLayer = PopupLayer()

        this += CardGameContainer(gameLayer, animationLayer, popupLayer)

        updateOffscreenFrameBuffer()
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