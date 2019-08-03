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

import com.maltaisn.cardgame.markdown.Markdown
import com.maltaisn.cardgame.tests.core.ActionBarTest
import com.maltaisn.cardgame.widget.CardGameLayout
import com.maltaisn.cardgame.widget.menu.MainMenu
import com.maltaisn.cardgame.widget.menu.MenuIcons
import com.maltaisn.cardgame.widget.menu.MenuItem
import ktx.assets.load
import ktx.log.info


/**
 * Test [MainMenu] layout options and behavior with dummy content and items.
 */
class MainMenuTest : ActionBarTest() {

    override fun load() {
        super.load()
        assetManager.load<Markdown>("lorem-ipsum")
    }

    override fun layout(layout: CardGameLayout) {
        super.layout(layout)

        val menu = MainMenu(skin)
        menu.addItems(MenuItem(0, "Settings", this@MainMenuTest.skin.getDrawable(MenuIcons.SETTINGS), MainMenu.ITEM_POS_TOP),
                MenuItem(1, "Rules", this@MainMenuTest.skin.getDrawable(MenuIcons.BOOK), MainMenu.ITEM_POS_TOP),
                MenuItem(2, "Stats", this@MainMenuTest.skin.getDrawable(MenuIcons.LIST), MainMenu.ITEM_POS_TOP),
                MenuItem(3, "New game", this@MainMenuTest.skin.getDrawable(MenuIcons.CARDS), MainMenu.ITEM_POS_BOTTOM),
                MenuItem(4, "Continue", this@MainMenuTest.skin.getDrawable(MenuIcons.ARROW_RIGHT), MainMenu.ITEM_POS_BOTTOM))

        menu.itemClickListener = {
            info { "Menu item clicked: $it" }
        }

        layout.centerTable.apply {
            getCell(btnTable).padBottom(40f)
            add(menu).grow()
        }

        menu.shown = true

        // Action buttons
        addActionBtn("Hide") {
            menu.shown = !menu.shown
            it.title = if (menu.shown) "Hide" else "Show"
        }
        addTwoStateActionBtn("Hide item", "Show item") { _, shown ->
            menu.items[1].shown = shown
        }
        addTwoStateActionBtn("Disable item", "Enable item") { _, enabled ->
            menu.items[4].enabled = enabled
        }
        addToggleBtn("Debug") { _, debug ->
            menu.setDebug(debug, true)
        }
    }

}
