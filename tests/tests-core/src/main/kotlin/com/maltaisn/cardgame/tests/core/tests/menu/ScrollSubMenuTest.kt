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

import com.maltaisn.cardgame.CardGameListener
import com.maltaisn.cardgame.markdown.Markdown
import com.maltaisn.cardgame.tests.core.ActionBarTest
import com.maltaisn.cardgame.tests.core.TestRes
import com.maltaisn.cardgame.widget.CardGameLayout
import com.maltaisn.cardgame.widget.CoreIcons
import com.maltaisn.cardgame.widget.markdown.MarkdownView
import com.maltaisn.cardgame.widget.menu.MenuItem
import com.maltaisn.cardgame.widget.menu.ScrollSubMenu
import com.maltaisn.cardgame.widget.menu.SubMenu
import ktx.assets.load
import ktx.log.info


/**
 * Test [ScrollSubMenu] layout options and behavior with dummy content and items.
 */
class ScrollSubMenuTest(listener: CardGameListener) : ActionBarTest(listener) {

    override fun load() {
        super.load()
        assetManager.load<Markdown>(TestRes.LOREM_IPSUM_MARKDOWN)
    }

    override fun layout(layout: CardGameLayout) {
        super.layout(layout)

        val menuItems = listOf(MenuItem(0, "First item", skin.getDrawable(CoreIcons.BOOK), SubMenu.ITEM_POS_TOP),
                MenuItem(1, "No icon", null, SubMenu.ITEM_POS_TOP),
                MenuItem(2, "Bottom aligned and long title", skin.getDrawable(CoreIcons.CHEVRON_RIGHT), SubMenu.ITEM_POS_BOTTOM))
        menuItems[2].checkable = false

        val markdown: Markdown = assetManager[TestRes.LOREM_IPSUM_MARKDOWN]
        val mdView = MarkdownView(skin, markdown)

        val menu = ScrollSubMenu(skin).apply {
            itemClickListener = { info { "Item clicked: $it" } }
            title = "Sub menu test"
            scrollContent.actor = mdView

            for (item in menuItems) {
                addItem(item)
            }

            scrollToTop()
        }

        layout.centerTable.apply {
            getCell(btnTable).padBottom(0f)
            add(menu).grow()
        }

        menu.shown = true

        // Action buttons
        val showBtn = addActionBtn("Hide") { btn ->
            menu.shown = !menu.shown
            btn.title = if (menu.shown) "Hide" else "Show"
        }
        menu.backArrowClickListener = {
            menu.shown = false
            showBtn.title = "Show"
            info { "Back arrow clicked" }
        }

        addActionBtn("Scroll to top") {
            menu.scrollToTop()
        }

        addActionBtn("Check 2nd item") {
            menu.checkItem(menuItems[1])
        }

        var option = 0
        addActionBtn("Right side") { btn ->
            option = (option + 1) % 3
            when (option) {
                0 -> {
                    for (item in menuItems) {
                        menu.addItem(item)
                    }
                    menu.menuPosition = SubMenu.MenuPosition.LEFT
                    btn.title = "Right side"
                }
                1 -> {
                    menu.menuPosition = SubMenu.MenuPosition.RIGHT
                    btn.title = "No items"
                }
                2 -> {
                    menu.clearItems()
                    btn.title = "Left side"
                }
            }
        }

        addTwoStateActionBtn("Hide last", "Show last") { _, shown ->
            menuItems.last().shown = shown
        }

        addToggleBtn("Debug") { _, debug ->
            menu.setDebug(debug, true)
        }
    }

}
