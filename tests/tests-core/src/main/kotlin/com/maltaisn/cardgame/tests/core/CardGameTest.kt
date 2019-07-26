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

package com.maltaisn.cardgame.tests.core

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.maltaisn.cardgame.CardGameScreen
import com.maltaisn.cardgame.CoreRes
import com.maltaisn.cardgame.pcard.PCardStyle
import com.maltaisn.cardgame.widget.CardGameLayout
import ktx.assets.load
import ktx.style.get


abstract class CardGameTest : CardGameScreen() {

    protected lateinit var pcardStyle: PCardStyle


    override fun load() {
        assetManager.load<TextureAtlas>(CoreRes.PCARD_SKIN_ATLAS)
    }

    override fun start() {
        addSkin(CoreRes.PCARD_SKIN, CoreRes.PCARD_SKIN_ATLAS)
        pcardStyle = skin.get()

        val gameLayout = CardGameLayout(skin)
        addActor(gameLayout)
        layout(gameLayout)
    }

    override fun keyDown(keyCode: Int): Boolean {
        var handled = super.keyDown(keyCode)
        if (!handled && (keyCode == Input.Keys.BACK || keyCode == Input.Keys.ESCAPE)) {
            Gdx.app.exit()
            handled = true
        }
        return handled
    }

    open fun layout(layout: CardGameLayout) = Unit

}
