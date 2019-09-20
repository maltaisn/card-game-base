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
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Container
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.maltaisn.cardgame.CardGameListener
import com.maltaisn.cardgame.markdown.Markdown
import com.maltaisn.cardgame.tests.core.ActionBarTest
import com.maltaisn.cardgame.tests.core.TestRes
import com.maltaisn.cardgame.tests.core.fontStyle
import com.maltaisn.cardgame.widget.CardGameLayout
import com.maltaisn.cardgame.widget.CoreIcons
import com.maltaisn.cardgame.widget.ScrollView
import com.maltaisn.cardgame.widget.markdown.MarkdownView
import com.maltaisn.cardgame.widget.menu.MenuItem
import com.maltaisn.cardgame.widget.menu.PagedSubMenu
import com.maltaisn.cardgame.widget.menu.SubMenu
import com.maltaisn.msdfgdx.widget.MsdfLabel
import ktx.assets.load
import ktx.log.info


/**
 * Test [PagedSubMenu] layout options and behavior with dummy content and items.
 */
class PagedSubMenuTest(listener: CardGameListener) : ActionBarTest(listener) {

    override fun load() {
        super.load()
        assetManager.load<Markdown>(TestRes.LOREM_IPSUM_MARKDOWN)
    }

    override fun layout(layout: CardGameLayout) {
        super.layout(layout)

        val continueItem = MenuItem(1000, "Continue",
                skin.getDrawable(CoreIcons.ARROW_RIGHT), SubMenu.ITEM_POS_BOTTOM)
        continueItem.checkable = false

        val rules: Markdown = assetManager[TestRes.LOREM_IPSUM_MARKDOWN]
        val rulesView = ScrollView(MarkdownView(skin, rules)
                .pad(0f, 40f, 0f, 40f))
        val rulesPage = TestPage(0, "Rules",
                skin.getDrawable(CoreIcons.BOOK), SubMenu.ITEM_POS_TOP, rulesView)

        val scoresView = Container(MsdfLabel("Nothing here.", skin,
                fontStyle(color = Color.BLACK, size = 60f)))
        val scoresPage = TestPage(1, "Scores",
                skin.getDrawable(CoreIcons.LIST), SubMenu.ITEM_POS_BOTTOM, scoresView)

        val menu = PagedSubMenu(skin).apply {
            title = "Scoreboard"
            itemClickListener = { info { "Item clicked: $it" } }
            addItems(rulesPage, scoresPage, continueItem)
            checkItem(scoresPage)
        }

        layout.centerTable.apply {
            getCell(btnTable).padBottom(0f)
            add(menu).grow()
        }

        menu.shown = true

        // Action buttons
        val showBtn = addActionBtn("Hide") {
            menu.shown = !menu.shown
            it.title = if (menu.shown) "Hide" else "Show"
        }
        menu.backArrowClickListener = {
            menu.shown = false
            showBtn.title = "Show"
            info { "Back arrow clicked" }
        }

        addTwoStateActionBtn("Hide page", "Show page") { _, shown ->
            rulesPage.shown = shown
        }

        addActionBtn("Check scores page") {
            menu.checkItem(scoresPage)
        }

        addToggleBtn("Debug") { _, debug ->
            menu.setDebug(debug, true)
        }
    }

    private open class TestPage(id: Int, title: String, icon: Drawable?, position: Int,
                                content: Actor? = null) :
            PagedSubMenu.Page(id, title, icon, position, content) {

        override fun onPageSelectionChanged(selected: Boolean) {
            info { "Page '$title' was ${if (selected) "selected" else "unselected"}" }
        }

    }

}
