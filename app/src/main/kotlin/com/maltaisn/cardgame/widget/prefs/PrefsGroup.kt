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
import com.badlogic.gdx.utils.Align
import com.maltaisn.cardgame.prefs.GamePref
import com.maltaisn.cardgame.prefs.GamePrefs
import com.maltaisn.cardgame.prefs.ListPref
import com.maltaisn.cardgame.widget.Separator


class PrefsGroup(skin: Skin, val prefs: GamePrefs) : Table() {

    /** Listener called when a preference help icon is clicked. */
    var helpListener: ((GamePref) -> Unit)? = null
        set(value) {
            field = value
            for (child in children) {
                if (child is PrefCategoryView) {
                    child.helpListener = value
                }
            }
        }

    /** Listener called when a list preference value is clicked. */
    var listClickListener: ((ListPref) -> Unit)? = null
        set(value) {
            field = value
            for (child in children) {
                if (child is PrefCategoryView) {
                    child.listClickListener = value
                }
            }
        }


    init {
        pad(20f, 0f, 40f, 0f)
        align(Align.top)

        val prefsList = prefs.prefs.values.toList()
        for ((i, pref) in prefsList.withIndex()) {
            // Add preference view
            val view = pref.createView(skin)
            if (view is GamePrefView<*>) {
                view.helpListener = {
                    helpListener?.invoke(view.pref)
                }
                if (view is ListPrefView) {
                    view.valueClickListener = {
                        listClickListener?.invoke(view.pref)
                    }
                }
            }
            add(view).growX().row()

            // Separator between preferences
            if (pref is GamePref && prefsList.getOrNull(i + 1) is GamePref) {
                add(Separator(skin)).growX().pad(20f, 30f, 20f, 0f).row()
            }
        }
    }

}
