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

import com.maltaisn.cardgame.tests.core.ActionBarTest
import com.maltaisn.cardgame.widget.CardGameLayout
import com.maltaisn.cardgame.widget.FadeTable
import com.maltaisn.cardgame.widget.PlayerLabel
import kotlin.random.Random


/**
 * Test fade transition for [PlayerLabel]
 */
class PlayerLabelFadeTest : ActionBarTest() {

    override fun layout(layout: CardGameLayout) {
        super.layout(layout)

        var score = 0
        val label = PlayerLabel(skin, "Player")
        label.score = "10 ($score)"

        val table = FadeTable().apply {
            shown = true
            add(label).grow()
        }
        layout.gameLayer.centerTable.add(table).grow()

        var fadeMode = true
        addTwoStateActionBtn("Hide", "Show") { _, state ->
            if (fadeMode) {
                table.fade(state)
            } else {
                table.shown = state
            }
        }
        addTwoStateActionBtn("Mode: Fade", "Mode: Instant") { _, state ->
            fadeMode = state
        }

        addActionBtn("Change score") {
            score += 1
            label.score = "10 ($score)"
        }
        addActionBtn("Change all") {
            score += 1
            label.score = CharArray(10) { Random.nextInt(65, 91).toChar() }.joinToString("")
        }
        addToggleBtn("Debug") { _, debug -> table.setDebug(debug, true) }
    }

}
