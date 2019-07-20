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
import com.maltaisn.cardgame.prefs.PlayerNamesPref
import com.maltaisn.cardgame.prefs.PrefEntry
import com.maltaisn.cardgame.widget.FontStyle
import com.maltaisn.cardgame.widget.SdfTextField
import ktx.actors.onKeyboardFocus
import ktx.style.get


class PlayerNamesPrefView(skin: Skin, pref: PlayerNamesPref) : GamePrefView<PlayerNamesPref>(skin, pref) {

    override var enabled
        get() = super.enabled
        set(value) {
            super.enabled = value
            for (textField in textFields) {
                textField.isDisabled = !value
            }
        }

    private val textFields: Array<SdfTextField>

    init {
        val style: PlayerNamesPrefViewStyle = skin.get()

        pad(10f, 0f, 10f, 20f)
        add(titleLabel).colspan(2).growX().pad(10f, 20f, 20f, 20f).row()

        textFields = Array(pref.size) { player ->
            val textField = SdfTextField(skin, style.fieldStyle,
                    style.fieldFontStyle, pref.names[player]).apply {
                maxLength = pref.maxLength
                onKeyboardFocus {
                    if (!it) {
                        // Change the preference value when the text field is unfocused.
                        if (text.isBlank()) {
                            // If user enters nothing, set default name
                            text = pref.defaultNames[player]
                        }
                        text = text.trim()
                        pref.setName(player, text)
                    }
                }
                setTextFieldFilter { _, c -> pref.filter == null || c in pref.filter!! }
            }

            add(textField).growX().pad(10f, 20f, 10f, 20f)
            if (player % 2 == 1) {
                row()
            }

            textField
        }

        this.enabled = enabled
    }

    override fun onPreferenceValueChanged(pref: PrefEntry) {
        for ((i, name) in this.pref.names.withIndex()) {
            textFields[i].text = name
        }
    }


    class PlayerNamesPrefViewStyle {
        lateinit var fieldStyle: TextField.TextFieldStyle
        lateinit var fieldFontStyle: FontStyle
    }

}
