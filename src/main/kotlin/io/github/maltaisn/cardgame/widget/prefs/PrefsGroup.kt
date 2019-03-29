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

package io.github.maltaisn.cardgame.widget.prefs

import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.utils.Scaling
import io.github.maltaisn.cardgame.prefs.GamePref
import io.github.maltaisn.cardgame.prefs.GamePrefs


class PrefsGroup(skin: Skin, prefs: GamePrefs) : Table() {

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

    init {
        val style = skin[PrefsGroupStyle::class.java]
        pad(10f, 0f, 20f, 0f)

        val prefsList = prefs.prefs.values.toList()
        for ((i, pref) in prefsList.withIndex()) {
            // Add preference view
            val view = pref.createView(skin)
            if (view is PrefCategoryView) {
                view.helpListener = helpListener
            } else if (view is GamePrefView<*>) {
                view.helpListener = {
                    helpListener?.invoke(pref as GamePref)
                }
            }
            add(view).growX().row()

            // Separator between preferences
            if (pref is GamePref && prefsList.getOrNull(i + 1) is GamePref) {
                val separator = Image(style.separator, Scaling.stretchX)
                add(separator).growX().pad(10f, 15f, 10f, 0f).row()
            }
        }
    }

    /**
     * Detach all preference listeners attached when the views were created.
     * Must be called when the view is not used anymore to prevent memory leak.
     */
    fun detachListeners() {
        for (child in children) {
            if (child is PrefEntryView<*>) {
                child.detachListener()
            }
        }
    }

    class PrefsGroupStyle {
        lateinit var separator: Drawable
    }

}