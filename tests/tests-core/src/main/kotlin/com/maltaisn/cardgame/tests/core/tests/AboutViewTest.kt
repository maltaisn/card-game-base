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
import com.maltaisn.cardgame.CardGameListener
import com.maltaisn.cardgame.tests.core.SubmenuContentTest
import com.maltaisn.cardgame.widget.AboutView
import com.maltaisn.cardgame.widget.CardGameLayout
import com.maltaisn.cardgame.widget.CoreIcons
import ktx.actors.onClick
import ktx.log.info


class AboutViewTest(listener: CardGameListener) : SubmenuContentTest(listener) {

    override fun layoutContent(layout: CardGameLayout, content: Table) {
        val aboutView = AboutView(skin, "Hearts", "1.0.0", "Chuck Norris")

        aboutView.addButton("Rate app", skin.getDrawable(CoreIcons.STAR))
                .onClick { info { "Rate app" } }
        aboutView.addButton("Send feedback", skin.getDrawable(CoreIcons.ALERT))
                .onClick { info { "Send feedback" } }
        aboutView.addButton("View changelog", skin.getDrawable(CoreIcons.LIST))
                .onClick { info { "View changelog" } }
        aboutView.addButton("Open source libraries", skin.getDrawable(CoreIcons.INFO))
                .onClick { info { "Open source libraries" } }
        aboutView.addButton("Donate", skin.getDrawable(CoreIcons.COIN))
                .onClick { info { "Please" } }

        content.add(aboutView).growX().pad(0f, 300f, 0f, 300f)

        // Action buttons
        addToggleBtn("Debug") { _, debug ->
            content.setDebug(debug, true)
        }
    }

}
