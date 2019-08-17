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

package com.maltaisn.cardgame.stats

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.I18NBundle
import com.badlogic.gdx.utils.JsonReader
import com.maltaisn.cardgame.utils.StringRefJson
import ktx.json.addClassTag
import ktx.json.readValue


/**
 * Game statistics that can be loaded from JSON and inflated to a table of stat views.
 * Manages loading, saving and clearing values.
 */
class Statistics {

    /**
     * The names of the variants for this statistic object.
     * `null` if there are no variants.
     */
    val variants: List<String?>

    /**
     * The name under which the statistics are stored.
     * Variants are stored with a `_<index>` suffix.
     */
    val name: String

    /**
     * The map of statistics by key.
     */
    val stats: Map<String, Statistic<*>>
        get() = _stats

    private val _stats = mutableMapOf<String, Statistic<*>>()

    /**
     * The preferences where the stats values get stored for each variant.
     */
    private val preferences: Preferences


    constructor(name: String, variants: List<String?> = listOf(null)) {
        this.variants = variants
        this.name = name
        preferences = Gdx.app.getPreferences(name)
    }

    /**
     * Create a statistics object from a [file] and
     * using a [bundle] for resolving the string references.
     */
    constructor(file: FileHandle, bundle: I18NBundle) {
        // Create JSON parser
        val json = StringRefJson(bundle).apply {
            setTypeName("type")
            setUsePrototypes(false)

            // Add class tags
            addClassTag<NumberStat>("number")
            addClassTag<AverageStat>("average")
            addClassTag<PercentStat>("percent")
        }

        val data = JsonReader().parse(file)

        // Get name and create the preferences handles
        variants = data["variants"]?.asStringArray()?.toList() ?: listOf(null)
        name = checkNotNull(data["name"]?.asString()) { "JSON statistics file must specify a name attribute." }
        preferences = Gdx.app.getPreferences(name)

        // Create statistics entries for all variants
        val statsJson = data["stats"]
        if (statsJson != null) {
            // Parse statistics entries, and set the keys.
            // Then clone the statistics for all variants.
            _stats += json.readValue<LinkedHashMap<String, Statistic<*>>>(statsJson)
            for ((key, stat) in stats) {
                this[key] = stat
            }

            // Load stats values
            load()
        }
    }

    /** Get a statistic by [key]. */
    operator fun get(key: String) = stats[key]

    /** Set the statistic at [key] to a [stat]. */
    operator fun set(key: String, stat: Statistic<*>) {
        stat.key = key
        stat.initialize(variants.size)
        if (stat is CompositeStat<*>) {
            stat.setOtherStats(this)
        }
        _stats[key] = stat
    }

    /** Remove a statistic by [key]. */
    fun remove(key: String) = _stats.remove(key)

    fun getNumber(key: String) = checkNotNull(this[key] as? NumberStat) {
        "Invalid number statistic key: '$key'."
    }

    /**
     * Reset the value of all statistics.
     */
    fun reset() {
        for (stat in stats.values) {
            stat.reset()
        }
    }


    /**
     * Load values for all stats from the preferences file.
     */
    fun load() {
        for (stat in stats.values) {
            stat.loadValue(preferences)
        }
    }

    /**
     * Save values for all stats to the preferences files.
     */
    fun save() {
        for (stat in stats.values) {
            stat.saveValue(preferences)
        }
        preferences.flush()
    }

}
