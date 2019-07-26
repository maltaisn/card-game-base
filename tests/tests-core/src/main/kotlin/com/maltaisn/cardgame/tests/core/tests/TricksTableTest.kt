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

import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.maltaisn.cardgame.game.drawBottom
import com.maltaisn.cardgame.pcard.PCard
import com.maltaisn.cardgame.tests.core.SubmenuContentTest
import com.maltaisn.cardgame.widget.CardGameLayout
import com.maltaisn.cardgame.widget.table.TricksTable
import kotlin.random.Random


class TricksTableTest : SubmenuContentTest() {

    override fun layoutContent(layout: CardGameLayout, content: Table) {
        val table = TricksTable(skin, pcardStyle, 4)
        table.headers = listOf("South", "East", "North", "West")

        val cards = mutableListOf<List<TricksTable.TrickCard>>()
        table.cards = cards
        table.cardSize = 240f

        fun addTrick() {
            val trick = PCard.fullDecks(shuffled = true).drawBottom(4)
            val checkedPos = Random.nextInt(trick.size)
            cards += List(trick.size) { TricksTable.TrickCard(trick[it], it == checkedPos) }
            table.cellAdapter?.notifyChanged()
        }

        repeat(5) { addTrick() }

        // Do the layout
        content.add(table).grow().pad(40f, 300f, 40f, 300f)

        // Action buttons
        addActionBtn("Add trick") {
            addTrick()
        }
        addActionBtn("Size: ${table.cardSize.toInt()}") {
            table.cardSize = (table.cardSize - 180f) % 100f + 200f
            it.title = "Size: ${table.cardSize.toInt()}"
        }
        addToggleBtn("Debug") { _, debug ->
            table.setDebug(debug, true)
        }
    }

}
