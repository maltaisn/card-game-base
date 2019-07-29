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
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.maltaisn.cardgame.prefs.GamePref
import com.maltaisn.cardgame.prefs.GamePrefs
import com.maltaisn.cardgame.prefs.PrefEntry
import com.maltaisn.cardgame.tests.core.SubmenuContentTest
import com.maltaisn.cardgame.tests.core.TestRes
import com.maltaisn.cardgame.widget.CardGameLayout
import com.maltaisn.cardgame.widget.ScrollView
import com.maltaisn.cardgame.widget.menu.MenuDrawer
import com.maltaisn.cardgame.widget.prefs.PrefsGroup
import com.maltaisn.cardgame.widget.prefs.ResetGameDialog
import ktx.assets.load
import ktx.log.info


/**
 * Test for preference group and views, preference parsing and inflating.
 */
class PrefsViewTest : SubmenuContentTest() {

    override fun load() {
        super.load()
        assetManager.load<GamePrefs>(TestRes.PREFS)
    }

    override fun layoutContent(layout: CardGameLayout, content: Table) {
        val drawer = MenuDrawer(skin)
        drawer.backBtnText = "Back"
        layout.addActor(drawer)

        val prefs: GamePrefs = assetManager.get(TestRes.PREFS)
        val prefsGroup = PrefsGroup(skin, prefs, drawer)

        var confirmChanges = true
        val confirmDialog = ResetGameDialog(skin)
        prefsGroup.confirmCallback = { pref, callback ->
            if (confirmChanges) {
                confirmDialog.let {
                    it.pref = pref
                    it.callback = callback
                    it.show(this@PrefsViewTest)
                }
            }
        }

        prefs.addValueListener(::onPrefValueChanged)
        prefs.forEachPref { it.enabledListeners += ::onPrefEnabledChanged }

        this.prefs += prefs

        // Do the layout
        val prefsContainer = Container(prefsGroup)
        prefsContainer.fill().pad(0f, 40f, 0f, 40f)

        content.add(ScrollView(prefsContainer)).grow()

        // Action buttons
        addToggleBtn("Enable confirmation", startState = confirmChanges) { _, enabled ->
            confirmChanges = enabled
        }
        addToggleBtn("Debug") { _, debug ->
            prefsGroup.setDebug(debug, true)
        }
    }

    private fun <T : Any?> onPrefValueChanged(pref: GamePref<T>, value: T) {
        info { "Preference '${pref.key}' value changed." }
    }

    private fun onPrefEnabledChanged(pref: PrefEntry, enabled: Boolean) {
        info { "Preference '${pref.key}' enabled state changed." }
    }

}
