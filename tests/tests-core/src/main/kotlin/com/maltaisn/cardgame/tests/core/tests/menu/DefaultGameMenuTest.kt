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
import com.badlogic.gdx.utils.I18NBundle
import com.maltaisn.cardgame.CardGameListener
import com.maltaisn.cardgame.markdown.Markdown
import com.maltaisn.cardgame.pcard.PCard
import com.maltaisn.cardgame.prefs.GamePrefs
import com.maltaisn.cardgame.prefs.SliderPref
import com.maltaisn.cardgame.prefs.SwitchPref
import com.maltaisn.cardgame.tests.core.CardGameTest
import com.maltaisn.cardgame.tests.core.TestRes
import com.maltaisn.cardgame.tests.core.builders.buildTestNewGameOptions
import com.maltaisn.cardgame.tests.core.builders.buildTestSettings
import com.maltaisn.cardgame.tests.core.builders.buildTestStats
import com.maltaisn.cardgame.widget.AboutView
import com.maltaisn.cardgame.widget.CardGameLayout
import com.maltaisn.cardgame.widget.CoreIcons
import com.maltaisn.cardgame.widget.action.TimeAction
import com.maltaisn.cardgame.widget.menu.*
import com.maltaisn.cardgame.widget.prefs.ResetGameDialog
import com.maltaisn.cardgame.widget.table.ScoresTable
import ktx.actors.onKeyDownEvent
import ktx.assets.load
import ktx.log.info
import ktx.style.get
import kotlin.random.Random


/**
 * Test [DefaultGameMenu] with settings and rules.
 * Test [GamePrefs.save] on pause.
 */
class DefaultGameMenuTest(listener: CardGameListener) : CardGameTest(listener) {

    private lateinit var menu: DefaultGameMenu


    override fun load() {
        super.load()
        assetManager.load<Markdown>(TestRes.LOREM_IPSUM_MARKDOWN)
    }

    override fun layout(layout: CardGameLayout) {
        super.layout(layout)

        val bundle: I18NBundle = skin.get()

        menu = DefaultGameMenu(skin, pcardStyle)
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

            override fun onGameSummaryOpenClicked() {
                menu.showMenu(menu.gameSummaryMenu)
                info { "Game summary opened" }
            }

            override fun onGameSummaryCloseClicked() {
                menu.goBack()
                info { "Game summary closed" }
            }
        }

        val confirmDialog = ResetGameDialog(skin)
        menu.confirmCallback = { pref, callback ->
            confirmDialog.pref = pref
            confirmDialog.callback = callback
            confirmDialog.show(this@DefaultGameMenuTest)
        }

        // Main menu
        menu.mainMenu.title = "Test"
        menu.mainMenu.cards = listOf(PCard("K♥"), PCard("A♥"),
                PCard("Q♠"), PCard("J♥"), PCard("Q♥"))

        // New game
        menu.newGameOptions = buildTestNewGameOptions(bundle)

        // Settings
        val settingsPrefs = buildTestSettings(bundle)
        menu.settings = settingsPrefs

        // Rules
        menu.rules = assetManager[TestRes.LOREM_IPSUM_MARKDOWN]

        // Stats
        menu.stats = buildTestStats(skin.get())

        // About
        val aboutView = AboutView(skin, "App name", "1.0.0", "Author name").apply {
            appIcon = skin.getDrawable("icon")
            addButton("Rate app", skin.getDrawable(CoreIcons.STAR))
            addButton("View changelog", skin.getDrawable(CoreIcons.LIST))
        }
        val aboutPage = PagedSubMenu.Page(0, "About",
                skin.getDrawable(CoreIcons.INFO), SubMenu.ITEM_POS_TOP, aboutView)
        menu.aboutMenu.addItem(aboutPage)
        menu.aboutMenu.checkItem(aboutPage)

        // Continue
        val continuePref = settingsPrefs[PREF_CONTINUE] as SwitchPref
        continuePref.valueListeners += { _, value -> menu.continueItem.enabled = value }
        menu.continueItem.enabled = continuePref.value

        // In-game
        menu.inGameMenu.addItem(MenuItem(0, null,
                skin.getDrawable(CoreIcons.CARDS), InGameMenu.ITEM_POS_LEFT))

        // Game summary
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
        val scoresPage = PagedSubMenu.Page(0, "Scores",
                skin.getDrawable(CoreIcons.CHART), SubMenu.ITEM_POS_TOP, scoresView)

        val continueItem = MenuItem(1000, "Continue",
                skin.getDrawable(CoreIcons.ARROW_RIGHT), SubMenu.ITEM_POS_BOTTOM, true)
        continueItem.checkable = false

        menu.gameSummaryMenu.apply {
            addItems(scoresPage, continueItem)
            checkItem(scoresPage)
            itemClickListener = { item ->
                if (item === continueItem) {
                    menu.goBack()
                }
            }
        }

        // Debug
        val debugPref = settingsPrefs[PREF_DEBUG] as SwitchPref
        debugPref.valueListeners += { _, value -> isDebugAll = value }
        isDebugAll = debugPref.value

        // Animation speed
        val animSpeedPref = settingsPrefs[PREF_ANIM_SPEED] as SliderPref
        animSpeedPref.valueListeners += { _, value -> TimeAction.SPEED_MULTIPLIER = value }

        // Back key listener
        layout.onKeyDownEvent(true) { event, _, key ->
            if (!event.isHandled && (key == Input.Keys.BACK || key == Input.Keys.ESCAPE)) {
                menu.goBack()
            }
        }
    }

    override fun pause() {
        super.pause()

        menu.settings?.save()
        menu.newGameOptions?.save()
    }

    companion object {
        private const val PREF_CONTINUE = "enable_continue"
        private const val PREF_DEBUG = "debug"
        private const val PREF_ANIM_SPEED = "anim_speed"
    }

}
