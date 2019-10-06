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
import com.maltaisn.cardgame.prefs.TextPref.Companion.NO_MAX_LENGTH
import com.maltaisn.cardgame.widget.prefs.TextPrefView


/**
 * A switch preference for a single boolean value.
 *
 * @property inputTitle The title for the input window if input is delegated.
 * @property maxLength The maximum number of characters that can be entered, or [NO_MAX_LENGTH] for no maximum.
 * @property filter A string of accepted input characters, or `null` for no filter.
 */
class TextPref(
        key: String,
        title: String,
        dependency: String?,
        defaultValue: String,
        shortTitle: String?,
        help: String?,
        confirmChanges: Boolean,
        val inputTitle: String?,
        val maxLength: Int,
        val filter: String?)
    : GamePref<String>(key, title, dependency, defaultValue, shortTitle, help, confirmChanges) {

    override var value = ""
        set(value) {
            if (field == value) return
            field = value
            notifyValueChanged()
        }


    override fun loadValue(prefs: Preferences) = prefs.getString(key, defaultValue)

    @Suppress("LibGDXMissingFlush")
    override fun saveValue(prefs: Preferences) {
        prefs.putString(key, value)
    }


    override fun createView(skin: Skin) = TextPrefView(skin, this)


    class Builder(key: String) : GamePref.Builder<String>(key) {
        override var defaultValue = ""
        var inputTitle: String? = null
        var maxLength = NO_MAX_LENGTH
        var filter: String? = null

        fun build() = TextPref(key, title, dependency, defaultValue, shortTitle,
                help, confirmChanges, inputTitle, maxLength, filter)
    }


    override fun toString() = "TextPref[" +
            "inputTitle: $inputTitle" +
            "maxLength: $maxLength" +
            "filter: $filter" +
            super.toString().substringAfter("[")


    companion object {
        const val NO_MAX_LENGTH = 0
    }

}
