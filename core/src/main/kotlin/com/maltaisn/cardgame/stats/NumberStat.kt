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
 * A statistic with a number value.
 */
class NumberStat(
        key: String,
        title: String,
        precision: Int,
        internal: Boolean,
        val defaultValue: Float)
    : Statistic<Float>(key, title, precision, internal) {

    private var value = FloatArray(0)


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

    override fun loadValue(handle: Preferences) {
        for (i in value.indices) {
            val variantKey = "${key}_$i"
            value[i] = try {
                handle.getFloat(variantKey, defaultValue)
            } catch (e: Exception) {
                error { "Wrong saved type for statistic '$variantKey', resetting." }
                defaultValue
            }
        }
    }


    @Suppress("GDXKotlinMissingFlush")
    override fun saveValue(handle: Preferences) {
        for (variant in value.indices) {
            handle.putFloat("${key}_$variant", value[variant])
        }
    }

    override fun createView(skin: Skin) = NumberStatView(skin, this)


    class Builder(key: String) : Statistic.Builder(key) {
        var defaultValue = 0f

        fun build() = NumberStat(key, title, precision, internal, defaultValue)
    }


    override fun toString() = "NumberStat[" +
            "value: ${value.contentToString()}, " +
            "defaultValue: $defaultValue, " +
            super.toString().substringAfter("[")

}
