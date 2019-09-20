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
import com.maltaisn.cardgame.CardGameListener
import com.maltaisn.cardgame.tests.core.SubmenuContentTest
import com.maltaisn.cardgame.tests.core.fontStyle
import com.maltaisn.cardgame.widget.CardGameLayout
import com.maltaisn.cardgame.widget.MsdfTextField


class MsdfTextFieldTest(listener: CardGameListener) : SubmenuContentTest(listener) {

    override fun layoutContent(layout: CardGameLayout, content: Table) {
        val topField = MsdfTextField(skin, fontStyle(size = 48f, color = Color.BLACK),
                text = "Text input")
        topField.maxLength = 20
        topField.inputTitle = "Enter text"

        val bottomField = MsdfTextField(skin,
                fontStyle(size = 48f, color = Color.BLACK),
                fontStyle(size = 48f, color = Color.GRAY), "Text input 2")
        bottomField.maxLength = 128
        bottomField.messageText = "Enter text here"
        bottomField.inputTitle = "Enter player name"

        // Do the layout
        content.add(topField).width(600f).expand().row()
        content.add(bottomField).width(1000f).expand()

        addActionBtn("Clear") {
            topField.text = null
            bottomField.text = null
        }
        addTwoStateActionBtn("Disable", "Enable") { _, enabled ->
            topField.isDisabled = !enabled
            bottomField.isDisabled = !enabled
        }
        addToggleBtn("Debug") { _, debug ->
            content.setDebug(debug, true)
        }
    }

}
