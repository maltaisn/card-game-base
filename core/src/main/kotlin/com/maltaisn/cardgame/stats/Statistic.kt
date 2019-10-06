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

import com.badlogic.gdx.Preferences
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.maltaisn.cardgame.widget.stats.StatView
import java.text.NumberFormat


/**
 * Base class for a game statistic.
 *
 * @property key The key under which the statistic value is put in maps and saved.
 * @property title The statistic title.
 * @property precision The maximum number of fraction digits of the number shown by this statistic.
 * @property internal Whether the statistic is for internal use or not.
 * Internal stats are not shown to the user.
 */
abstract class Statistic<T>(
        val key: String,
        val title: String,
        val precision: Int,
        val internal: Boolean) {

    /** A number format to be used to format this statistic value. */
    val numberFmt = NumberFormat.getNumberInstance().apply {
        maximumFractionDigits = precision
    }

    /**
     * Get the value of this statistic for a [variant].
     */
    abstract operator fun get(variant: Int = 0): T

    /**
     * Reset the value of all variants of this statistic.
     */
    abstract fun reset()

    /**
     * Initialize this statistic for a number of [variants].
     */
    internal abstract fun initialize(variants: Int)

    /**
     * Load the value of this statistic from [handle].
     * If no value is found, the value is initialized to the default value.
     */
    internal abstract fun loadValue(handle: Preferences)

    /**
     * Save the value of this statistic to [handle], if it's not null.
     * Doesn't flush the preferences, must be done afterwards.
     */
    internal abstract fun saveValue(handle: Preferences)

    /**
     * Create a view for this statistic.
     */
    abstract fun createView(skin: Skin): StatView<*>


    abstract class Builder(val key: String) {
        var title = ""
        var precision = 0
        var internal = false
    }

    override fun toString() = "Statistic[" +
            "key: '$key', " +
            "title: '$title', " +
            "precision: $precision, " +
            "internal: $internal]"

}
