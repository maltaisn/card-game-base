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
import com.maltaisn.cardgame.prefs.PlayerNamesPref
import com.maltaisn.cardgame.widget.MsdfTextField
import com.maltaisn.msdfgdx.FontStyle
import ktx.actors.onKeyboardFocus
import ktx.style.get


class PlayerNamesPrefView(skin: Skin, pref: PlayerNamesPref) :
        GamePrefView<PlayerNamesPref, Array<String>>(skin, pref) {

    override var enabled
        get() = super.enabled
        set(value) {
            super.enabled = value
            for (textField in textFields) {
                textField.isDisabled = !value
            }
        }

    private val textFields: Array<MsdfTextField>

    init {
        val style: PlayerNamesPrefViewStyle = skin.get()

        pad(10f, 0f, 10f, 20f)
        add(titleLabel).colspan(2).growX().pad(10f, 20f, 20f, 20f).row()

        textFields = Array(pref.size) { playerPos ->
            val textField = MsdfTextField(skin, skin.get<TextField.TextFieldStyle>(),
                    style.fieldFontStyle, text = pref.getPlayerName(playerPos))
            textField.inputTitle = pref.inputTitle
            textField.maxLength = pref.maxLength
            textField.onKeyboardFocus { focused ->
                if (!focused) {
                    val oldValue = pref.getPlayerName(playerPos)
                    val newValue = (if (textField.text.isBlank()) {
                        pref.defaultValue[playerPos]
                    } else {
                        textField.text
                    }).trim()

                    if (newValue != oldValue) {
                        // Change the preference value when the text field is unfocused.
                        // If user enters nothing, set default name
                        pref.value[playerPos] = newValue
                        changePreferenceValue(pref.value) {
                            pref.value[playerPos] = oldValue
                            textField.text = oldValue
                        }
                    } else {
                        // Both values may be equal, but maybe user added some spaces that were trimmed...
                        textField.text = oldValue
                    }
                }
            }
            textField.setTextFieldFilter { _, c -> c.isLetterOrDigit() || c == ' ' }

            add(textField).growX().pad(20f)
            if (playerPos % 2 == 1) {
                row()
            }

            textField
        }

        this.enabled = enabled
    }

    override fun onPreferenceValueChanged(pref: GamePref<Array<String>>, value: Array<String>) {
        for (i in 0 until this.pref.size) {
            textFields[i].text = this.pref.getPlayerName(i)
        }
    }


    class PlayerNamesPrefViewStyle {
        lateinit var fieldFontStyle: FontStyle
    }

}
