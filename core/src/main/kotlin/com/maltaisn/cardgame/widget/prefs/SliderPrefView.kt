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

import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.Align
import com.maltaisn.cardgame.prefs.GamePref
import com.maltaisn.cardgame.prefs.SliderPref
import com.maltaisn.cardgame.widget.Slider
import com.maltaisn.msdfgdx.FontStyle
import com.maltaisn.msdfgdx.widget.MsdfLabel
import ktx.style.get
import java.text.NumberFormat


class SliderPrefView(skin: Skin, pref: SliderPref) :
        GamePrefView<SliderPref, Float>(skin, pref) {

    override var enabled
        get() = super.enabled
        set(value) {
            super.enabled = value
            valueLabel.isDisabled = !value
            slider.enabled = value
        }

    private val valueLabel: MsdfLabel
    private val slider: Slider


    init {
        val style: SliderPrefViewStyle = skin.get()

        valueLabel = MsdfLabel(getValueText(pref.value), skin, style.valueFontStyle)
        valueLabel.setAlignment(Align.right)

        slider = Slider(skin).apply {
            minProgress = pref.minValue
            maxProgress = pref.maxValue
            step = pref.step
            progress = pref.value
            changeListener = { valueLabel.txt = getValueText(it) }
            addCaptureListener(object : InputListener() {
                private var oldValue = 0f

                override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                    oldValue = progress
                    return true
                }

                override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                    if (progress != oldValue) {
                        changePreferenceValue(progress) { slideTo(oldValue) }
                    }
                }
            })
        }

        pad(10f, 0f, 10f, 0f)
        add(titleLabel).growX().pad(10f, 20f, 10f, 20f)
        add(valueLabel).pad(10f, 10f, 10f, 40f).row()
        add(slider).growX().colspan(2).pad(10f, 0f, 10f, 0f)

        this.enabled = enabled
    }

    override fun onPreferenceValueChanged(pref: GamePref<Float>, value: Float) {
        slider.progress = this.pref.value
    }

    private fun getValueText(progress: Float) = if (pref.enumValues == null) {
        NUMBER_FORMAT.format(progress)
    } else {
        val index = ((progress - pref.minValue) / pref.step).toInt()
        pref.enumValues!!.getOrElse(index) { NUMBER_FORMAT.format(progress) }
    }

    class SliderPrefViewStyle {
        lateinit var valueFontStyle: FontStyle
    }

    companion object {
        private val NUMBER_FORMAT = NumberFormat.getInstance()
    }

}
