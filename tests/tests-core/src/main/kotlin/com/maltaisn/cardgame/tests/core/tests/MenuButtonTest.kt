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
import com.badlogic.gdx.utils.Align
import com.maltaisn.cardgame.tests.core.ActionBarTest
import com.maltaisn.cardgame.widget.CardGameLayout
import com.maltaisn.cardgame.widget.FontStyle
import com.maltaisn.cardgame.widget.menu.MenuButton
import com.maltaisn.cardgame.widget.menu.MenuButton.Side
import com.maltaisn.cardgame.widget.menu.MenuIcons
import ktx.actors.onClick
import ktx.log.info


/**
 * Test [MenuButton] options and layout.
 */
class MenuButtonTest : ActionBarTest() {

    override fun layout(layout: CardGameLayout) {
        super.layout(layout)

        val fontStyle = FontStyle(
                bold = true,
                fontSize = 48f,
                fontColor = Color.WHITE,
                drawShadow = true,
                shadowColor = Color.BLACK)

        val btn = MenuButton(skin, fontStyle)
        btn.onClick {
            info { "Menu button clicked" }
        }
        val btnCell = layout.gameLayer.centerTable.add(btn).expand()

        // Action buttons
        addTwoStateActionBtn("Disable", "Enable") { _, enabled ->
            btn.enabled = enabled
        }
        addToggleBtn("Title shown") { _, shown ->
            btn.title = if (shown) "Menu button" else null
        }
        addToggleBtn("Icon shown") { _, shown ->
            btn.icon = if (shown) skin.getDrawable(MenuIcons.CARDS) else null
        }

        var anchorSideIndex = 0
        addActionBtn("Anchor side: ${btn.anchorSide}") {
            anchorSideIndex = (anchorSideIndex + 1) % BTN_SIDES.size
            btn.anchorSide = BTN_SIDES[anchorSideIndex]
            it.title = "Anchor side: ${btn.anchorSide}"

            btnCell.align(when (btn.anchorSide) {
                Side.NONE -> Align.center
                Side.TOP -> Align.top
                Side.LEFT -> Align.left
                Side.BOTTOM -> Align.bottom
                Side.RIGHT -> Align.right
            })
        }

        var iconSideIndex = 0
        addActionBtn("Icon side: ${btn.iconSide}") {
            iconSideIndex = (iconSideIndex + 1) % BTN_SIDES.size
            btn.iconSide = BTN_SIDES[iconSideIndex]
            it.title = "Icon side: ${btn.iconSide}"
        }

        addActionBtn("Icon size: ${btn.iconSize.toInt()}") {
            btn.iconSize = (btn.iconSize - 24f) % 104f + 32f
            it.title = "Icon size: ${btn.iconSize.toInt()}"
        }
    }

    companion object {
        private val BTN_SIDES = listOf(Side.NONE, Side.TOP, Side.LEFT, Side.BOTTOM, Side.RIGHT)
    }

}
