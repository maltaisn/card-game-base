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

import com.maltaisn.cardgame.tests.core.SingleActionTest
import com.maltaisn.cardgame.widget.CardGameLayout
import com.maltaisn.cardgame.widget.menu.InGameMenu
import com.maltaisn.cardgame.widget.menu.MenuIcons
import com.maltaisn.cardgame.widget.menu.MenuItem
import ktx.log.info


/**
 * Test [InGameMenu] layout and animation.
 */
class InGameMenuTest : SingleActionTest() {

    override fun layout(layout: CardGameLayout) {
        super.layout(layout)

        //isDebugAll = true

        val menu = InGameMenu(coreSkin)

        menu.items += MenuItem(0, null, coreSkin.getDrawable(MenuIcons.CHEVRON_LEFT), InGameMenu.ITEM_POS_LEFT)
        menu.items += MenuItem(1, "Sort hand", coreSkin.getDrawable(MenuIcons.CARDS), InGameMenu.ITEM_POS_LEFT)
        menu.items += MenuItem(3, "Cheats", coreSkin.getDrawable(MenuIcons.BOOK), InGameMenu.ITEM_POS_RIGHT)
        menu.items += MenuItem(2, null, coreSkin.getDrawable(MenuIcons.LIST), InGameMenu.ITEM_POS_RIGHT)
        menu.invalidateLayout()

        menu.itemClickListener = {
            info { "Menu item clicked: $it" }
        }

        layout.gameLayer.centerTable.add(menu).grow()

        action = {
            menu.shown = !menu.shown
        }
    }

}