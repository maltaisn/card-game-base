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
import com.maltaisn.cardgame.widget.stats.NumberStatView
import ktx.log.error


/**
 * A statistic for a single number.
 */
class NumberStat : Statistic<Float>() {

    private lateinit var value: FloatArray

    /** The value used to reset the statistic. */
    var defaultValue = 0f


    override fun get(variant: Int) = value[variant]

    operator fun set(variant: Int, value: Float) {
        this.value[variant] = value
    }

    operator fun set(variant: Int, value: Int) {
        this[variant] = value.toFloat()
    }

    override fun reset() {
        value.fill(defaultValue)
    }

    override fun initialize(variants: Int) {
        value = FloatArray(variants)
    }

    override fun loadValue(prefs: Preferences) {
        for (i in 0 until value.size) {
            val variantKey = "${key}_$i"
            value[i] = try {
                prefs.getFloat(variantKey, defaultValue)
            } catch (e: Exception) {
                error { "Wrong saved type for statistic '$variantKey', resetting." }
                defaultValue
            }
        }
    }


    @Suppress("LibGDXMissingFlush")
    override fun saveValue(prefs: Preferences) {
        for (variant in 0 until value.size) {
            prefs.putFloat("${key}_$variant", value[variant])
        }
    }

    override fun createView(skin: Skin) = NumberStatView(skin, this)

}
