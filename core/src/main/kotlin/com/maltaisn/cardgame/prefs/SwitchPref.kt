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
import com.maltaisn.cardgame.widget.prefs.SwitchPrefView


/**
 * A switch preference for a single boolean value.
 *
 * @property disableDependentsState The checked state of the switch on which dependents are disabled.
 */
class SwitchPref(
        key: String,
        title: String,
        dependency: String?,
        defaultValue: Boolean,
        shortTitle: String?,
        help: String?,
        confirmChanges: Boolean,
        val disableDependentsState: Boolean)
    : GamePref<Boolean>(key, title, dependency, defaultValue, shortTitle, help, confirmChanges) {

    override var value = false
        set(value) {
            if (field == value) return
            field = value
            notifyValueChanged()
        }


    override fun loadValue(handle: Preferences) = handle.getBoolean(key, defaultValue)

    @Suppress("LibGDXMissingFlush")
    override fun saveValue(handle: Preferences) {
        handle.putBoolean(key, value)
    }


    override fun createView(skin: Skin) = SwitchPrefView(skin, this)


    class Builder(key: String) : GamePref.Builder<Boolean>(key) {
        override var defaultValue = false
        var disableDependentsState = false

        fun build() = SwitchPref(key, title, dependency, defaultValue, shortTitle,
                help, confirmChanges, disableDependentsState)
    }


    override fun toString() = "SwitchPref[" +
            "disableDependentsState: $disableDependentsState, " +
            super.toString().substringAfter("[")

}
