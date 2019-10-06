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
import com.maltaisn.cardgame.widget.prefs.GamePrefView


/**
 * The base class for a preference with a value.
 *
 * @property defaultValue The default value of this preference.
 * @property shortTitle Optional shorter title to use in menu drawer, use `null` to use normal title.
 * @property help Optional help message shown to the user, use `null` for no help message.
 * @property confirmChanges Whether a confirmation dialog must be shown to the user
 * before effectively changing the preference value.
 */
abstract class GamePref<T : Any>(
        key: String,
        title: String,
        dependency: String?,
        val defaultValue: T,
        val shortTitle: String? = null,
        val help: String? = null,
        val confirmChanges: Boolean = false)
    : PrefEntry(key, title, dependency) {

    /**
     * The preference value.
     */
    abstract var value: T

    /**
     * Listeners called when the value of this preference is changed.
     */
    val valueListeners = mutableListOf<PrefValueListener<T>>()


    /**
     * Has to be called when the preference value is changed to call the value listeners.
     */
    protected fun notifyValueChanged() {
        for (listener in valueListeners) {
            listener(this, value)
        }
    }

    /**
     * Load the value of this preference from [handle].
     */
    internal abstract fun loadValue(handle: Preferences): T

    /**
     * Save the value of this preference to [handle], if it's not null.
     * Doesn't flush the preferences, must be done afterwards.
     */
    internal abstract fun saveValue(handle: Preferences)


    abstract override fun createView(skin: Skin): GamePrefView<out GamePref<T>, T>


    abstract class Builder<T>(key: String) : PrefEntry.Builder(key) {
        var shortTitle: String? = null
        var help: String? = null
        var confirmChanges = false
        abstract var defaultValue: T
    }

    override fun toString() = "GamePref[" +
            "value: $value, " +
            "defaultValue: $defaultValue, " +
            "shortTitle: $shortTitle, " +
            "help: $help, " +
            "confirmChanges: $confirmChanges, " +
            super.toString().substringAfter("[")

}

typealias PrefValueListener<T> = (pref: GamePref<T>, value: T) -> Unit
