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

package io.github.maltaisn.cardgame.prefs

import com.badlogic.gdx.Preferences
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import io.github.maltaisn.cardgame.widget.prefs.ListPrefView


/**
 * A preference that shows a list to the user.
 * Each list item have a key, used to save them, and a value displayed.
 */
class ListPref : GamePref() {

    /** The selected item key. */
    var value: String? = null
        set(value) {
            if (field != value) {
                field = value
                notifyValueChanged()
            }
        }

    /** The default selected item key. */
    var defaultValue: String? = null

    /** The map of item values by keys. */
    lateinit var entries: LinkedHashMap<String, String>

    val keys by lazy { entries.keys.toList() }
    val values by lazy { entries.values.toList() }

    /** The display text for the selected item. */
    val displayValue: String
        get() = checkNotNull(entries[value]) { "Unknown list preference item key '$value'." }


    override fun loadValue(prefs: Preferences) {
        if (defaultValue == null) {
            defaultValue = keys.first()
        }
        value = prefs.getString(key, defaultValue)
    }

    override fun saveValue(prefs: Preferences) {
        if (entries.containsKey(value)) {
            @Suppress("LibGDXMissingFlush")
            prefs.putString(key, value)
        }
    }

    override fun createView(skin: Skin) = ListPrefView(skin, this)

    override fun toString() = super.toString().dropLast(1) +
            ", value: $value, defaultValue: $defaultValue, ${entries.size} items]"

}