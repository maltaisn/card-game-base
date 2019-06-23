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
import com.maltaisn.cardgame.tests.core.ActionBarTest
import com.maltaisn.cardgame.widget.CardGameLayout
import com.maltaisn.cardgame.widget.menu.table.ScoresTable
import com.maltaisn.cardgame.widget.menu.table.ScoresTable.Header
import com.maltaisn.cardgame.widget.menu.table.ScoresTable.Score
import ktx.actors.setScrollFocus
import java.text.NumberFormat
import kotlin.random.Random


class ScoresTableTest : ActionBarTest() {

    override fun layout(layout: CardGameLayout) {
        super.layout(layout)

        val table = ScoresTable(coreSkin, 4)
        repeat(10) { table.scores += createScoresRow() }
        table.cellAdapter?.notifyChanged()
        updateHeaders(table, true)
        updateTotalScores(table)

        // Do the layout
        val content = Table().apply {
            background = coreSkin.getDrawable("submenu-content-background")
            add(table).grow().pad(20f, 100f, 20f, 100f)
        }
        layout.gameLayer.centerTable.add(content).grow()
                .pad(0f, 20f, 0f, 20f)
        table.itemScrollView.setScrollFocus()

        // Action buttons
        addActionBtn("Add scores row") {
            table.scores += createScoresRow()
            table.cellAdapter?.notifyChanged()
            updateTotalScores(table)
        }
        addToggleBtn("Show player difficulty", startState = true) { _, shown ->
            updateHeaders(table, shown)
        }
        addTwoStateActionBtn("Highlight random", "Highlight none") { _, unhighlight ->
            // Highlight random values or unhighlight all
            val scores = table.scores.map { row ->
                row.map {
                    Score(it.value, when {
                        unhighlight -> Score.Highlight.NONE
                        Random.nextInt(5) == 0 -> Score.Highlight.POSITIVE
                        Random.nextInt(5) == 0 -> Score.Highlight.NEGATIVE
                        else -> Score.Highlight.NONE
                    })
                }
            }
            table.scores.clear()
            table.scores += scores
            table.cellAdapter?.notifyChanged()
        }
        addTwoStateActionBtn("Currency format", "Normal format") { _, isNormal ->
            table.numberFormat = if (isNormal) {
                NumberFormat.getNumberInstance()
            } else {
                NumberFormat.getCurrencyInstance()
            }
        }
        addToggleBtn("Debug") { _, debug ->
            table.setDebug(debug, true)
        }
    }

    private fun updateHeaders(table: ScoresTable, showDifficulty: Boolean) {
        table.headers = listOf(
                Header("South", null),
                Header("East", if (showDifficulty) "Intermediate" else null),
                Header("North", if (showDifficulty) "Advanced" else null),
                Header("West", if (showDifficulty) "Expert" else null))
    }

    private fun updateTotalScores(table: ScoresTable) {
        table.footerScores = table.scores.map { row ->
            Score(row.sumBy { it.value.toInt() }.toFloat())
        }
    }

    private fun createScoresRow() = List(4) {
        Score(Random.nextInt(-30, 30).toFloat())
    }

}
