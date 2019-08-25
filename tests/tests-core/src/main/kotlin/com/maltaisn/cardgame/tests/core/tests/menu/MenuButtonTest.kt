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

package com.maltaisn.cardgame.tests.core.tests.menu

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.Align
import com.maltaisn.cardgame.tests.core.ActionBarTest
import com.maltaisn.cardgame.utils.defaultSize
import com.maltaisn.cardgame.widget.CardGameLayout
import com.maltaisn.cardgame.widget.CoreIcons
import com.maltaisn.cardgame.widget.menu.MenuButton
import com.maltaisn.cardgame.widget.menu.MenuButton.Side
import com.maltaisn.cardgame.widget.text.FontStyle
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
        val btnCell = layout.centerTable.add(btn).expand()

        // Action buttons
        btnFontSize = 24f
        addTwoStateActionBtn("Disable", "Enable") { _, enabled ->
            btn.enabled = enabled
        }
        addToggleBtn("Title shown") { _, shown ->
            btn.title = if (shown) "Menu button" else null
        }
        addToggleBtn("Icon shown") { _, shown ->
            btn.icon = if (shown) skin.getDrawable(CoreIcons.CARDS) else null
        }
        addEnumBtn("Anchor side", Side.values().toList()) { _, value ->
            btn.anchorSide = value
            btnCell.align(when (value) {
                Side.NONE -> Align.center
                Side.TOP -> Align.top
                Side.LEFT -> Align.left
                Side.BOTTOM -> Align.bottom
                Side.RIGHT -> Align.right
            })
        }
        addEnumBtn("Icon side", Side.values().toList()) { _, value ->
            btn.iconSide = value
        }
        addValueBtn("Icon size", 32f, 128f, btn.iconSize, 8f) { _, size, _ ->
            btn.iconSize = size
        }
        addEnumBtn("Title align", listOf(Align.center, Align.top, Align.left, Align.bottom, Align.right),
                listOf("CENTER", "TOP", "LEFT", "BOTTOM", "RIGHT")) { _, align ->
            btn.titleAlign = align
        }
        addToggleBtn("Constrained size", startState = true) { _, constrained ->
            if (constrained) {
                btnCell.fill(0f, 0f).defaultSize()
            } else {
                btnCell.size(500f, 300f).fill()
            }
            btn.invalidateHierarchy()
        }
        addToggleBtn("Debug") { _, debug ->
            btn.setDebug(debug, true)
        }
    }

    companion object {
        private val BTN_SIDES = listOf(Side.NONE, Side.TOP, Side.LEFT, Side.BOTTOM, Side.RIGHT)
    }

}
