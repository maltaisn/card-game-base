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
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.maltaisn.cardgame.prefs.GamePref
import com.maltaisn.cardgame.prefs.TextPref
import com.maltaisn.cardgame.widget.text.FontStyle
import com.maltaisn.cardgame.widget.text.SdfTextField
import ktx.actors.onKeyboardFocus
import ktx.style.get


class TextPrefView(skin: Skin, pref: TextPref) :
        GamePrefView<TextPref, String>(skin, pref) {

    override var enabled
        get() = super.enabled
        set(value) {
            super.enabled = value
            textField.isDisabled = !value
        }

    private val textField: SdfTextField

    init {
        val style: TextPrefViewStyle = skin.get()

        textField = SdfTextField(skin, skin.get<TextField.TextFieldStyle>(),
                style.fieldFontStyle, text = pref.value)
        textField.maxLength = pref.maxLength
        textField.setTextFieldFilter { _, c -> pref.filter == null || c in pref.filter!! }
        textField.onKeyboardFocus { focused ->
            if (!focused && textField.text != pref.value) {
                changePreferenceValue(textField.text) {
                    textField.text = pref.value
                }
            }
        }

        pad(10f, 20f, 10f, 40f)
        add(titleLabel).growX().pad(10f, 0f, 20f, 0f).row()
        add(textField).growX().pad(10f, 0f, 10f, 0f)

        this.enabled = enabled
    }

    override fun onPreferenceValueChanged(pref: GamePref<String>, value: String) {
        textField.text = this.pref.value
    }


    class TextPrefViewStyle {
        lateinit var fieldFontStyle: FontStyle
    }

}