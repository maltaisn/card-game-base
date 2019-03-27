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

    init {
        val style = skin[PrefsGroupStyle::class.java]
        pad(10f, 0f, 20f, 0f)

        for ((i, pref) in prefs.entries.withIndex()) {
            // Add preference view
            val view = pref.createView(skin)
            if (view is GamePrefView<*>) {
                view.helpListener = {
                    helpListener?.invoke(pref as GamePref)
                }
            }
            add(view).growX().row()

            // Add a separator, only between two game preferences, not after and before category or at the end.
            if (pref is GamePref && prefs.entries.getOrNull(i + 1) is GamePref) {
                val separator = Image(style.separator, Scaling.stretchX)
                add(separator).growX().pad(10f, 15f, 10f, 0f).row()
            }
        }
    }

    class PrefsGroupStyle {
        lateinit var separator: Drawable
    }

}