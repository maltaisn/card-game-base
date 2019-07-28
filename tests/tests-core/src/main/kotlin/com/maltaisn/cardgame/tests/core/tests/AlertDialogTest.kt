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

import com.badlogic.gdx.utils.Align
import com.maltaisn.cardgame.markdown.Markdown
import com.maltaisn.cardgame.tests.core.ActionBarTest
import com.maltaisn.cardgame.tests.core.TestRes
import com.maltaisn.cardgame.widget.AlertDialog
import com.maltaisn.cardgame.widget.CardGameLayout
import com.maltaisn.cardgame.widget.ScrollView
import com.maltaisn.cardgame.widget.Separator
import com.maltaisn.cardgame.widget.markdown.MarkdownView
import ktx.actors.onClick
import ktx.assets.load
import ktx.log.info


class AlertDialogTest : ActionBarTest() {

    override fun load() {
        super.load()
        assetManager.load<Markdown>(TestRes.LOREM_IPSUM_MARKDOWN)
    }

    override fun layout(layout: CardGameLayout) {
        super.layout(layout)

        layout.centerTable.add().grow()  // Prevent action bar from being centered

        val dialog = AlertDialog(skin)
        dialog.dialogWidth = 1000f
        dialog.dismissOnClickOutside = true

        val markdown: Markdown = assetManager.get(TestRes.LOREM_IPSUM_MARKDOWN)
        val mdView = ScrollView(MarkdownView(skin, markdown))

        val btnSeparator = Separator(skin)
        btnSeparator.isVisible = false

        var contentShown = false
        var buttonsShown = false

        // Action buttons
        addActionBtn("Show dialog") {
            dialog.show(this)
        }
        addToggleBtn("Show title") { _, shown ->
            dialog.title = if (shown) "Privacy policy" else null
        }
        addToggleBtn("Center title") { _, shown ->
            dialog.titleLabel.setAlignment(if (shown) Align.center else Align.left)
        }
        addToggleBtn("Show message") { _, shown ->
            dialog.message = if (shown) "Please read the privary policy and click accept to continue." else null
        }
        addToggleBtn("Show content") { _, shown ->
            contentShown = shown
            if (shown) {
                dialog.alertContent.apply {
                    add(mdView).grow().pad(0f, 30f, 0f, 30f).row()
                    add(btnSeparator).growX().pad(0f, 60f, 0f, 60f)
                }
            } else {
                dialog.alertContent.clearChildren()
            }
            btnSeparator.isVisible = contentShown && buttonsShown
        }
        addToggleBtn("Show buttons") { _, shown ->
            buttonsShown = shown
            if (shown) {
                val denyBtn = dialog.addButton("Deny")
                denyBtn.onClick {
                    info { "Denied" }
                    dialog.hide()
                }

                val acceptBtn = dialog.addButton("Accept")
                acceptBtn.onClick {
                    info { "Accepted" }
                    dialog.hide()
                }
            } else {
                dialog.clearButtons()
            }
            btnSeparator.isVisible = contentShown && buttonsShown
        }
        addToggleBtn("Debug") { _, debug ->
            dialog.setDebug(debug, true)
        }
    }

}
