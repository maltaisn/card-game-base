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
import com.maltaisn.cardgame.utils.padH
import com.maltaisn.cardgame.utils.padV
import com.maltaisn.msdfgdx.FontStyle
import com.maltaisn.msdfgdx.widget.MsdfLabel
import ktx.actors.alpha
import ktx.actors.onClick
import ktx.style.get


class ListPrefView(skin: Skin, pref: ListPref) :
        GamePrefView<ListPref, String>(skin, pref) {

    override var enabled
        get() = super.enabled
        set(value) {
            super.enabled = value
            valueLabel.isDisabled = !value
            arrowIcon.alpha = if (value) 1f else 0.5f
        }


    private val valueLabel: MsdfLabel
    private val arrowIcon: Image


    init {
        val style: ListPrefViewStyle = skin.get()

        valueLabel = MsdfLabel(pref.displayValue, skin, style.valueFontStyle)
        valueLabel.isDisabled = !enabled

        arrowIcon = Image(style.arrowIcon, Scaling.fit)
        arrowIcon.color = style.arrowIconColor

        val valueBtn = Table().apply {
            touchable = Touchable.enabled
            add(valueLabel).padRight(20f)
            add(arrowIcon).size(style.valueFontStyle.size + 16f)
            onClick {
                if (enabled) {
                    prefsGroup?.showListPrefChoices(this@ListPrefView)
                }
            }
        }

        padV(10f)
        add(titleLabel).growX().pad(20f, 20f, 20f, 30f)
        add(valueBtn).growY().padH(10f)
    }


    override fun onPreferenceValueChanged(pref: GamePref<String>, value: String) {
        valueLabel.txt = this.pref.displayValue
    }


    class ListPrefViewStyle {
        lateinit var valueFontStyle: FontStyle
        lateinit var arrowIcon: Drawable
        lateinit var arrowIconColor: Color
    }

}
