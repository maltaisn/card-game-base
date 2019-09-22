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
import com.maltaisn.cardgame.CardGameListener
import com.maltaisn.cardgame.tests.core.ActionBarTest
import com.maltaisn.cardgame.tests.core.fontStyle
import com.maltaisn.cardgame.widget.CardGameLayout
import com.maltaisn.cardgame.widget.CoreIcons
import com.maltaisn.cardgame.widget.menu.MenuButton
import com.maltaisn.cardgame.widget.menu.MenuButton.AnchorSide
import ktx.actors.onClick
import ktx.log.info


/**
 * Test [MenuButton] options and layout.
 */
class MenuButtonTest(listener: CardGameListener) : ActionBarTest(listener) {

    override fun layout(layout: CardGameLayout) {
        super.layout(layout)

        val fontStyle = fontStyle(
                weight = 0.1f,
                size = 48f,
                color = Color.WHITE,
                shadowColor = Color.BLACK)

        val btn = MenuButton(skin, fontStyle,
                "Menu button", skin.getDrawable(CoreIcons.COIN)).apply {
            add(iconImage).size(64f).padBottom(10f).row()
            add(titleLabel).row()
            onClick { info { "Menu button clicked" } }
        }

        val btnCell = layout.centerTable.add(btn).expand()

        // Action buttons
        btnFontStyle.size = 24f
        addTwoStateActionBtn("Disable", "Enable") { _, enabled ->
            btn.enabled = enabled
        }
        addEnumBtn("Anchor side", AnchorSide.values().toList()) { _, value ->
            btn.anchorSide = value
            btnCell.align(when (value) {
                AnchorSide.NONE -> Align.center
                AnchorSide.TOP -> Align.top
                AnchorSide.LEFT -> Align.left
                AnchorSide.BOTTOM -> Align.bottom
                AnchorSide.RIGHT -> Align.right
            })
        }
        addToggleBtn("Debug") { _, debug ->
            btn.setDebug(debug, true)
        }
    }

}
