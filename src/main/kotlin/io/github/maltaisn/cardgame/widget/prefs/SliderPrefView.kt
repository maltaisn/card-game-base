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

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.Align
import io.github.maltaisn.cardgame.prefs.SliderPref
import io.github.maltaisn.cardgame.widget.SdfLabel
import io.github.maltaisn.cardgame.widget.Slider
import java.text.NumberFormat


class SliderPrefView(skin: Skin, pref: SliderPref) : GamePrefView<SliderPref>(skin, pref) {

    init {
        val style = skin[SliderPrefViewStyle::class.java]

        val valueLabel = SdfLabel(NUMBER_FORMAT.format(pref.value), skin, style.valueFontStyle)
        valueLabel.setAlignment(Align.right)

        val slider = Slider(style.sliderStyle).apply {
            minValue = pref.minValue
            maxValue = pref.maxValue
            step = pref.step
            progress = pref.value
            changeListener = {
                pref.value = it
                valueLabel.setText(NUMBER_FORMAT.format(it))
            }
        }

        pad(5f, 0f, 5f, 0f)
        add(titleLabel).growX().pad(5f, 10f, 5f, 10f)
        add(valueLabel).width(60f).pad(5f, 5f, 5f, 20f).row()
        add(slider).growX().colspan(2).pad(5f, 0f, 5f, 0f)
    }

    class SliderPrefViewStyle {
        lateinit var valueFontStyle: SdfLabel.FontStyle
        lateinit var sliderStyle: Slider.SliderStyle
    }

    companion object {
        private val NUMBER_FORMAT = NumberFormat.getInstance()
    }

}