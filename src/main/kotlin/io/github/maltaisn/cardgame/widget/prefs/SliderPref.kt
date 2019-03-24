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

package io.github.maltaisn.cardgame.widget.prefs

import com.badlogic.gdx.Preferences
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import io.github.maltaisn.cardgame.widget.SdfLabel
import io.github.maltaisn.cardgame.widget.Slider
import java.text.NumberFormat


/**
 * A float preference that shows a slider to the user.
 */
class SliderPref : GamePref {

    /** Slider minimum value. */
    var minValue = 0f

    /** Slider maximum value. */
    var maxValue = 100f

    /** Value by which the slider value is incremented. */
    var step = 1f

    /** The slider value. */
    var value = 0f

    /** The slider default value. */
    var defaultValue = 0f

    // JSON reflection constructor
    constructor() : super()

    constructor(key: String, title: String, help: String? = null,
                min: Float, max: Float, step: Float, default: Float) : super(key, title, help) {
        minValue = min
        maxValue = max
        this.step = step
        defaultValue = default
    }

    override fun loadValue(prefs: Preferences) {
        value = prefs.getFloat(key, defaultValue)
    }

    override fun saveValue(prefs: Preferences, flush: Boolean) {
        prefs.putFloat(key, value)
        if (flush) prefs.flush()
    }

    override fun createView(skin: Skin): Table {
        val style = skin[SliderPrefStyle::class.java]
        val table = Table()

        val helpIcon = if (help == null) null else style.helpIcon
        val label = PrefTitleLabel(title, skin, style.titleFontStyle, helpIcon)
        label.setWrap(true)
        label.setAlignment(Align.left)

        val valueLabel = SdfLabel(title, skin, style.valueFontStyle)
        valueLabel.setAlignment(Align.right)
        valueLabel.setText(NUMBER_FORMAT.format(value))

        val slider = Slider(style.sliderStyle)
        slider.minValue = minValue
        slider.maxValue = maxValue
        slider.step = step
        slider.progress = value
        slider.changeListener = {
            value = it
            valueLabel.setText(NUMBER_FORMAT.format(it))
        }

        table.pad(5f, 0f, 5f, 0f)
        table.add(label).growX().pad(5f, 10f, 5f, 10f)
        table.add(valueLabel).width(60f).pad(5f, 5f, 5f, 20f)
        table.row()
        table.add(slider).growX().colspan(2).pad(5f, 0f, 5f, 0f)
        return table
    }

    override fun toString() = super.toString().dropLast(1) + ", value: $value, defaultValue: $defaultValue]"


    class SliderPrefStyle : GamePrefStyle() {
        lateinit var valueFontStyle: SdfLabel.SdfLabelStyle
        lateinit var sliderStyle: Slider.SliderStyle
    }

    companion object {
        private val NUMBER_FORMAT = NumberFormat.getInstance()
    }

}