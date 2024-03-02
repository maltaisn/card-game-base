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
import com.maltaisn.cardgame.CardGameListener
import com.maltaisn.cardgame.prefs.GamePref
import com.maltaisn.cardgame.prefs.GamePrefs
import com.maltaisn.cardgame.prefs.PrefEntry
import com.maltaisn.cardgame.tests.core.SubmenuContentTest
import com.maltaisn.cardgame.tests.core.builders.buildTestSettings
import com.maltaisn.cardgame.utils.padH
import com.maltaisn.cardgame.widget.CardGameLayout
import com.maltaisn.cardgame.widget.ScrollView
import com.maltaisn.cardgame.widget.menu.MenuDrawer
import com.maltaisn.cardgame.widget.prefs.PrefsGroup
import com.maltaisn.cardgame.widget.prefs.ResetGameDialog
import ktx.log.info
import ktx.style.get


/**
 * Test for preference group and views, preference parsing and inflating.
 */
class PrefsViewTest(listener: CardGameListener) : SubmenuContentTest(listener) {

    private lateinit var prefs: GamePrefs

    override fun layoutContent(layout: CardGameLayout, content: Table) {
        val drawer = MenuDrawer(skin)
        drawer.backBtnText = "Back"
        layout.addActor(drawer)

        prefs = buildTestSettings(skin.get())
        val prefsGroup = PrefsGroup(skin, prefs, drawer)

        var confirmChanges = true
        val confirmDialog = ResetGameDialog(skin)
        prefsGroup.confirmCallback = { pref, callback ->
            if (confirmChanges) {
                confirmDialog.pref = pref
                confirmDialog.callback = callback
                confirmDialog.show(this@PrefsViewTest)
            }
        }

        prefs.addValueListener(::onPrefValueChanged)
        prefs.forEachPref { it.enabledListeners += ::onPrefEnabledChanged }

        // Do the layout
        val prefsContainer = Container(prefsGroup)
        prefsContainer.fill().padH(40f)

        content.add(ScrollView(prefsContainer)).grow()

        // Action buttons
        addToggleBtn("Enable confirmation", startState = confirmChanges) { _, enabled ->
            confirmChanges = enabled
        }
        addToggleBtn("Debug") { _, debug ->
            prefsGroup.setDebug(debug, true)
        }
    }

    override fun pause() {
        super.pause()
        prefs.save()
    }

    @Suppress("UNUSED_PARAMETER")
    private fun <T : Any> onPrefValueChanged(pref: GamePref<T>, value: T) {
        info { "Preference '${pref.key}' value changed." }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onPrefEnabledChanged(pref: PrefEntry, enabled: Boolean) {
        info { "Preference '${pref.key}' enabled state changed." }
    }

}
