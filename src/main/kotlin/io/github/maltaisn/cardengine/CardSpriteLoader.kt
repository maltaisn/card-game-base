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

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import ktx.collections.lastIndex


/**
 * The base class for loading and accessing cards. Must be disposed.
 * The loader loads all the sprites when created.
 */
abstract class CardSpriteLoader(private val assetManager: AssetManager,
                                private val atlasPath: String) {

    // Sprites
    private lateinit var cardSprites: Array<Sprite>
    private lateinit var shadowSprite: Sprite
    private lateinit var backSprite: Sprite
    private lateinit var hoverSprite: Sprite
    private lateinit var selectionSprite: Sprite
    private lateinit var slotSprite: Sprite

    // Offset for drawing the shadow textures, relative to where the card sprite is drawn.
    abstract val shadowOffset: Float
    abstract val hoverOffset: Float

    init {
        assetManager.load(atlasPath, TextureAtlas::class.java)
    }

    /**
     * Must be called when asset manager is done loading, loads all the card sprites.
     */
    fun initialize() {
        // Load all sprites in atlas
        val atlas = assetManager.get(atlasPath, TextureAtlas::class.java)
        val regions = atlas.regions
        val cards = arrayOfNulls<Sprite>(54)
        for (i in 0..regions.lastIndex) {
            val region = regions[i]
            val sprite = Sprite(region)
            sprite.setOrigin(0f, 0f)
            when (region.name) {
                KEY_BACK -> backSprite = sprite
                KEY_SHADOW -> shadowSprite = sprite
                KEY_HOVER -> hoverSprite = sprite
                KEY_SELECTION -> selectionSprite = sprite
                KEY_SLOT -> slotSprite = sprite
                else -> cards[region.name.toInt()] = sprite
            }
        }

        cardSprites = cards.requireNoNulls()
    }

    /**
     * Get a sprite with a [value]. It can be a card value or a special value.
     */
    fun getSprite(value: Int) = when (value) {
        SHADOW -> shadowSprite
        BACK -> backSprite
        HOVER -> hoverSprite
        SELECTION -> selectionSprite
        SLOT -> slotSprite
        else -> cardSprites[value]
    }

    // Methods to get the size of a card sprite.
    // All card sprites are assumed to have the same dimensions.
    fun getCardWidth() = cardSprites.first().width

    fun getCardHeight() = cardSprites.first().height

    companion object {
        const val SHADOW = -1
        const val BACK = -2
        const val HOVER = -3
        const val SELECTION = -4
        const val SLOT = -5

        private const val KEY_BACK = "back"
        private const val KEY_SHADOW = "shadow"
        private const val KEY_HOVER = "hover"
        private const val KEY_SELECTION = "selection"
        private const val KEY_SLOT = "slot"
    }

}