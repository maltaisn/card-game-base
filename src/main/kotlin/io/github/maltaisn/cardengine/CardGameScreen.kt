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
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.SkinLoader
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import io.github.maltaisn.cardengine.widget.AnimationLayer
import io.github.maltaisn.cardengine.widget.GameLayer
import io.github.maltaisn.cardengine.widget.PopupLayer


abstract class CardGameScreen(game: CardGame) : BaseScreen(game) {

    val assetManager = AssetManager()
    val coreSkin: Skin

    val gameLayer: GameLayer
    val animationLayer: AnimationLayer
    val popupLayer: PopupLayer

    init {
        assetManager.load(Resources.CORE_SKIN_ATLAS, TextureAtlas::class.java)
        assetManager.load(Resources.CORE_SKIN, Skin::class.java,
                SkinLoader.SkinParameter(Resources.CORE_SKIN_ATLAS))
        assetManager.finishLoading()

        coreSkin = assetManager.get(Resources.CORE_SKIN, Skin::class.java)
        gameLayer = GameLayer(coreSkin)
        animationLayer = AnimationLayer()
        popupLayer = PopupLayer()

        addActor(CardGameContainer(gameLayer, animationLayer, popupLayer))
    }

    final override fun addActor(actor: Actor?) = super.addActor(actor)

    override fun render(delta: Float) {
        // Clear with black
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        super.render(delta)
    }

    override fun dispose() {
        super.dispose()
        assetManager.dispose()
    }

}