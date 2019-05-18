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

import com.maltaisn.cardgame.markdown.Markdown
import com.maltaisn.cardgame.prefs.GamePrefs
import com.maltaisn.cardgame.prefs.PrefEntry
import com.maltaisn.cardgame.prefs.SwitchPref
import com.maltaisn.cardgame.tests.core.CardGameTest
import com.maltaisn.cardgame.widget.CardGameLayout
import com.maltaisn.cardgame.widget.menu.DefaultGameMenu
import ktx.assets.load
import ktx.log.info


/**
 * Test [DefaultGameMenu] with settings and rules.
 * Test [GamePrefs.save] on pause.
 */
class DefaultGameMenuTest : CardGameTest() {

    private lateinit var debugPref: SwitchPref
    private lateinit var continuePref: SwitchPref

    override fun load() {
        super.load()

        assetManager.load<GamePrefs>(PREFS_NEW_GAME)
        assetManager.load<GamePrefs>(PREFS_SETTINGS)
        assetManager.load<Markdown>(MD_RULES)
    }

    override fun layout(layout: CardGameLayout) {
        super.layout(layout)

        val menu = DefaultGameMenu(coreSkin)
        layout.gameMenu = menu

        // New game
        val newGamePrefs = assetManager.get<GamePrefs>(PREFS_NEW_GAME)
        menu.newGameOptions = newGamePrefs
        menu.startGameListener = {
            menu.showInGameMenu()
            info { "Start game clicked." }
        }
        prefs += newGamePrefs

        // Settings
        val settingsPrefs = assetManager.get<GamePrefs>(PREFS_SETTINGS)
        menu.settings = settingsPrefs
        prefs += settingsPrefs

        // Rules
        menu.rules = assetManager.get(MD_RULES)

        // Continue
        continuePref = settingsPrefs["enable_continue"] as SwitchPref
        continuePref.listeners += object : PrefEntry.PrefListener {
            override fun onPreferenceValueChanged(pref: PrefEntry) {
                menu.continueItem.enabled = continuePref.value
            }
        }
        menu.continueItem.enabled = continuePref.value
        menu.continueListener = {
            info { "Continue clicked." }
            menu.showInGameMenu()
        }

        // In game
        menu.exitGameListener = {
            info { "Exit game clicked." }
        }
        menu.scoreboardListener = {
            info { "Show scoreboard clicked." }
        }

        // Debug
        debugPref = settingsPrefs["debug"] as SwitchPref
        debugPref.listeners += object : PrefEntry.PrefListener {
            override fun onPreferenceValueChanged(pref: PrefEntry) {
                isDebugAll = debugPref.value
            }
        }
        isDebugAll = debugPref.value
    }

    override fun pause() {
        super.pause()

        // Save all preferences when game is paused
        for (pref in prefs) {
            pref.save()
        }
    }

    override fun dispose() {
        super.dispose()
        continuePref.listeners.clear()
        debugPref.listeners.clear()
    }

    companion object {
        private const val PREFS_NEW_GAME = "new-game-options.json"
        private const val PREFS_SETTINGS = "settings.json"
        private const val MD_RULES = "rules"
    }

}
