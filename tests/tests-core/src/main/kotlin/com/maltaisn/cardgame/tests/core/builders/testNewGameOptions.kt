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
import com.maltaisn.cardgame.prefs.GamePrefs


fun testNewGameOptions(strings: I18NBundle) =
        GamePrefs("com.maltaisn.cardgametest.newGameOptions") {
            slider("player_count") {
                title = strings["pref_player_count"]
                minValue = 2f
                maxValue = 6f
                defaultValue = 4f
            }
            slider("difficulty") {
                title = strings["pref_difficulty"]
                help = strings["pref_difficulty_help"]
                minValue = 0f
                maxValue = 3f
                defaultValue = 1f
                enumValues = listOf(
                        strings["pref_difficulty_0"],
                        strings["pref_difficulty_1"],
                        strings["pref_difficulty_2"],
                        strings["pref_difficulty_3"])
            }
            list("game_speed") {
                title = strings["pref_game_speed"]
                defaultValue = "normal"
                entries += mapOf(
                        "very_slow" to strings["pref_game_speed_0"],
                        "slow" to strings["pref_game_speed_1"],
                        "normal" to strings["pref_game_speed_2"],
                        "fast" to strings["pref_game_speed_3"],
                        "very_fast" to strings["pref_game_speed_4"])
            }
            switch("cheat_mode") {
                title = strings["pref_cheat_mode"]
                help = strings["pref_cheat_mode_help"]
                defaultValue = false
            }
        }
