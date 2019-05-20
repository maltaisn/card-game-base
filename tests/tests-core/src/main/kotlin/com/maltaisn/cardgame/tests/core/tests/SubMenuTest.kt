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
import com.maltaisn.cardgame.widget.markdown.MarkdownView
import com.maltaisn.cardgame.widget.menu.MenuIcons
import com.maltaisn.cardgame.widget.menu.MenuItem
import com.maltaisn.cardgame.widget.menu.SubMenu
import ktx.actors.setKeyboardFocus
import ktx.assets.load
import ktx.log.info


/**
 * Test [SubMenu] layout options with dummy content and items
 */
class SubMenuTest : ActionBarTest() {

    override fun load() {
        super.load()
        assetManager.load<Markdown>("lorem-ipsum")
    }

    override fun layout(layout: CardGameLayout) {
        super.layout(layout)

        // FIXME if item has a title too long, menu layout breaks.
        //  Add an option in MenuButton to allow ellipsis.

        val menuItems = listOf(MenuItem(0, "First item", coreSkin.getDrawable(MenuIcons.BOOK), SubMenu.ITEM_POS_TOP),
                MenuItem(1, "No icon", null, SubMenu.ITEM_POS_TOP),
                MenuItem(2, "Bottom aligned", coreSkin.getDrawable(MenuIcons.CHEVRON_RIGHT), SubMenu.ITEM_POS_BOTTOM))

        val markdown = assetManager.get<Markdown>("lorem-ipsum")
        val menu = SubMenu(coreSkin).apply {
            backArrowClickListener = { info { "Back arrow clicked" } }
            itemClickListener = { info { "Item checked: $it" } }
            title = "Sub menu test"
            items += menuItems
            content.actor = MarkdownView(coreSkin, markdown)
            invalidateLayout()
        }
        menu.shown = true
        menu.setKeyboardFocus()

        layout.gameLayer.centerTable.apply {
            getCell(btnTable).padBottom(0f)
            add(menu).grow()
        }

        // Action buttons
        addTwoStateActionBtn("Hide", "Show") { _, shown ->
            menu.shown = shown
        }

        var option = 0
        addActionBtn("Right side") {
            option = (option + 1) % 3
            when (option) {
                0 -> {
                    menu.items += menuItems
                    menu.menuPosition = SubMenu.MenuPosition.LEFT
                    it.title = "Right side"
                }
                1 -> {
                    menu.menuPosition = SubMenu.MenuPosition.RIGHT
                    it.title = "No items"
                }
                2 -> {
                    menu.items.clear()
                    it.title = "Left side"
                }
            }
            menu.invalidateLayout()
        }

        addToggleBtn("Debug") { _, debug ->
            menu.setDebug(debug, true)
        }
    }

}
