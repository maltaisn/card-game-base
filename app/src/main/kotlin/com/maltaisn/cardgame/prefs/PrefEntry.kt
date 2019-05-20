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
 * An entry in a [GamePrefs] object.
 * Can be either a preference or a category.
 */
abstract class PrefEntry {

    /** Key under which the preference value is put in maps and saved. */
    lateinit var key: String

    /**
     * Whether the preference is enabled or not.
     * A preference can be enabled in a disabled category.
     */
    var enabled = true
        set(value) {
            field = value
            for (listener in listeners) {
                listener.onPreferenceEnabledStateChanged(this, enabled)
            }
        }

    /** The preference entry title. */
    var title = ""

    /** Optional dependency, the key of a switch preference. */
    var dependency: String? = null

    /** Listeners called when the value or enabled state of this preference has changed. */
    val listeners = mutableListOf<PrefListener>()


    protected fun notifyValueChanged() {
        for (listener in listeners) {
            listener.onPreferenceValueChanged(this)
        }
    }

    /** Create a view for this preference. */
    abstract fun createView(skin: Skin): PrefEntryView<out PrefEntry>


    override fun toString() = "[key: \"$key\", title: \"$title\"" +
            (if (dependency != null) ", dependency: \"$dependency\"" else "") +
            (if (!enabled) ", disabled" else "") + "]"


    interface PrefListener {
        /**
         * Called when the preference value is changed.
         */
        fun onPreferenceValueChanged(pref: PrefEntry) {}

        /**
         * Called when the preference enabled state is changed.
         */
        fun onPreferenceEnabledStateChanged(pref: PrefEntry, enabled: Boolean) {}
    }

}