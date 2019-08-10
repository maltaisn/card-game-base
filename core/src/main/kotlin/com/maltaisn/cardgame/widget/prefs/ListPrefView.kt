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

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.utils.Scaling
import com.maltaisn.cardgame.prefs.GamePref
import com.maltaisn.cardgame.prefs.ListPref
import com.maltaisn.cardgame.widget.text.FontStyle
import com.maltaisn.cardgame.widget.text.SdfLabel
import ktx.actors.alpha
import ktx.actors.onClick
import ktx.style.get


class ListPrefView(skin: Skin, pref: ListPref) :
        GamePrefView<ListPref, String?>(skin, pref) {

    override var enabled
        get() = super.enabled
        set(value) {
            super.enabled = value
            valueLabel.enabled = value
            arrowIcon.alpha = if (value) 1f else 0.5f
        }


    private val valueLabel: SdfLabel
    private val arrowIcon: Image


    init {
        val style: ListPrefViewStyle = skin.get()

        valueLabel = SdfLabel(skin, style.valueFontStyle, pref.displayValue)
        valueLabel.enabled = enabled

        arrowIcon = Image(style.arrowIcon, Scaling.fit)
        arrowIcon.color = style.arrowIconColor

        val valueBtn = Table().apply {
            touchable = Touchable.enabled
            add(valueLabel).padRight(20f)
            add(arrowIcon).size(style.valueFontStyle.fontSize + 16f)
            onClick {
                if (enabled) {
                    prefsGroup?.showListPrefChoices(this@ListPrefView)
                }
            }
        }

        pad(10f, 0f, 10f, 0f)
        add(titleLabel).growX().pad(20f, 20f, 20f, 30f)
        add(valueBtn).growY().pad(0f, 10f, 0f, 10f)
    }


    override fun onPreferenceValueChanged(pref: GamePref<String?>, value: String?) {
        valueLabel.setText(this.pref.displayValue)
    }


    class ListPrefViewStyle {
        lateinit var valueFontStyle: FontStyle
        lateinit var arrowIcon: Drawable
        lateinit var arrowIconColor: Color
    }

}