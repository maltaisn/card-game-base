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

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.I18NBundle
import com.badlogic.gdx.utils.Json
import com.badlogic.gdx.utils.JsonValue
import com.maltaisn.cardgame.addClassTag
import com.maltaisn.cardgame.fromJson
import com.maltaisn.cardgame.readValue
import kotlin.collections.component1
import kotlin.collections.component2


/**
 * Game preferences that can be loaded from JSON and inflated to a table of preference views.
 * Manages loading and saving values, setting default values.
 *
 * To get a preference object, use `gamePrefs["key"]`.
 * The preference value can be changed, listeners can be attached to get notified of changes.
 */
class GamePrefs {

    /** The name under which the preferences are stored. */
    val name: String?

    /** The map of preferences and categories by key, can be safely edited. */
    val prefs = linkedMapOf<String, PrefEntry>()

    /**
     * The preferences where the values get stored.
     */
    private val preferences: Preferences?

    /**
     * Create empty game preferences object with a [name].
     * Name can be `null` for test preferences.
     */
    constructor(name: String?) {
        this.name = name
        preferences = if (name != null) {
            Gdx.app.getPreferences(name)
        } else {
            null
        }
    }

    /**
     * Load game preferences to this preferences object, from a [file] and using a [bundle] for resolving
     * the string references. Any previous preferences are cleared.
     */
    constructor(file: FileHandle, bundle: I18NBundle) {
        // Create JSON parser
        val json = object : Json() {
            init {
                setTypeName("type")
                setUsePrototypes(false)

                // Add class tags
                addClassTag<PrefCategory>("category")
                addClassTag<SwitchPref>("switch")
                addClassTag<SliderPref>("slider")
                addClassTag<ListPref>("list")
                addClassTag<TextPref>("text")
                addClassTag<PlayerNamesPref>("player-names")
            }

            override fun <T : Any?> readValue(type: Class<T>?, elementType: Class<*>?, jsonData: JsonValue): T {
                if (jsonData.isString && jsonData.asString().startsWith("@string/")) {
                    // The string is a reference, resolve it.
                    @Suppress("UNCHECKED_CAST")
                    return bundle[jsonData.asString().substring(8)] as T
                }
                return super.readValue(type, elementType, jsonData)
            }
        }

        val data: HashMap<String, Any> = json.fromJson(file)

        // Get name and create a preferences handle
        name = checkNotNull(data["name"] as? String) { "JSON preferences file must specify a name attribute." }
        preferences = Gdx.app.getPreferences(name)

        // Load preference entries
        val entryData = data["prefs"] as? JsonValue
        if (entryData != null) {
            // Parse preference entries, and set the keys
            prefs += json.readValue<LinkedHashMap<String, PrefEntry>>(entryData)
            for ((key, pref) in prefs.entries) {
                pref.key = key
                if (pref is PrefCategory) {
                    for ((subKey, subPref) in pref.prefs) {
                        subPref.key = subKey
                    }
                }
            }

            load()
        }

        // Add dependency listeners
        forEachPref(true) { dependant ->
            val dependency = dependant.dependency
            if (dependency != null) {
                val depPref = this[dependency]
                checkNotNull(depPref) { "Preference '${dependant.key}' has dependency that doesn't exists." }
                check(depPref is SwitchPref) { "Preference '${dependant.key}' has dependency that isn't a switch." }
                depPref.valueListeners += { _, _ ->
                    dependant.enabled = (depPref.value != depPref.disableDependentsState)
                }
                dependant.enabled = (depPref.value != depPref.disableDependentsState)
            }
        }
    }

    /**
     * Do an [action] on each game preference, including those inside categories.
     * @param includeCategories Whether to also do the action on categories.
     */
    fun forEachPref(includeCategories: Boolean = false, action: (PrefEntry) -> Unit) {
        for (pref in prefs.values) {
            if (pref is PrefCategory) {
                if (includeCategories) {
                    action(pref)
                }
                for (subPref in pref.prefs.values) {
                    action(subPref)
                }
            } else {
                action(pref)
            }
        }
    }

    /**
     * Set or replace a preference by key.
     */
    operator fun set(key: String, pref: PrefEntry) {
        prefs[key] = pref
    }

    /**
     * Get a preference by key by searching in all categories.
     * Returns `null` if the preference key doesn't exist.
     */
    operator fun get(key: String): PrefEntry? {
        var entry = prefs[key]
        if (entry != null) {
            return entry
        } else {
            for (pref in prefs.values) {
                if (pref is PrefCategory) {
                    entry = pref.prefs[key]
                    if (entry != null) {
                        return entry
                    }
                }
            }
            return null
        }
    }

    /**
     * Load values from the file. If default values were not saved, save them.
     */
    fun load() {
        checkNotNull(preferences) { "Cannot save preferences without name." }

        var prefsChanged = false
        forEachPref {
            if (it is GamePref<*>) {
                it.loadValue(preferences)
                if (!preferences.contains(it.key)) {
                    it.saveValue(preferences)
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
        checkNotNull(preferences) { "Cannot save preferences without name." }

        forEachPref {
            if (it is GamePref<*>) {
                it.saveValue(preferences)
            }
        }
        preferences.flush()
    }


    fun getBoolean(key: String) = checkNotNull((this[key] as SwitchPref?)?.value) {
        "Invalid switch preference key '$key'."
    }

    fun getFloat(key: String) = checkNotNull((this[key] as SliderPref?)?.value) {
        "Invalid slider preference key '$key'."
    }

    fun getInt(key: String) = getFloat(key).toInt()

    fun getChoice(key: String) = checkNotNull((this[key] as ListPref?)?.value) {
        "Invalid list preference key '$key'."
    }

    fun getString(key: String) = checkNotNull((this[key] as TextPref?)?.value) {
        "Invalid text preference key '$key'."
    }

    /**
     * Add a preference value listener for all preferences.
     */
    fun addValueListener(listener: (GamePref<Any?>, Any?) -> Unit) {
        forEachPref(true) {
            @Suppress("UNCHECKED_CAST")
            (it as? GamePref<Any?>)?.valueListeners?.add(listener)
        }
    }

    /**
     * Remove a preference value listener for all preferences.
     */
    fun removeValueListener(listener: (GamePref<Any?>, Any?) -> Unit) {
        forEachPref(true) {
            @Suppress("UNCHECKED_CAST")
            (it as? GamePref<Any?>)?.valueListeners?.remove(listener)
        }
    }

    /**
     * Clear all preference listeners.
     * Preference object should not be used after calling this.
     */
    fun clearAllListeners() {
        for (pref in prefs.values) {
            pref.enabledListeners.clear()
            (pref as? GamePref<*>)?.valueListeners?.clear()
        }
    }

    override fun toString() = "[name: \"$name\", ${prefs.size} entries]"

}
