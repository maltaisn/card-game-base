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

package com.maltaisn.cardgame.tests.core.tests.menu

import com.badlogic.gdx.Input
import com.badlogic.gdx.scenes.scene2d.ui.Container
import com.maltaisn.cardgame.markdown.Markdown
import com.maltaisn.cardgame.prefs.GamePrefs
import com.maltaisn.cardgame.prefs.SwitchPref
import com.maltaisn.cardgame.stats.Statistics
import com.maltaisn.cardgame.tests.core.CardGameTest
import com.maltaisn.cardgame.tests.core.TestRes
import com.maltaisn.cardgame.widget.CardGameLayout
import com.maltaisn.cardgame.widget.menu.*
import com.maltaisn.cardgame.widget.prefs.ResetGameDialog
import com.maltaisn.cardgame.widget.table.ScoresTable
import ktx.actors.onKeyDown
import ktx.assets.load
import ktx.log.info
import kotlin.random.Random


/**
 * Test [DefaultGameMenu] with settings and rules.
 * Test [GamePrefs.save] on pause.
 */
class DefaultGameMenuTest : CardGameTest() {

    private lateinit var menu: DefaultGameMenu


    override fun load() {
        super.load()

        assetManager.load<GamePrefs>(TestRes.NEW_GAME_OPTIONS)
        assetManager.load<GamePrefs>(TestRes.SETTINGS)
        assetManager.load<Markdown>(TestRes.LOREM_IPSUM_MARKDOWN)
        assetManager.load<Statistics>(TestRes.STATS)
    }

    override fun layout(layout: CardGameLayout) {
        super.layout(layout)

        menu = DefaultGameMenu(skin)
        layout.addActor(menu)

        menu.callback = object : DefaultGameMenu.Callback {
            override fun onContinueClicked() {
                menu.showMenu(menu.inGameMenu)
                info { "Continue clicked" }
            }

            override fun onStartGameClicked() {
                menu.showMenu(menu.inGameMenu, false)
                info { "Start game clicked" }
            }

            override fun onInGameMenuItemClicked(item: MenuItem) {
                info { "In-game item clicked: $item" }
            }

            override fun onExitGameClicked() {
                menu.goBack()
                info { "Exit game clicked" }
            }

            override fun onScoreboardOpenClicked() {
                menu.showMenu(menu.scoreboardMenu)
                info { "Scoreboard opened" }
            }

            override fun onScoreboardCloseClicked() {
                menu.goBack()
                info { "Scoreboard closed" }
            }
        }

        val confirmDialog = ResetGameDialog(skin)
        menu.confirmCallback = { pref, callback ->
            confirmDialog.let {
                it.pref = pref
                it.callback = callback
                it.show(this@DefaultGameMenuTest)
            }
        }

        // New game
        val newGamePrefs: GamePrefs = assetManager[TestRes.NEW_GAME_OPTIONS]
        menu.newGameOptions = newGamePrefs
        prefs += newGamePrefs

        // Settings
        val settingsPrefs: GamePrefs = assetManager[TestRes.SETTINGS]
        menu.settings = settingsPrefs
        prefs += settingsPrefs

        // Rules
        menu.rules = assetManager[TestRes.LOREM_IPSUM_MARKDOWN]

        // Stats
        menu.stats = assetManager[TestRes.STATS]

        // Continue
        val continuePref = settingsPrefs[PREF_CONTINUE] as SwitchPref
        continuePref.valueListeners += { _, value -> menu.continueItem.enabled = value }
        menu.continueItem.enabled = continuePref.value

        // In-game
        menu.inGameMenu.addItem(MenuItem(0, null,
                skin.getDrawable(MenuIcons.CARDS), InGameMenu.ITEM_POS_LEFT))

        // Scoreboard
        val scoresTable = ScoresTable(skin, 4)
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

        val scoresView = Container(scoresTable).pad(60f, 30f, 60f, 30f).fill()
        val scoresPage = PagedSubMenu.Page(1, "Scores",
                skin.getDrawable(MenuIcons.CHART), SubMenu.ITEM_POS_TOP, scoresView)
        menu.scoreboardMenu.apply {
            addItem(scoresPage)
            checkItem(scoresPage)
        }

        // Debug
        val debugPref = settingsPrefs[PREF_DEBUG] as SwitchPref
        debugPref.valueListeners += { _, value -> isDebugAll = value }
        isDebugAll = debugPref.value

        // Back key listener
        layout.onKeyDown(true) {
            if (it == Input.Keys.BACK || it == Input.Keys.ESCAPE) {
                menu.goBack()
            }
        }
    }

    override fun pause() {
        super.pause()

        // Save all preferences when game is paused
        for (pref in prefs) {
            pref.save()
        }
    }

    companion object {
        private const val PREF_CONTINUE = "enable_continue"
        private const val PREF_DEBUG = "debug"
    }

}
