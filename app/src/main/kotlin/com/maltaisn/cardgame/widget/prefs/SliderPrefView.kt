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

package com.maltaisn.cardgame.widget.prefs

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.Align
import com.maltaisn.cardgame.prefs.PrefEntry
import com.maltaisn.cardgame.prefs.SliderPref
import com.maltaisn.cardgame.widget.FontStyle
import com.maltaisn.cardgame.widget.SdfLabel
import com.maltaisn.cardgame.widget.Slider
import ktx.style.get
import java.text.NumberFormat


class SliderPrefView(skin: Skin, pref: SliderPref) : GamePrefView<SliderPref>(skin, pref) {

    override var enabled
        get() = super.enabled
        set(value) {
            super.enabled = value
            valueLabel.enabled = value
            slider.enabled = value
        }

    private val valueLabel: SdfLabel
    private val slider: Slider

    /** The value label text to display. */
    private val valueText: String
        get() = if (pref.enumValues == null) {
            NUMBER_FORMAT.format(pref.value)
        } else {
            val index = ((pref.value - pref.minValue) / pref.step).toInt()
            pref.enumValues!!.getOrElse(index) { NUMBER_FORMAT.format(pref.value) }
        }

    init {
        val style: SliderPrefViewStyle = skin.get()

        valueLabel = SdfLabel(skin, style.valueFontStyle, valueText)
        valueLabel.setAlignment(Align.right)

        slider = Slider(skin).apply {
            minProgress = pref.minValue
            maxProgress = pref.maxValue
            step = pref.step
            progress = pref.value
            changeListener = {
                pref.value = it
                valueLabel.setText(valueText)
            }
        }

        pad(10f, 0f, 10f, 0f)
        add(titleLabel).growX().pad(10f, 20f, 10f, 20f)
        add(valueLabel).pad(10f, 10f, 10f, 40f).row()
        add(slider).growX().colspan(2).pad(10f, 0f, 10f, 0f)

        this.enabled = enabled
    }

    override fun onPreferenceValueChanged(pref: PrefEntry) {
        slider.progress = this.pref.value
    }


    class SliderPrefViewStyle {
        lateinit var valueFontStyle: FontStyle
    }

    companion object {
        private val NUMBER_FORMAT = NumberFormat.getInstance()
    }

}
