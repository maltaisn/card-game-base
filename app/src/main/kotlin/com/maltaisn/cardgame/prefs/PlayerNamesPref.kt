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
import com.maltaisn.cardgame.widget.prefs.PlayerNamesPrefView
import ktx.log.error


/**
 * A preference to enter the names of the players.
 * Names are saved under the preference key suffixed with "_<playerPos>"
 * These keys can be obtained with [getPlayerNameKey].
 */
class PlayerNamesPref : GamePref<Array<String>>() {

    /**
     * The player names array. Should be an immutable list but reflection won't allow it.
     * The value should be set everytime the array is changed.
     */
    override var value = emptyArray<String>()
        set(value) {
            field = value
            notifyValueChanged()
        }

    /** The default player names array. */
    lateinit var defaultValue: Array<String>

    /** The maximum number of characters in a name, or [NO_MAX_LENGTH] for no maximum. */
    var maxLength = NO_MAX_LENGTH

    /** A string of accepted input characters, or `null` for no filter. */
    var filter: String? = null

    /** The number of players. */
    val size: Int
        get() = defaultValue.size


    override fun loadValue(prefs: Preferences) {
        value = Array(size) {
            val playerKey = getPlayerNameKey(it)
            try {
                prefs.getString(playerKey, defaultValue[it])
            } catch (e: Exception) {
                error { "Wrong saved type for preference '$playerKey', using default value." }
                defaultValue[it]
            }
        }
    }

    override fun saveValue(prefs: Preferences) {
        @Suppress("LibGDXMissingFlush")
        for ((i, name) in value.withIndex()) {
            prefs.putString(getPlayerNameKey(i), name)
        }
    }

    /** Get the key under which the name of a [player] is saved. */
    fun getPlayerNameKey(player: Int) = "${key}_$player"


    override fun createView(skin: Skin) = PlayerNamesPrefView(skin, this)


    override fun toString() = super.toString().dropLast(1) + ", value: ${value.contentToString()}, " +
            "defaultValue: ${defaultValue.contentToString()}, maxLength: $maxLength]"

    companion object {
        const val NO_MAX_LENGTH = 0
    }

}
