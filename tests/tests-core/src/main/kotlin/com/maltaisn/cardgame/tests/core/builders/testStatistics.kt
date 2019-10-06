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

package com.maltaisn.cardgame.tests.core.builders

import com.badlogic.gdx.utils.I18NBundle
import com.maltaisn.cardgame.stats.Statistics
import kotlin.random.Random


fun buildTestStats(strings: I18NBundle) =
        Statistics("com.maltaisn.cardgametest.stats") {
            variants = listOf("Easy", "Intermediate", "Hard")
            number("tricksTaken") {
                internal = true
            }
            number("roundsPlayed") {
                internal = true
            }
            number("gamesPlayed") {
                title = strings["stat_games_played"]
            }
            average("averageTricksTaken") {
                title = strings["stat_average_tricks"]
                totalStatKey = "tricksTaken"
                countStatKey = "roundsPlayed"
                precision = 2
            }
            number("gamesWon") {
                title = strings["stat_games_won"]
            }
            percent("percentGamesWon") {
                title = strings["stat_percent_games_won"]
                fracStatKey = "gamesWon"
                totalStatKey = "gamesPlayed"
                precision = 1
            }
            number("tradeCount_internal") {
                internal = true
            }
            percent("tradeCount") {
                title = "Number and percentage hands traded"
                fracStatKey = "tradeCount_internal"
                totalStatKey = "roundsPlayed"
                showFrac = true
            }
            number("totalPoints") {
                title = "Total points scored"
            }
            number("minRoundsInGame") {
                defaultValue = Float.NaN
                title = "Minimum rounds before the game ended"
            }
        }


fun changeAllTestStatsValues(stats: Statistics, variant: Int) {
    assert(stats.name == "com.maltaisn.cardgametest.stats")
    stats.apply {
        getNumber("tricksTaken")[variant] += Random.nextInt(8)
        getNumber("roundsPlayed")[variant]++
        getNumber("gamesPlayed")[variant] += Random.nextInt(1, 4)
        getNumber("gamesWon")[variant]++
        getNumber("tradeCount_internal")[variant] += Random.nextInt(2)
        getNumber("totalPoints")[variant] += Random.nextInt(10)
        getNumber("minRoundsInGame")[variant] = 3
        save()
    }
}
