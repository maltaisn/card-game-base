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
import com.maltaisn.cardgame.tests.core.SingleActionTest
import com.maltaisn.cardgame.widget.menu.DefaultGameMenu
import ktx.assets.load


/**
 * Test [DefaultGameMenu] with settings and rules.
 * Test [GamePrefs.save] on pause.
 */
class MenuTest : SingleActionTest() {

    override fun load() {
        super.load()

        assetManager.load<GamePrefs>(PREFS_NEW_GAME)
        assetManager.load<GamePrefs>(PREFS_SETTINGS)
        assetManager.load<Markdown>(MD_RULES)
    }

    override fun start() {
        super.start()

        //isDebugAll = true

        val menu = DefaultGameMenu(coreSkin)
        menu.continueItem.enabled = false
        menu.shown = true
        gameMenu = menu

        val newGamePrefs = assetManager.get<GamePrefs>(PREFS_NEW_GAME)
        menu.newGameOptions = newGamePrefs
        menu.startGameListener = {
            menu.shown = false
        }
        prefs += newGamePrefs

        val settingsPrefs = assetManager.get<GamePrefs>(PREFS_SETTINGS)
        menu.settings = settingsPrefs
        prefs += settingsPrefs

        menu.rules = assetManager.get(MD_RULES)

        action = {
            menu.shown = !menu.shown
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
        private const val PREFS_NEW_GAME = "new-game-options.json"
        private const val PREFS_SETTINGS = "settings.json"
        private const val MD_RULES = "rules"
    }

}