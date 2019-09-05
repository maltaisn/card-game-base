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
import com.badlogic.gdx.scenes.scene2d.ui.Container
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Value
import com.badlogic.gdx.utils.Align
import com.maltaisn.cardgame.tests.core.ActionBarTest
import com.maltaisn.cardgame.tests.core.fontStyle
import com.maltaisn.cardgame.widget.CardGameLayout
import com.maltaisn.cardgame.widget.CoreIcons
import com.maltaisn.cardgame.widget.menu.MenuDrawer
import com.maltaisn.cardgame.widget.menu.MenuDrawerList
import com.maltaisn.msdfgdx.widget.MsdfLabel
import ktx.log.info
import kotlin.math.max
import kotlin.math.min


/**
 * Test for [MenuDrawer]: title, drawer width, dismissal and content.
 * Also tests [MenuDrawerList] widget and its selection listener.
 */
class MenuDrawerTest : ActionBarTest() {

    override fun layout(layout: CardGameLayout) {
        super.layout(layout)

        val drawer = MenuDrawer(skin)
        drawer.backBtnText = "Back"

        var drawerWidth = 0.5f
        drawer.drawerWidth = Value.percentWidth(drawerWidth, drawer)

        val image = Image(skin.getDrawable(CoreIcons.INFO))
        image.color = Color.BLACK
        val imageContent = Container(image).size(300f, 300f)

        val text = MsdfLabel("""
            Lorem ipsum dolor sit amet, consectetur adipiscing elit.
            Nulla consectetur nunc scelerisque, pretium urna eu, maximus
            augue. In lacinia lobortis enim, quis sodales turpis pulvinar
            iaculis. Praesent placerat cursus fringilla. Etiam non dolor
            tellus. Aenean erat odio, fringilla in interdum et, dapibus at
            nulla. Donec massa velit, elementum id rhoncus sed, sagittis
            eget velit.
        """.trimIndent().replace('\n', ' '),
                skin, fontStyle(size = 44f, color = Color.BLACK))
        text.setWrap(true)
        val textContent = Container(text).pad(0f, 60f, 0f, 60f).align(Align.top).fillX()

        val listContent = MenuDrawerList(skin).apply {
            items = (1..10).map { "Item $it" }
            selectionChangeListener = {
                info { "List content item clicked: ${items[it]}" }
            }
        }

        drawer.content.actor = textContent
        drawer.content.padBottom(60f)

        // Action buttons
        addActionBtn("Show") { drawer.shown = true }
        addTwoStateActionBtn("Show title", "Hide title") { _, hidden ->
            drawer.title = if (hidden) null else "Menu drawer title"
        }

        addActionBtn("Image") { drawer.content.actor = imageContent }
        addActionBtn("Text") { drawer.content.actor = textContent }
        addActionBtn("List") { drawer.content.actor = listContent }

        addActionBtn("Width -") {
            drawerWidth = max(drawerWidth - 0.1f, 0.2f)
            drawer.drawerWidth = Value.percentWidth(drawerWidth, drawer)
        }
        addActionBtn("Width +") {
            drawerWidth = min(drawerWidth + 0.1f, 0.9f)
            drawer.drawerWidth = Value.percentWidth(drawerWidth, drawer)
        }

        // Do the layout
        layout.centerTable.apply {
            clearChildren()

            val btnContainer = Container(btnTable).align(Align.top)
                    .pad(50f, 40f, 50f, 40f).fillX()
            stack(btnContainer, drawer).grow()
        }
    }
}
