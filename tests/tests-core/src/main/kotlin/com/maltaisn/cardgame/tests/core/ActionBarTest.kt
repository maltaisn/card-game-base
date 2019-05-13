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

import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.maltaisn.cardgame.CardGameLayout
import com.maltaisn.cardgame.widget.FontStyle
import com.maltaisn.cardgame.widget.menu.MenuButton


/**
 * Base class for a test with a top toolbar with buttons to provide test actions.
 */
abstract class ActionBarTest : CardGameTest() {

    protected val btnTable = Table()

    override fun layout(layout: CardGameLayout) {
        super.layout(layout)

        layout.gameLayer.centerTable.add(btnTable).growX().colspan(100)
                .pad(25f, 20f, 25f, 20f).row()
    }

    /**
     * Add a button with a [title] and a click [action].
     */
    protected inline fun addActionBtn(title: String, crossinline action: (MenuButton) -> Unit) {
        val fontStyle = FontStyle(fontSize = 20f, drawShadow = true)
        val btn = MenuButton(coreSkin, fontStyle, title, null)
        btn.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                action(btn)
            }
        })
        btnTable.add(btn).grow().pad(0f, 5f, 0f, 5f).expand()
    }


}