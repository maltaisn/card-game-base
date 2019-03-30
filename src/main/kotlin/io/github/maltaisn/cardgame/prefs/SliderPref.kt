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

import com.badlogic.gdx.Preferences
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import io.github.maltaisn.cardgame.widget.prefs.SliderPrefView


/**
 * A float preference that shows a slider to the user.
 */
class SliderPref : GamePref() {

    /** Slider minimum value. */
    var minValue = 0f

    /** Slider maximum value. */
    var maxValue = 100f

    /** Value by which the slider value is incremented. */
    var step = 1f

    /** The slider value. */
    var value = 0f
        set(value) {
            field = value
            notifyValueChanged()
        }

    /** The slider default value. */
    var defaultValue = 0f

    /**
     * The array of text values to use for formatting instead of a number.
     * The first item of the array is applied to the lowest slider value.
     * Use `null` for the standard number display.
     */
    var enumValues: Array<String>? = null


    override fun loadValue(prefs: Preferences) {
        value = prefs.getFloat(key, defaultValue)
    }

    override fun saveValue(prefs: Preferences, flush: Boolean) {
        prefs.putFloat(key, value)
        if (flush) prefs.flush()
    }

    override fun createView(skin: Skin) = SliderPrefView(skin, this)


    override fun toString() = super.toString().dropLast(1) + ", value: $value, defaultValue: $defaultValue]"

}