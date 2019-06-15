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
import com.badlogic.gdx.scenes.scene2d.ui.Container
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.maltaisn.cardgame.markdown.Markdown
import com.maltaisn.cardgame.tests.core.ActionBarTest
import com.maltaisn.cardgame.widget.CardGameLayout
import com.maltaisn.cardgame.widget.FontStyle
import com.maltaisn.cardgame.widget.ScrollView
import com.maltaisn.cardgame.widget.SdfLabel
import com.maltaisn.cardgame.widget.markdown.MarkdownView
import com.maltaisn.cardgame.widget.menu.MenuIcons
import com.maltaisn.cardgame.widget.menu.MenuItem
import com.maltaisn.cardgame.widget.menu.PagedSubMenu
import com.maltaisn.cardgame.widget.menu.SubMenu
import ktx.actors.setScrollFocus
import ktx.assets.load
import ktx.log.info


/**
 * Test [PagedSubMenu] layout options and behavior with dummy content and items.
 */
class PagedSubMenuTest : ActionBarTest() {

    override fun load() {
        super.load()
        assetManager.load<Markdown>("lorem-ipsum")
    }

    override fun layout(layout: CardGameLayout) {
        super.layout(layout)

        val continueItem = MenuItem(1000, "Continue",
                coreSkin.getDrawable(MenuIcons.ARROW_RIGHT), SubMenu.ITEM_POS_BOTTOM)
        continueItem.checkable = false

        val rules: Markdown = assetManager.get("lorem-ipsum")
        val rulesView = ScrollView(MarkdownView(coreSkin, rules).pad(0f, 20f, 0f, 20f))
        val rulesPage = object : TestPage(0, "Rules", coreSkin.getDrawable(MenuIcons.BOOK), SubMenu.ITEM_POS_TOP) {
            override fun onPageSelectionChanged(selected: Boolean) {
                super.onPageSelectionChanged(selected)
                rulesView.setScrollFocus(selected)
            }
        }
        rulesPage.content = rulesView

        val scoresView = Container(SdfLabel(coreSkin, FontStyle(fontColor = Color.BLACK, fontSize = 30f), "TODO!"))
        val scoresPage = TestPage(1, "Scores", coreSkin.getDrawable(MenuIcons.LIST), SubMenu.ITEM_POS_BOTTOM)
        scoresPage.content = scoresView

        val menu = PagedSubMenu(coreSkin).apply {
            title = "Scoreboard"
            addItem(rulesPage)
            addItem(scoresPage)
            addItem(continueItem)
            scoresPage.checked = true
        }

        layout.gameLayer.centerTable.apply {
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

        addToggleBtn("Debug") { _, debug ->
            menu.setDebug(debug, true)
        }
    }

    private open class TestPage(id: Int, title: String, icon: Drawable?, position: Int) :
            PagedSubMenu.Page(id, title, icon, position) {

        override fun onPageSelectionChanged(selected: Boolean) {
            info { "Page '$title' was ${if (selected) "selected" else "unselected"}" }
        }

    }

}
