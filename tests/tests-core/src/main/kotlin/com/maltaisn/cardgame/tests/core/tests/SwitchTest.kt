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

import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.maltaisn.cardgame.tests.core.SubmenuContentTest
import com.maltaisn.cardgame.widget.CardGameLayout
import com.maltaisn.cardgame.widget.Switch
import ktx.log.info


class SwitchTest : SubmenuContentTest() {

    override fun layoutContent(layout: CardGameLayout, content: Table) {
        val topSwitch = Switch(coreSkin)
        topSwitch.checkListener = { info { "Top switch check changed to $it" } }

        val bottomSwitch = Switch(coreSkin)
        bottomSwitch.checkListener = { info { "Bottom switch check changed to $it" } }

        // Do the layout
        content.add(topSwitch).size(600f, 200f).expand().row()
        content.add(bottomSwitch).expand()

        // Action buttons
        var checkAnimationEnabled = true
        addToggleBtn("Check animation enabled", startState = true) { _, enabled ->
            checkAnimationEnabled = enabled
        }
        addActionBtn("Check") {
            topSwitch.check(true, checkAnimationEnabled)
            bottomSwitch.check(true, checkAnimationEnabled)
        }
        addActionBtn("Uncheck") {
            topSwitch.check(false, checkAnimationEnabled)
            bottomSwitch.check(false, checkAnimationEnabled)
        }
        addActionBtn("Toggle") {
            topSwitch.check(!topSwitch.checked, checkAnimationEnabled)
            bottomSwitch.check(!bottomSwitch.checked, checkAnimationEnabled)
        }
        addTwoStateActionBtn("Disable", "Enable") { _, enabled ->
            topSwitch.enabled = enabled
            bottomSwitch.enabled = enabled
        }
        addToggleBtn("Debug") { _, debug ->
            content.setDebug(debug, true)
        }
    }

}
