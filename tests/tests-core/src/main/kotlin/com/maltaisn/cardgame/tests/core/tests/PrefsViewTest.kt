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

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.ui.Container
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.maltaisn.cardgame.CardGameLayout
import com.maltaisn.cardgame.prefs.GamePrefs
import com.maltaisn.cardgame.tests.core.CardGameTest
import com.maltaisn.cardgame.widget.ScrollView
import com.maltaisn.cardgame.widget.prefs.PrefsGroup
import ktx.actors.setScrollFocus
import ktx.assets.load
import ktx.log.info


/**
 * Test for preference group and views, preference parsing and inflating.
 */
class PrefsViewTest : CardGameTest() {

    override fun load() {
        super.load()
        assetManager.load<GamePrefs>(PREFS_FILE)
    }

    override fun layout(layout: CardGameLayout) {
        super.layout(layout)

        val prefs = assetManager.get<GamePrefs>(PREFS_FILE)
        val prefsView = PrefsGroup(coreSkin, prefs)
        prefsView.helpListener = { pref ->
            info { "Help for ${pref.shortTitle ?: pref.title}: ${pref.help}" }
        }
        prefsView.listClickListener = { pref ->
            info { "List preference clicked. Available choices: ${pref.values.joinToString()}." }
        }

        // Do the layout
        val content = Container(prefsView)
        content.fill().pad(0f, 20f, 0f, 20f)

        ScrollView(content, ScrollPane.ScrollPaneStyle(
                coreSkin.getDrawable("submenu-content-background"),
                null, null, null, null)).apply contentPane@{
            setScrollingDisabled(true, false)
            setOverscroll(false, false)
            setCancelTouchFocus(false)
            layout.gameLayer.centerTable.add(this).grow()
                    .pad(20f, 20f, 0f, 20f)
            addListener(object : InputListener() {
                override fun enter(event: InputEvent, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
                    this@contentPane.setScrollFocus(true)
                }

                override fun exit(event: InputEvent?, x: Float, y: Float, pointer: Int, toActor: Actor?) {
                    this@contentPane.setScrollFocus(false)
                }
            })
        }
    }

    companion object {
        private const val PREFS_FILE = "settings.json"
    }

}