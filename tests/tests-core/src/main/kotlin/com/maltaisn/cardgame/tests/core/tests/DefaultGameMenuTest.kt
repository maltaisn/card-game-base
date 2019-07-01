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
import com.maltaisn.cardgame.markdown.Markdown
import com.maltaisn.cardgame.prefs.GamePrefs
import com.maltaisn.cardgame.prefs.PrefEntry
import com.maltaisn.cardgame.prefs.SwitchPref
import com.maltaisn.cardgame.tests.core.CardGameTest
import com.maltaisn.cardgame.widget.CardGameLayout
import com.maltaisn.cardgame.widget.menu.*
import com.maltaisn.cardgame.widget.menu.table.ScoresTable
import ktx.assets.load
import ktx.log.info
import kotlin.random.Random


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
                info { "Continue clicked" }
            }

            override fun onStartGameClicked() {
                info { "Start game clicked" }
            }

            override fun onInGameMenuItemClicked(item: MenuItem) {
                info { "In-game item clicked: $item" }
            }

            override fun onExitGameClicked() {
                info { "Exit game clicked" }
            }

            override fun onScoreboardOpened() {
                info { "Scoreboard opened" }
            }

            override fun onScoreboardClosed() {
                info { "Scoreboard closed" }
            }
        }
        layout.addActor(menu)

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
        val scoresTable = ScoresTable(coreSkin, 4)
        scoresTable.headers = listOf(
                ScoresTable.Header("South", null),
                ScoresTable.Header("East", null),
                ScoresTable.Header("North", null),
                ScoresTable.Header("West", null))
        repeat(10) {
            scoresTable.scores += List(4) { ScoresTable.Score(Random.nextInt(30).toString()) }
        }
        scoresTable.footerScores = List(4) { column ->
            ScoresTable.Score(scoresTable.scores.map { it[column].value.toInt() }.sum().toString())
        }

        val scoresView = Container(scoresTable).pad(30f, 15f, 30f, 15f).fill()
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
