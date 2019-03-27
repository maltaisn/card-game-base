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

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.I18NBundle
import com.badlogic.gdx.utils.Json
import com.badlogic.gdx.utils.JsonValue
import kotlin.collections.component1
import kotlin.collections.component2


/**
 * Game preferences that can be loaded from JSON and inflated to a vertical group of preferences.
 * Manages loading and saving values, setting default values.
 */
class GamePrefs {

    /** The name under which the preferences are stored. */
    val name: String

    /** The preferences where the values get stored. */
    val preferences: Preferences

    /** The list of preference entries, can be safely edited. */
    val entries = mutableListOf<PrefEntry>()

    /** Create empty game preferences object with a [name]. */
    constructor(name: String) {
        this.name = name
        preferences = Gdx.app.getPreferences(name)
    }

    /**
     * Load game preferences to this preferences object, from a [file] and using a [bundle] for resolving
     * the string references. Any previous preferences are cleared.
     */
    @Suppress("UNCHECKED_CAST")
    constructor(file: FileHandle, bundle: I18NBundle) {
        entries.clear()

        // Create JSON parser
        val json = object : Json() {
            override fun <T : Any?> readValue(type: Class<T>?, elementType: Class<*>?, jsonData: JsonValue): T {
                if (jsonData.isString && jsonData.asString().startsWith("@string/")) {
                    // The string is a reference, resolve it.
                    @Suppress("UNCHECKED_CAST")
                    return bundle.get(jsonData.asString().substring(8)) as T
                }
                return super.readValue(type, elementType, jsonData)
            }
        }
        json.setTypeName("type")
        json.setUsePrototypes(false)
        for ((tag, type) in CLASS_TAGS) {
            json.addClassTag(tag, type)
        }

        val data = json.fromJson(HashMap::class.java, file) as HashMap<String, Any>

        // Get name and create a preferences handle
        name = data["name"] as? String
                ?: throw IllegalStateException("JSON preferences file must specify a name attribute.")
        preferences = Gdx.app.getPreferences(name)

        // Load preference entries
        val entryData = data["prefs"] as? JsonValue
        if (entryData != null) {
            // Parse preference entries, add set the keys
            val prefMap = json.readValue(LinkedHashMap::class.java, entryData) as LinkedHashMap<String, PrefEntry>
            for ((key, pref) in prefMap.entries) {
                if (pref is GamePref) {
                    pref.key = key
                }
                entries += pref
            }

            load()
        }
    }


    /**
     * Load values from the file. If default values were not saved, save them.
     */
    fun load() {
        var prefsChanged = false
        for (pref in entries) {
            if (pref is GamePref) {
                pref.loadValue(preferences)
                val key = pref.key
                if (!preferences.contains(key)) {
                    pref.saveValue(preferences, false)
                    prefsChanged = true
                }
            }
        }
        if (prefsChanged) {
            preferences.flush()
        }
    }

    /**
     * Save the value of all entries to the file.
     */
    fun save() {
        for (pref in entries) {
            if (pref is GamePref) {
                pref.saveValue(preferences, false)
            }
        }
        preferences.flush()
    }

    companion object {
        private val CLASS_TAGS = arrayOf(
                "category" to PrefCategory::class.java,
                "switch" to SwitchPref::class.java,
                "slider" to SliderPref::class.java)
    }

}