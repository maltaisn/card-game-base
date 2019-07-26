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
import com.maltaisn.cardgame.tests.core.SubmenuContentTest
import com.maltaisn.cardgame.widget.CardGameLayout
import com.maltaisn.cardgame.widget.table.ScoresTable
import com.maltaisn.cardgame.widget.table.ScoresTable.Header
import com.maltaisn.cardgame.widget.table.ScoresTable.Score
import kotlin.random.Random


class ScoresTableTest : SubmenuContentTest() {

    override fun layoutContent(layout: CardGameLayout, content: Table) {
        val table = ScoresTable(skin, 4)
        var footerShown = false

        fun updateHeaders(showDifficulty: Boolean) {
            table.headers = listOf(
                    Header("South", null),
                    Header("East", if (showDifficulty) "Intermediate" else null),
                    Header("North", if (showDifficulty) "Advanced" else null),
                    Header("West", if (showDifficulty) "Expert" else null))
        }

        fun updateTotalScores() {
            table.footerScores = if (footerShown) List(4) { column ->
                Score(table.scores.map { it[column].value.toInt() }.sum().toString())
            } else null
        }

        fun createScoresRow() = List(4) {
            Score(Random.nextInt(-30, 30).toString())
        }

        repeat(10) { table.scores += createScoresRow() }
        table.cellAdapter?.notifyChanged()
        updateHeaders(true)
        updateTotalScores()

        // Do the layout
        content.add(table).grow().pad(40f, 200f, 40f, 200f)

        // Action buttons
        addToggleBtn("Footer shown") { _, shown ->
            footerShown = shown
            updateTotalScores()
        }
        addActionBtn("Add scores row") {
            table.scores += createScoresRow()
            table.cellAdapter?.notifyChanged()
            updateTotalScores()
        }
        addToggleBtn("Show player difficulty", startState = true) { _, shown ->
            updateHeaders(shown)
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
        addToggleBtn("Debug") { _, debug ->
            table.setDebug(debug, true)
        }
    }

}
