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

package com.maltaisn.cardgame.tests.core.tests

import com.badlogic.gdx.graphics.Color
import com.maltaisn.cardgame.tests.core.CardGameTest
import com.maltaisn.cardgame.widget.CardGameLayout
import com.maltaisn.cardgame.widget.FontStyle
import com.maltaisn.cardgame.widget.menu.MenuButton
import com.maltaisn.cardgame.widget.menu.MenuIcons
import ktx.actors.onClick


/**
 * Test [MenuButton] options and layout.
 */
class MenuButtonTest : CardGameTest() {

    override fun layout(layout: CardGameLayout) {
        super.layout(layout)

        val table = layout.gameLayer.centerTable

        val fontStyle = FontStyle(
                bold = true,
                fontSize = 24f,
                allCaps = true,
                fontColor = Color.WHITE,
                drawShadow = true,
                shadowColor = Color.BLACK)

        // Side buttons
        val topBtn = MenuButton(coreSkin, fontStyle, "Top button",
                coreSkin.getDrawable(MenuIcons.BOOK)).apply {
            anchorSide = MenuButton.Side.TOP
            iconSide = MenuButton.Side.LEFT
            iconSize = 24f
        }
        val bottomBtn = MenuButton(coreSkin, fontStyle, "Bottom button",
                coreSkin.getDrawable(MenuIcons.CARDS)).apply {
            anchorSide = MenuButton.Side.BOTTOM
            iconSide = MenuButton.Side.BOTTOM
            iconSize = 32f
        }
        val leftBtn = MenuButton(coreSkin, fontStyle, "Left button", null).apply {
            anchorSide = MenuButton.Side.LEFT
        }
        val rightBtn = MenuButton(coreSkin, fontStyle, "Right\nbutton",
                coreSkin.getDrawable(MenuIcons.LIST)).apply {
            anchorSide = MenuButton.Side.RIGHT
            iconSide = MenuButton.Side.RIGHT
            iconSize = 64f
        }
        val sideBtns = listOf(topBtn, bottomBtn, leftBtn, rightBtn)

        // Center button
        val centerBtn = MenuButton(coreSkin, fontStyle, "Enable all", null).apply {
            anchorSide = MenuButton.Side.NONE
            iconSide = MenuButton.Side.RIGHT
            iconSize = 32f
        }
        centerBtn.onClick {
            // Enable or disable all
            val enabled = !topBtn.enabled
            for (btn in sideBtns) {
                btn.enabled = enabled
            }

            centerBtn.title = if (enabled) "Enable all" else "Disable all"
        }

        // Add buttons to table
        table.add()
        table.add(topBtn).height(100f).expandX()
        table.add().row()
        table.add(leftBtn).height(100f)
        table.add(centerBtn).height(80f).expand()
        table.add(rightBtn).height(100f).row()
        table.add()
        table.add(bottomBtn).expandX()
        table.add().row()
    }

}