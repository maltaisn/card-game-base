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
import com.maltaisn.cardgame.CardGameListener
import com.maltaisn.cardgame.stats.Statistics
import com.maltaisn.cardgame.tests.core.SubmenuContentTest
import com.maltaisn.cardgame.tests.core.TestRes
import com.maltaisn.cardgame.widget.CardGameLayout
import com.maltaisn.cardgame.widget.ScrollView
import com.maltaisn.cardgame.widget.stats.StatsGroup
import ktx.assets.load
import kotlin.random.Random


/**
 * Test for statistics views, parsing and inflating.
 */
class StatsViewTest(listener: CardGameListener) : SubmenuContentTest(listener) {

    override fun load() {
        super.load()
        assetManager.load<Statistics>(TestRes.STATS)
    }

    override fun layoutContent(layout: CardGameLayout, content: Table) {
        val stats: Statistics = assetManager[TestRes.STATS]

        // Do the layout
        val statsGroup = StatsGroup(skin, stats)
        statsGroup.padLeft(40f).padRight(40f)
        content.add(ScrollView(statsGroup)).grow()

        // Action buttons
        addEnumBtn("Variant", stats.variants!!.indices.toList(), stats.variants) { _, value ->
            statsGroup.shownVariant = value
        }
        addActionBtn("Change all values") {
            stats.apply {
                val v = statsGroup.shownVariant
                getNumber("tricksTaken")[v] += Random.nextInt(8)
                getNumber("roundsPlayed")[v]++
                getNumber("gamesPlayed")[v] += Random.nextInt(1, 4)
                getNumber("gamesWon")[v]++
                getNumber("tradeCount_internal")[v] += Random.nextInt(2)
                getNumber("totalPoints")[v] += Random.nextInt(10)
                getNumber("minRoundsInGame")[v] = 3
                save()
            }
            statsGroup.refresh()
        }
        addActionBtn("Reset") {
            stats.reset()
            stats.save()
            statsGroup.refresh()
        }
        addToggleBtn("Debug") { _, debug ->
            statsGroup.setDebug(debug, true)
        }
    }

}
