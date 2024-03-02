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

import com.badlogic.gdx.Preferences
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.maltaisn.cardgame.widget.prefs.ListPrefView


/**
 * A list preference for an enum value from a list of strings.
 *
 * @property entries The map of item values by keys.
 */
class ListPref(
        key: String,
        title: String,
        dependency: String?,
        defaultValue: String,
        shortTitle: String?,
        help: String?,
        confirmChanges: Boolean,
        val entries: Map<String, String>)
    : GamePref<String>(key, title, dependency, defaultValue, shortTitle, help, confirmChanges) {

    override var value = NO_VALUE
        set(value) {
            if (field == value) return
            field = value
            notifyValueChanged()
        }

    val keys by lazy { entries.keys.toList() }
    val values by lazy { entries.values.toList() }

    /**
     * The display text for the selected item.
     */
    val displayValue: String
        get() = checkNotNull(entries[value]) { "Unknown list preference item key '$value'." }


    override fun loadValue(handle: Preferences): String {
        val value = handle.getString(key, defaultValue)
        return if (value !in keys) defaultValue else value
    }

    @Suppress("GDXKotlinMissingFlush")
    override fun saveValue(handle: Preferences) {
        if (entries.containsKey(value)) {
            handle.putString(key, value)
        }
    }


    override fun createView(skin: Skin) = ListPrefView(skin, this)


    class Builder(key: String) : GamePref.Builder<String>(key) {
        override var defaultValue = NO_VALUE
        val entries = mutableMapOf<String, String>()

        fun build(): ListPref {
            require(entries.isNotEmpty()) { "List preference must have at least one entry." }

            if (defaultValue == NO_VALUE) {
                defaultValue = entries.keys.first()
            }
            return ListPref(key, title, dependency, defaultValue, shortTitle,
                    help, confirmChanges, entries)
        }
    }


    override fun toString() = "ListPref[entries: $entries, " +
            super.toString().substringAfter("[")


    companion object {
        /**
         * Placeholder value to indicate that there's no value selected in list preference.
         */
        const val NO_VALUE = ""
    }

}
