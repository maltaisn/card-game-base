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
import com.badlogic.gdx.utils.Disposable
import ktx.log.error


/**
 * A class for storing a hierarchy of [PrefEntry] preferences by key.
 * The class provides a builder utility for creating preferences.
 *
 * @property name The name under which the preferences are stored, can be `null` if not saveable.
 * @property prefs The map of preferences by key.
 */
class GamePrefs(val name: String,
                val prefs: Map<String, PrefEntry>) : Disposable {

    /**
     * The preferences handle where the values get stored.
     */
    private val handle: Preferences = Gdx.app.getPreferences(name)


    init {
        // Add dependency listeners.
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

        // Load values from handle.
        load()
    }


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
     * Load values from the file. If default values were not saved, save them.
     */
    fun load() {
        var prefsChanged = false
        forEachPref {
            if (it is GamePref<*>) {
                loadPrefValue(it)
                if (!handle.contains(it.key)) {
                    it.saveValue(handle)
                    prefsChanged = true
                }
            }
        }
        if (prefsChanged) {
            handle.flush()
        }
    }

    /**
     * Save the value of all entries to the file.
     */
    fun save() {
        forEachPref {
            if (it is GamePref<*>) {
                it.saveValue(handle)
            }
        }
        handle.flush()
    }


    /**
     * Try to load a preference value from the preferences [handle].
     * If load fails because value type is wrong, default value will be used.
     */
    private fun <T : Any> loadPrefValue(pref: GamePref<T>) {
        pref.value = try {
            pref.loadValue(handle)
        } catch (e: Exception) {
            error { "Wrong saved type for preference '${pref.key}', using default value." }
            pref.defaultValue
        }
    }

    /**
     * Add a preference value listener for all preferences.
     */
    fun addValueListener(listener: PrefValueListener<Any>) {
        forEachPref {
            @Suppress("UNCHECKED_CAST")
            (it as GamePref<Any>).valueListeners.add(listener)
        }
    }

    /**
     * Remove a preference value listener for all preferences.
     */
    fun removeValueListener(listener: PrefValueListener<Any>) {
        forEachPref {
            @Suppress("UNCHECKED_CAST")
            (it as GamePref<Any>).valueListeners.remove(listener)
        }
    }

    override fun dispose() {
        // Clear all preference listeners.
        forEachPref(true) {
            it.enabledListeners.clear()
            if (it is GamePref<*>) {
                it.valueListeners.clear()
            }
        }
    }


    /**
     * Builder class providing DSL for populating a [GamePrefs] object.
     */
    class Builder(val name: String) : CategoryBuilder() {

        inline fun category(key: String, build: PrefCategory.Builder.() -> Unit) {
            val builder = PrefCategory.Builder(key)
            build(builder)
            prefs[key] = builder.build()
        }

        fun build() = GamePrefs(name, prefs)
    }

    /**
     * Builder class providing DSL for populating a [PrefCategory] object.
     */
    abstract class CategoryBuilder {

        val prefs = mutableMapOf<String, PrefEntry>()

        inline fun switch(key: String, build: SwitchPref.Builder.() -> Unit) {
            val builder = SwitchPref.Builder(key)
            build(builder)
            prefs[key] = builder.build()
        }

        inline fun slider(key: String, build: SliderPref.Builder.() -> Unit) {
            val builder = SliderPref.Builder(key)
            build(builder)
            prefs[key] = builder.build()
        }

        inline fun text(key: String, build: TextPref.Builder.() -> Unit) {
            val builder = TextPref.Builder(key)
            build(builder)
            prefs[key] = builder.build()
        }

        inline fun list(key: String, build: ListPref.Builder.() -> Unit) {
            val builder = ListPref.Builder(key)
            build(builder)
            prefs[key] = builder.build()
        }

        inline fun playerNames(key: String, build: PlayerNamesPref.Builder.() -> Unit) {
            val builder = PlayerNamesPref.Builder(key)
            build(builder)
            prefs[key] = builder.build()
        }
    }


    override fun toString() = "GamePrefs[name: '$name', ${prefs.size} prefs]"


    companion object {

        /**
         * Utility function to create new game preferences using DSL builder syntax.
         */
        inline operator fun invoke(name: String, build: Builder.() -> Unit = {}): GamePrefs {
            val builder = Builder(name)
            build(builder)
            return builder.build()
        }
    }

}
