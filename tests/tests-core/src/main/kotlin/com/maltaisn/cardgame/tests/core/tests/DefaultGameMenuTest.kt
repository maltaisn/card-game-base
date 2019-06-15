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

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.Container
import com.maltaisn.cardgame.markdown.Markdown
import com.maltaisn.cardgame.prefs.GamePrefs
import com.maltaisn.cardgame.prefs.PrefEntry
import com.maltaisn.cardgame.prefs.SwitchPref
import com.maltaisn.cardgame.tests.core.CardGameTest
import com.maltaisn.cardgame.widget.CardGameLayout
import com.maltaisn.cardgame.widget.FontStyle
import com.maltaisn.cardgame.widget.SdfLabel
import com.maltaisn.cardgame.widget.menu.*
import ktx.assets.load
import ktx.log.info


/**
 * Test [DefaultGameMenu] with settings and rules.
 * Test [GamePrefs.save] on pause.
 */
class DefaultGameMenuTest : CardGameTest() {

    override fun load() {
        super.load()

        assetManager.load<GamePrefs>(PREFS_NEW_GAME)
        assetManager.load<GamePrefs>(PREFS_SETTINGS)
        assetManager.load<Markdown>(MD_RULES)
    }

    override fun layout(layout: CardGameLayout) {
        super.layout(layout)

        val menu = object : DefaultGameMenu(coreSkin) {
            override fun onContinueClicked() {
                super.onContinueClicked()
                info { "Continue clicked" }
            }

            override fun onStartGameClicked() {
                super.onStartGameClicked()
                info { "Start game clicked" }
            }

            override fun onInGameMenuItemClicked(item: MenuItem) {
                super.onInGameMenuItemClicked(item)
                info { "In-game item clicked: $item" }
            }

            override fun onExitGameClicked() {
                super.onExitGameClicked()
                info { "Exit game clicked" }
            }
        }
        layout.gameMenu = menu

        // New game
        val newGamePrefs: GamePrefs = assetManager.get(PREFS_NEW_GAME)
        menu.newGameOptions = newGamePrefs
        prefs += newGamePrefs

        // Settings
        val settingsPrefs: GamePrefs = assetManager.get(PREFS_SETTINGS)
        menu.settings = settingsPrefs
        prefs += settingsPrefs

        // Rules
        menu.rules = assetManager.get(MD_RULES)

        // Continue
        val continuePref = settingsPrefs[PREF_CONTINUE] as SwitchPref
        continuePref.listeners += object : PrefEntry.PrefListener {
            override fun onPreferenceValueChanged(pref: PrefEntry) {
                menu.continueItem.enabled = continuePref.value
            }
        }
        menu.continueItem.enabled = continuePref.value

        // In-game
        menu.inGameMenu.addItem(MenuItem(0, null, coreSkin.getDrawable(MenuIcons.CARDS), InGameMenu.ITEM_POS_LEFT))

        // Scoreboard
        val scoresView = Container(SdfLabel(coreSkin, FontStyle(fontColor = Color.BLACK, fontSize = 30f), "TODO!"))
        val scoresPage = PagedSubMenu.Page(1, "Scores", coreSkin.getDrawable(MenuIcons.LIST), SubMenu.ITEM_POS_TOP)
        scoresPage.content = scoresView
        scoresPage.checked = true
        menu.scoreboardMenu.addItem(scoresPage)

        // Debug
        val debugPref = settingsPrefs[PREF_DEBUG] as SwitchPref
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

    companion object {
        private const val PREFS_NEW_GAME = "new-game-options.json"
        private const val PREFS_SETTINGS = "settings.json"
        private const val MD_RULES = "lorem-ipsum"

        private const val PREF_CONTINUE = "enable_continue"
        private const val PREF_DEBUG = "debug"
    }

}
