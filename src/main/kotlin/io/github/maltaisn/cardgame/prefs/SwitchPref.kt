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
import io.github.maltaisn.cardgame.widget.prefs.SwitchPrefView
import ktx.log.error


/**
 * A boolean preference that shows a switch to the user.
 */
class SwitchPref : GamePref() {

    /** The switch value. */
    var value = false
        set(value) {
            if (field != value) {
                field = value
                notifyValueChanged()
            }
        }

    /** The switch default value. */
    var defaultValue = false

    /** The checked state of the switch on which dependents are disabled. */
    var disableDependentsState = false


    override fun loadValue(prefs: Preferences) {
        value = try {
            prefs.getBoolean(key, defaultValue)
        } catch (e: Exception) {
            error { "Wrong saved type for preference '$key', using default value." }
            defaultValue
        }
    }

    override fun saveValue(prefs: Preferences) {
        @Suppress("LibGDXMissingFlush")
        prefs.putBoolean(key, value)
    }

    override fun createView(skin: Skin) = SwitchPrefView(skin, this)


    override fun toString() = super.toString().dropLast(1) +
            ", value: $value, defaultValue: $defaultValue]"

}