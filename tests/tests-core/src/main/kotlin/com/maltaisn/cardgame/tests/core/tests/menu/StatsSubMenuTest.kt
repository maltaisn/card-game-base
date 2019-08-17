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

import com.maltaisn.cardgame.stats.Statistics
import com.maltaisn.cardgame.tests.core.ActionBarTest
import com.maltaisn.cardgame.tests.core.TestRes
import com.maltaisn.cardgame.widget.CardGameLayout
import com.maltaisn.cardgame.widget.stats.StatsSubMenu
import ktx.assets.load
import ktx.log.info
import kotlin.random.Random


class StatsSubMenuTest : ActionBarTest() {

    override fun load() {
        super.load()
        assetManager.load<Statistics>(TestRes.STATS)
    }

    override fun layout(layout: CardGameLayout) {
        super.layout(layout)

        val stats: Statistics = assetManager[TestRes.STATS]

        val menu = StatsSubMenu(skin).apply {
            this.stats = stats
            itemClickListener = { info { "Item clicked: $it" } }
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
        addActionBtn("Change all values") {
            stats.apply {
                val v = menu.checkedItem?.id ?: return@addActionBtn
                getNumber("tricksTaken")[v] += Random.nextInt(8)
                getNumber("roundsPlayed")[v]++
                getNumber("gamesPlayed")[v] += Random.nextInt(1, 4)
                getNumber("gamesWon")[v]++
                getNumber("tradeCount_internal")[v] += Random.nextInt(2)
                getNumber("totalPoints")[v] += Random.nextInt(10)
                save()
            }
            menu.refresh()
        }
        addTwoStateActionBtn("Unset stats", "Set stats") { _, set ->
            menu.stats = if (set) stats else null
        }
        addActionBtn("Check 2nd variant") {
            menu.checkItem(menu.items[1])
        }
        addToggleBtn("Debug") { _, debug ->
            menu.setDebug(debug, true)
        }
    }

}
