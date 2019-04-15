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
import io.github.maltaisn.cardgame.widget.prefs.TextPrefView
import ktx.log.error


/**
 * A string preference that shows a text field to the user.
 */
class TextPref : GamePref() {

    /** The field text. */
    var value = ""
        set(value) {
            if (field != value) {
                field = value
                notifyValueChanged()
            }
        }

    /** The field default text. */
    var defaultValue = ""

    /** The maximum number of characters that can be entered, or [NO_MAX_LENGTH] for no maximum. */
    var maxLength = NO_MAX_LENGTH

    /** A string of accepted input characters, or `null` for no filter. */
    var filter: String? = null


    override fun loadValue(prefs: Preferences) {
        value = try {
            prefs.getString(key, defaultValue)
        } catch (e: Exception) {
            error { "Wrong saved type for preference '$key', using default value." }
            defaultValue
        }
    }

    override fun saveValue(prefs: Preferences) {
        @Suppress("LibGDXMissingFlush")
        prefs.putString(key, value)
    }

    override fun createView(skin: Skin) = TextPrefView(skin, this)


    override fun toString() = super.toString().dropLast(1) +
            ", value: $value, defaultValue: $defaultValue, maxLength: $maxLength]"


    companion object {
        const val NO_MAX_LENGTH = 0
    }

}