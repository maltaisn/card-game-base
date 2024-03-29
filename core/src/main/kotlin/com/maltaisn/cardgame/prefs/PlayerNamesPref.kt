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
import com.maltaisn.cardgame.prefs.PlayerNamesPref.Companion.NO_MAX_LENGTH
import com.maltaisn.cardgame.widget.prefs.PlayerNamesPrefView


/**
 * A preference for entering player names.
 * Names are saved under the preference key suffixed with "_<playerPos>"
 * These keys can be obtained with [getPlayerNameKey].
 *
 * @property inputTitle The title for the input window if input is delegated.
 * @property maxLength The maximum number of characters in a name, or [NO_MAX_LENGTH] for no maximum.
 */
class PlayerNamesPref(
        key: String,
        title: String,
        dependency: String?,
        defaultValue: Array<String>,
        shortTitle: String?,
        help: String?,
        confirmChanges: Boolean,
        val inputTitle: String?,
        val maxLength: Int)
    : GamePref<Array<String>>(key, title, dependency, defaultValue, shortTitle, help, confirmChanges) {

    override var value = emptyArray<String>()
        set(value) {
            field = value
            for (i in value.indices) {
                if (value[i] == defaultValue[i]) {
                    value[i] = ""
                }
            }
            notifyValueChanged()
        }

    /**
     * The number of players.
     */
    val size: Int
        get() = defaultValue.size


    override fun loadValue(handle: Preferences) = Array(size) {
        handle.getString(getPlayerNameKey(it), "")
    }

    @Suppress("GDXKotlinMissingFlush")
    override fun saveValue(handle: Preferences) {
        for ((i, name) in value.withIndex()) {
            handle.putString(getPlayerNameKey(i), name)
        }
    }

    /**
     * Get the player name, or the default value if none was set.
     */
    fun getPlayerName(player: Int): String {
        val name = value[player]
        return name.ifEmpty {
            defaultValue[player]
        }
    }

    /**
     * Get the key under which the name of a [player] is saved.
     */
    fun getPlayerNameKey(player: Int) = "${key}_$player"


    override fun createView(skin: Skin) = PlayerNamesPrefView(skin, this)


    class Builder(key: String) : GamePref.Builder<Array<String>>(key) {
        override var defaultValue: Array<String> = emptyArray()
        var inputTitle: String? = null
        var maxLength = NO_MAX_LENGTH

        fun build() = PlayerNamesPref(key, title, dependency, defaultValue, shortTitle,
                help, confirmChanges, inputTitle, maxLength)
    }


    override fun toString() = "PlayerNamesPref[" +
            "value: ${value.contentToString()}, " +
            "defaultValue: ${defaultValue.contentToString()}, " +
            "inputTitle: $inputTitle, " +
            "maxLength: $maxLength, " +
            super.toString().substringAfter("[")


    companion object {
        const val NO_MAX_LENGTH = 0
    }

}
