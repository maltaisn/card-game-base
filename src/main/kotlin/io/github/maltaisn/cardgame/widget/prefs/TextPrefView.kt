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
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import io.github.maltaisn.cardgame.prefs.TextPref
import io.github.maltaisn.cardgame.widget.FontStyle
import io.github.maltaisn.cardgame.widget.SdfTextField
import ktx.actors.onKeyboardFocus


class TextPrefView(skin: Skin, pref: TextPref) : GamePrefView<TextPref>(skin, pref) {

    override var enabled
        get() = super.enabled
        set(value) {
            super.enabled = value
            textField.isDisabled = !value
        }

    private val textField: SdfTextField

    init {
        val style = skin[TextPrefViewStyle::class.java]

        textField = SdfTextField(skin, style.fieldStyle, style.fieldFontStyle, pref.value).apply {
            maxLength = pref.maxLength
            onKeyboardFocus {
                if (!it) {
                    // Change the preference value when the text field is unfocused.
                    pref.value = text
                }
            }
            setTextFieldFilter { _, c -> pref.filter == null || c in pref.filter!! }
        }

        pad(5f, 10f, 5f, 20f)
        add(titleLabel).growX().pad(5f, 0f, 10f, 0f).row()
        add(textField).growX().pad(5f, 0f, 5f, 0f)

        this.enabled = enabled
    }

    override fun onPreferenceValueChanged() {
        textField.text = pref.value
    }


    class TextPrefViewStyle {
        lateinit var fieldStyle: TextField.TextFieldStyle
        lateinit var fieldFontStyle: FontStyle
    }

}