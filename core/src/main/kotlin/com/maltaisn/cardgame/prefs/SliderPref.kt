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
import com.maltaisn.cardgame.widget.prefs.SliderPrefView


/**
 * A slider preference for a single number value.
 *
 * @property minValue Slider minimum value.
 * @property maxValue Slider maximum value.
 * @property step Value by which the slider value is incremented.
 * @property enumValues The list of text values to use for formatting instead of a number. The first
 * item of the list is applied to the lowest slider value. Use `null` for the standard number display.
 */
class SliderPref(
        key: String,
        title: String,
        dependency: String?,
        defaultValue: Float,
        shortTitle: String?,
        help: String?,
        confirmChanges: Boolean,
        val minValue: Float,
        val maxValue: Float,
        val step: Float,
        val enumValues: List<String>?)
    : GamePref<Float>(key, title, dependency, defaultValue, shortTitle, help, confirmChanges) {

    override var value = 0f
        set(value) {
            if (field == value) return
            field = value
            notifyValueChanged()
        }


    init {
        require(maxValue > minValue) { "Slider pref maximum value must be greater than minimum value." }
        require(step > 0f) { "Slider pref step must be greater than zero." }
    }


    override fun loadValue(handle: Preferences) = handle.getFloat(key, defaultValue)

    @Suppress("LibGDXMissingFlush")
    override fun saveValue(handle: Preferences) {
        handle.putFloat(key, value)
    }


    override fun createView(skin: Skin) = SliderPrefView(skin, this)


    class Builder(key: String) : GamePref.Builder<Float>(key) {
        override var defaultValue = 0f
        var minValue = 0f
        var maxValue = 100f
        var step = 1f
        var enumValues: List<String>? = null

        fun build() = SliderPref(key, title, dependency, defaultValue, shortTitle,
                help, confirmChanges, minValue, maxValue, step, enumValues)
    }


    override fun toString() = "SliderPref[" +
            "minValue: $minValue, " +
            "maxValue: $maxValue, " +
            "step: $step, " +
            "enumValues: $enumValues, " +
            super.toString().substringAfter("[")

}
