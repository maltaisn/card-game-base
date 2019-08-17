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

import com.badlogic.gdx.scenes.scene2d.ui.Container
import com.badlogic.gdx.scenes.scene2d.ui.Table
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
class StatsViewTest : SubmenuContentTest() {

    override fun load() {
        super.load()
        assetManager.load<Statistics>(TestRes.STATS)
    }

    override fun layoutContent(layout: CardGameLayout, content: Table) {
        val stats: Statistics = assetManager.get(TestRes.STATS)
        val statsView = StatsGroup(skin, stats)

        // Do the layout
        val container = Container(statsView)
        container.fill().pad(0f, 40f, 0f, 40f)

        content.add(ScrollView(container)).grow()

        // Action buttons
        addEnumBtn("Variant", stats.variants.indices.toList(), stats.variants) { _, value ->
            statsView.shownVariant = value
        }
        addActionBtn("Change all values") {
            val v = statsView.shownVariant
            stats.getNumber("tricksTaken")[v] += Random.nextInt(8)
            stats.getNumber("roundsPlayed")[v]++
            stats.getNumber("gamesPlayed")[v] += Random.nextInt(1, 4)
            stats.getNumber("gamesWon")[v]++
            stats.getNumber("tradeCount_internal")[v] += Random.nextInt(2)
            stats.getNumber("totalPoints")[v] += Random.nextInt(10)
            statsView.refresh()
            stats.save()
        }
        addActionBtn("Reset") {
            stats.reset()
            stats.save()
            statsView.refresh()
        }
        addToggleBtn("Debug") { _, debug ->
            statsView.setDebug(debug, true)
        }
    }

}
