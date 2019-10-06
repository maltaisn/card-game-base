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

package com.maltaisn.cardgame.prefs

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.maltaisn.cardgame.widget.prefs.PrefEntryView


/**
 * The base class for a preference in [GamePrefs].
 *
 * @property key The preference key, should be unique in its [GamePrefs] hierarchy.
 * @property title The preference title
 * @property dependency The key of the preference of which this preference
 * depends to be enabled. Must be a key of a [SwitchPref].
 */
abstract class PrefEntry(
        val key: String,
        val title: String,
        val dependency: String? = null) {

    /**
     * Whether the preference entry is enabled or not.
     */
    var enabled = true
        set(value) {
            if (field == value) return
            field = value
            for (listener in enabledListeners) {
                listener(this, enabled)
            }
        }

    /**
     * Listeners called when the enabled state of this preference is changed.
     */
    val enabledListeners = mutableListOf<PrefEnabledListener>()


    abstract fun createView(skin: Skin): PrefEntryView<*>


    abstract class Builder(val key: String) {
        var title = ""
        var dependency: String? = null
        var enabled = true
    }

    override fun toString() = "PrefEntry[" +
            "key: '$key', " +
            "title: '$title', " +
            "dependency: $dependency, " +
            "enabled: $enabled]"

}

typealias PrefEnabledListener = (pref: PrefEntry, enabled: Boolean) -> Unit
