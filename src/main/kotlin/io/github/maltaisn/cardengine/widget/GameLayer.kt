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

package io.github.maltaisn.cardengine.widget

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.ui.Table
import io.github.maltaisn.cardengine.Resources


class GameLayer(private val assetManager: AssetManager) : Table() {

    private lateinit var backgroundTexture: Texture

    init {
        assetManager.load(Resources.BACKGROUND, Texture::class.java)
    }

    override fun drawChildren(batch: Batch, parentAlpha: Float) {
        if (::backgroundTexture.isInitialized) {
            batch.draw(backgroundTexture, 0f, 0f, width, height)
        } else {
            if (assetManager.isLoaded(Resources.BACKGROUND)) {
                backgroundTexture = assetManager.get(Resources.BACKGROUND)
            }
        }

        super.drawChildren(batch, parentAlpha)
    }

}