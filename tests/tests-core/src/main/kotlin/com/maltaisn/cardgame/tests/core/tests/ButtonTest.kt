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

import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.maltaisn.cardgame.tests.core.SubmenuContentTest
import com.maltaisn.cardgame.widget.Button
import com.maltaisn.cardgame.widget.CardGameLayout


class ButtonTest : SubmenuContentTest() {

    override fun layoutContent(layout: CardGameLayout, content: Table) {
        val btn1 = Button(skin, "Default style")
        content.add(btn1).expand().row()

        val btn2 = Button(skin, "Borderless style", "borderless")
        content.add(btn2).expand().row()

        val image = Image(skin.getDrawable("icon-book"))
        image.setColor(0f, 0f, 0f, 0.7f)
        val btn3 = Button(skin, "Image button", "borderless").apply {
            clearChildren()
            add(image).pad(0f, 10f, 0f, 0f)
            add(label).pad(0f, 50f, 0f, 30f)
        }
        content.add(btn3).expand().row()

        // Action buttons
        addTwoStateActionBtn("Disable", "Enable") { _, enabled ->
            btn1.enabled = enabled
            btn2.enabled = enabled
            btn3.enabled = enabled
        }
        addToggleBtn("Debug") { _, debug ->
            content.setDebug(debug, true)
        }
    }

}
