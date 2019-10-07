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


/**
 * Game statistics that can be created with a DSL and inflated to a table of stat views.
 * Manages loading, saving and clearing values.
 *
 * @property name The name under which the statistics are stored.
 * Variants are stored with a `_<index>` suffix.
 * @property variants The names of the variants for this statistic object.
 * @property stats The map of statistics by key.
 */
class Statistics(
        val name: String,
        val variants: List<String>,
        val stats: Map<String, Statistic<*>>) {

    /**
     * The preferences where the stats values get stored for each variant.
     */
    private val handle: Preferences = Gdx.app.getPreferences(name)


    init {
        require(variants.isNotEmpty()) { "There must be at least one variant." }

        // Initialize statistic objects
        for (stat in stats.values) {
            stat.initialize(variants.size)
            if (stat is CompositeStat<*>) {
                stat.setOtherStats(this)
            }
        }

        // Load saved values from handle.
        load()
    }


    /**
     * Get a statistic by [key].
     */
    operator fun get(key: String) = stats[key]


    fun getNumber(key: String) = checkNotNull(this[key] as? NumberStat) {
        "Invalid number statistic key: '$key'."
    }

    /**
     * Reset the value of all statistics and save it to handle.
     */
    fun reset() {
        for (stat in stats.values) {
            stat.reset()
        }
        save()
    }

    /**
     * Load values for all stats from the preferences handle.
     */
    fun load() {
        for (stat in stats.values) {
            stat.loadValue(handle)
        }
    }

    /**
     * Save values for all stats to the preferences handle.
     */
    fun save() {
        for (stat in stats.values) {
            stat.saveValue(handle)
        }
        handle.flush()
    }

    class Builder(val name: String) {
        val stats = mutableMapOf<String, Statistic<*>>()
        var variants: List<String> = listOf("default")

        inline fun number(key: String, init: (@StatsDsl NumberStat.Builder).() -> Unit) {
            stats[key] = NumberStat.Builder(key).apply(init).build()
        }

        inline fun percent(key: String, init: (@StatsDsl PercentStat.Builder).() -> Unit) {
            stats[key] = PercentStat.Builder(key).apply(init).build()
        }

        inline fun average(key: String, init: (@StatsDsl AverageStat.Builder).() -> Unit) {
            stats[key] = AverageStat.Builder(key).apply(init).build()
        }

        fun build() = Statistics(name, variants, stats)
    }


    companion object {
        /**
         * Utility function to create new game statistics using DSL builder syntax.
         */
        inline operator fun invoke(name: String, init: (@StatsDsl Builder).() -> Unit = {}) =
                Builder(name).apply(init).build()
    }


    override fun toString() = "Statistics[" +
            "name: '$name', " +
            "variants: $variants, " +
            "${stats.size} stats]"

}
