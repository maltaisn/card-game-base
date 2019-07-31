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


/**
 * Create game preferences from a map of keys and values.
 * For testing purposes only.
 */
fun buildGamePrefsFromMap(map: Map<String, Any>): GamePrefs {
    val prefs = GamePrefs(null)
    for ((key, value) in map) {
        prefs[key] = when {
            value is Boolean -> SwitchPref().apply {
                defaultValue = value
                this.value = value
            }
            value is Number -> SliderPref().apply {
                defaultValue = value.toFloat()
                this.value = defaultValue
                minValue = defaultValue
                maxValue = defaultValue
            }
            value is String -> ListPref().apply {
                defaultValue = value
                this.value = value
                entries = linkedMapOf(value to value)
            }
            value is Array<*> && value.first() is String -> PlayerNamesPref().apply {
                @Suppress("UNCHECKED_CAST")
                defaultValue = value as Array<String>
                this.value = value
            }
            else -> error("Unknown preference value type.")
        }
    }
    return prefs
}
