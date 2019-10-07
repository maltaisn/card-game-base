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

package com.maltaisn.cardgame.pcard

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.maltaisn.cardgame.utils.Color
import ktx.style.add
import ktx.style.get


/**
 * Load the PCard skin into the core skin, adding regions and styles.
 * Project should use a Gradle task to copy the core assets to an `pcard/` folder under its assets.
 */
fun loadPCardSkin(assetManager: AssetManager, coreSkin: Skin) {
    coreSkin.apply {
        // Add texture atlas regions
        check(PCardRes.ATLAS in assetManager) { "PCard atlas must be loaded first." }
        addRegions(assetManager[PCardRes.ATLAS])

        // Add colors
        add("pcardRed", Color("#d40000"))
        add("pcardBlack", Color("#000000"))

        // Card style
        add(PCardStyle(
                cards = List(54) { get<Drawable>("pcard_$it") },
                back = get("pcard-back"),
                background = get("pcard-background"),
                hover = get("pcard-hover"),
                selection = get("pcard-selection"),
                slot = get("pcard-slot"),
                cardWidth = 240f,
                cardHeight = 336f,
                suitIcons = listOf(get("pcard-suit-heart"),
                        get("pcard-suit-spade"),
                        get("pcard-suit-diamond"),
                        get("pcard-suit-club"))
        ))
    }
}
