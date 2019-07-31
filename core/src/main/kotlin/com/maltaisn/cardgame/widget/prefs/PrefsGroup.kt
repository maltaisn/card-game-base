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

package com.maltaisn.cardgame.widget.prefs

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.Value
import com.badlogic.gdx.utils.Align
import com.maltaisn.cardgame.prefs.GamePref
import com.maltaisn.cardgame.prefs.GamePrefs
import com.maltaisn.cardgame.prefs.ListPref
import com.maltaisn.cardgame.widget.Separator
import com.maltaisn.cardgame.widget.menu.MenuDrawer
import com.maltaisn.cardgame.widget.menu.MenuDrawerList
import com.maltaisn.cardgame.widget.text.FontStyle
import com.maltaisn.cardgame.widget.text.SdfLabel
import ktx.style.get


/**
 * The group containing the preference views and managing them.
 * A menu drawer must be provided to show the help text and the [ListPref] choices.
 */
class PrefsGroup(skin: Skin,
                 val prefs: GamePrefs,
                 val menuDrawer: MenuDrawer) : Table() {

    private val style: PrefsGroupStyle = skin.get()

    /**
     * Callback called when the value of a `pref` preference that requires confirmation is changed.
     * A user confirmation should be shown and the result should be forwarded to the `callback`,
     * either `true` to keep the change or `false` to revert it.
     */
    var confirmCallback: ConfirmCallback? = null


    private val prefsHelpLabel = SdfLabel(skin, style.prefsHelpFontStyle)

    private var currentListPrefView: ListPrefView? = null
    private val prefsDrawerList = MenuDrawerList(skin)


    init {
        pad(20f, 0f, 40f, 0f)
        align(Align.top)

        val prefsList = prefs.prefs.values.toList()
        for ((i, pref) in prefsList.withIndex()) {
            // Add preference view
            add(pref.createView(skin)).growX().row()

            // Separator between preferences
            if (pref is GamePref<*> && prefsList.getOrNull(i + 1) is GamePref<*>) {
                add(Separator(skin)).growX().pad(20f, 30f, 20f, 0f).row()
            }
        }

        // Drawer widgets
        prefsHelpLabel.setWrap(true)
        prefsHelpLabel.setAlignment(Align.topLeft)

        prefsDrawerList.selectionChangeListener = { index ->
            if (index != -1) {
                val prefView = currentListPrefView!!
                val pref = prefView.pref
                val oldIndex = pref.keys.indexOf(pref.value)
                if (index != oldIndex) {
                    prefView.changePreferenceValue(pref.keys[index]) {
                        prefsDrawerList.selectedIndex = oldIndex
                    }
                }
            }
        }
    }

    /**
     * Show the help text of a [pref] in the menu drawer.
     */
    internal fun showHelpText(pref: GamePref<*>) {
        menuDrawer.apply {
            content.actor = prefsHelpLabel
            content.pad(0f, 60f, 0f, 60f)
            drawerWidth = Value.percentWidth(0.5f, menuDrawer)
            title = pref.shortTitle ?: pref.title
            shown = true
        }
        prefsHelpLabel.setText(pref.help)
    }

    /**
     * Show the choices of a [ListPref] in the menu drawer.
     */
    internal fun showListPrefChoices(prefView: ListPrefView) {
        currentListPrefView = prefView

        val pref = prefView.pref
        menuDrawer.apply {
            content.actor = prefsDrawerList
            content.pad(0f, 0f, 0f, 0f)
            drawerWidth = Value.percentWidth(0.4f, menuDrawer)
            title = pref.shortTitle ?: pref.title
            shown = true
        }
        prefsDrawerList.items = pref.values
        prefsDrawerList.selectedIndex = pref.keys.indexOf(pref.value)
    }


    class PrefsGroupStyle {
        lateinit var prefsHelpFontStyle: FontStyle
    }

}

typealias ConfirmCallback = (pref: GamePref<*>, callback: (keep: Boolean) -> Unit) -> Unit
