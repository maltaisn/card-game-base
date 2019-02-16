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
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.Actor
import io.github.maltaisn.cardengine.widget.AnimationLayer
import io.github.maltaisn.cardengine.widget.GameLayer


abstract class CardGameScreen(game: CardGame) : BaseScreen(game) {

    val assetManager = AssetManager()

    val gameLayer = GameLayer(assetManager)
    val animationLayer = AnimationLayer()

    init {
        addActor(CardGameContainer(gameLayer, animationLayer))
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