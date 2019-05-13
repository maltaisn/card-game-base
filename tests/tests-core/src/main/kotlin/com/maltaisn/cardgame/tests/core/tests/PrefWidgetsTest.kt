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
import com.maltaisn.cardgame.CardGameLayout
import com.maltaisn.cardgame.tests.core.ActionBarTest
import com.maltaisn.cardgame.widget.FontStyle
import com.maltaisn.cardgame.widget.SdfTextField
import com.maltaisn.cardgame.widget.Slider
import com.maltaisn.cardgame.widget.Switch
import ktx.log.debug


/**
 * Test widgets used by preferences: [Switch], [Slider] and [SdfTextField].
 * Test checked state, enabled state, animations.
 */
class PrefWidgetsTest : ActionBarTest() {

    override fun layout(layout: CardGameLayout) {
        super.layout(layout)

        // Text field
        val textField = SdfTextField(coreSkin, FontStyle().apply {
            fontSize = 24f
            fontColor = Color.BLACK
        }, "Text input")
        textField.maxLength = 20

        // Switch
        val switch = Switch(coreSkin)
        switch.checkListener = { checked ->
            debug { "Switch checked change to $checked" }
        }

        // Slider
        val slider = Slider(coreSkin)
        slider.progress = 50f
        slider.changeListener = { value ->
            debug { "Slider value changed to $value" }
        }

        // Do the layout
        val content = Table().apply {
            background = coreSkin.getDrawable("submenu-content-background")
            add(textField).width(300f).expand().row()
            add(switch).size(300f, 100f).expand().row()
            add(slider).width(500f).expand().row()
        }
        layout.gameLayer.centerTable.add(content).grow()
                .pad(0f, 20f, 0f, 20f)

        // Action buttons
        addActionBtn("Check") {
            switch.check(!switch.checked, true)
        }
        addActionBtn("Instant check") {
            switch.check(!switch.checked, false)
        }
        addActionBtn("Disable") {
            it.title = if (switch.enabled) "Disable" else "Enable"
            switch.enabled = !switch.enabled
            slider.enabled = !slider.enabled
            textField.isDisabled = !textField.isDisabled
        }
        addActionBtn("Debug") {
            content.setDebug(!content.debug, true)
        }
    }

}
