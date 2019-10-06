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


fun buildTestSettings(strings: I18NBundle) =
        GamePrefs("com.maltaisn.cardgametest.settings") {
            category("debug_category") {
                title = "Debug"
                icon = "icon-settings"
                switch("debug") {
                    title = "Enable debug mode"
                    defaultValue = false
                }
                slider("anim_speed") {
                    title = "Animation speed multiplier"
                    defaultValue = 1f
                    minValue = 0.1f
                    maxValue = 3f
                    step = 0.1f
                }
            }
            category("interface_category") {
                title = strings["pref_interface"]
                icon = "icon-pencil"
                switch("enable_continue") {
                    title = "Enable continue menu item"
                    defaultValue = false
                }
                switch("fullscreen") {
                    title = strings["pref_fullscreen"]
                    help = strings["pref_fullscreen_help"]
                    defaultValue = true
                }
                text("game_name") {
                    title = "Game name"
                    inputTitle = "Enter game name"
                    help = "I don't know why but you can choose the game name."
                    defaultValue = "Card game"
                    dependency = "fullscreen"
                }
                playerNames("player_names") {
                    title = "Player names"
                    inputTitle = "Enter player name"
                    maxLength = 20
                    defaultValue = arrayOf("North", "East", "South", "West")
                    dependency = "fullscreen"
                }
                switch("automatic_play") {
                    title = "Enable automatic play"
                    defaultValue = true
                }
                switch("auto_collect") {
                    title = "Collect tricks automatically"
                    defaultValue = true
                }
                switch("hearts_opening") {
                    title = "Opening with hearts not allowed"
                    defaultValue = true
                }
                switch("spade_queen_allow_heart") {
                    title = "Hearts can be played after queen of spade has been played. " +
                            "This setting has a somewhat long title than will hopefully span at least 2 lines."
                    help = "No help!"
                    shortTitle = "Hearts played"
                    defaultValue = true
                }
                list("variant") {
                    title = "Game variant"
                    defaultValue = "normal"
                    dependency = "spade_queen_allow_heart"
                    entries += mapOf(
                            "normal" to "Normal",
                            "complex" to "Complex hearts",
                            "jack" to "Jack of diamond")
                }
                switch("enable_game_category") {
                    title = "Enable game category"
                    defaultValue = true
                }
            }
            category("game_category") {
                title = strings["pref_game"]
                dependency = "enable_game_category"
                slider("players") {
                    title = strings["pref_players"]
                    minValue = 2f
                    maxValue = 6f
                    step = 1f
                    defaultValue = 4f
                    dependency = "hearts_opening"
                }
                slider("points_trick") {
                    title = "Points per trick taken"
                    minValue = -20f
                    maxValue = 20f
                    step = 2f
                    defaultValue = 6f
                }
                slider("game_speed_variant") {
                    title = "Game speed"
                    minValue = 0f
                    maxValue = 3f
                    defaultValue = 1f
                    enumValues = listOf("Slow", "Normal", "Fast", "Very fast")
                }
                switch("cheats") {
                    title = "Enable cheats"
                    defaultValue = false
                }
            }
            category("reset_game_category") {
                title = "Resets game"
                switch("rg_switch") {
                    title = "Switch preference"
                    defaultValue = false
                    confirmChanges = true
                }
                slider("rg_slider") {
                    title = "Slider preference"
                    minValue = 1f
                    maxValue = 10f
                    step = 1f
                    defaultValue = 5f
                    confirmChanges = true
                }
                text("rg_text") {
                    title = "Text preference"
                    inputTitle = "Enter text"
                    defaultValue = "Text"
                    confirmChanges = true
                }
                list("rg_list") {
                    title = "List preference"
                    defaultValue = "choice1"
                    confirmChanges = true
                    entries += mapOf(
                            "choice1" to "Choice 1",
                            "choice2" to "Choice 2",
                            "choice3" to "Choice 3")
                }
                playerNames("rg_player_names") {
                    title = "Player names preference"
                    inputTitle = "Enter player name"
                    maxLength = 20
                    defaultValue = arrayOf("Player 1", "Player 2")
                    confirmChanges = true
                }
            }
        }
