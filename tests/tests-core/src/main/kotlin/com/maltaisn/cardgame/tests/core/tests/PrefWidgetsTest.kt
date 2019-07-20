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

package com.maltaisn.cardgame.tests.core.tests

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.maltaisn.cardgame.tests.core.SubmenuContentTest
import com.maltaisn.cardgame.widget.*
import ktx.log.info


/**
 * Test widgets used by preferences: [Switch], [Slider] and [SdfTextField].
 * Test checked state, enabled state, animations.
 */
class PrefWidgetsTest : SubmenuContentTest() {

    override fun layoutContent(layout: CardGameLayout, content: Table) {
        // Text field
        val textField = SdfTextField(coreSkin, FontStyle().apply {
            fontSize = 48f
            fontColor = Color.BLACK
        }, "Text input")
        textField.maxLength = 20

        // Switch
        val switch = Switch(coreSkin)
        switch.checkListener = { checked ->
            info { "Switch checked change to $checked" }
        }

        // Slider
        val slider = Slider(coreSkin)
        slider.progress = 50f
        slider.changeListener = { value ->
            info { "Slider value changed to $value" }
        }

        // Do the layout
        content.add(textField).width(600f).expand().row()
        content.add(switch).size(600f, 200f).expand().row()
        content.add(slider).width(1000f).expand().row()

        // Action buttons
        addActionBtn("Check") {
            switch.check(!switch.checked, true)
        }
        addActionBtn("Instant check") {
            switch.check(!switch.checked, false)
        }
        addTwoStateActionBtn("Disable", "Enable") { _, enabled ->
            switch.enabled = enabled
            slider.enabled = enabled
            textField.isDisabled = !enabled
        }
        addToggleBtn("Debug") { _, debug ->
            content.setDebug(debug, true)
        }
    }

}
