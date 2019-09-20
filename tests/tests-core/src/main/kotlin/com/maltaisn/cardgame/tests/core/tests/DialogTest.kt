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

import com.maltaisn.cardgame.CardGameListener
import com.maltaisn.cardgame.markdown.Markdown
import com.maltaisn.cardgame.tests.core.ActionBarTest
import com.maltaisn.cardgame.tests.core.TestRes
import com.maltaisn.cardgame.utils.padH
import com.maltaisn.cardgame.widget.Button
import com.maltaisn.cardgame.widget.CardGameLayout
import com.maltaisn.cardgame.widget.Dialog
import com.maltaisn.cardgame.widget.ScrollView
import com.maltaisn.cardgame.widget.markdown.MarkdownView
import ktx.actors.onClick
import ktx.assets.load
import ktx.log.info


class DialogTest(listener: CardGameListener) : ActionBarTest(listener) {

    override fun load() {
        super.load()
        assetManager.load<Markdown>(TestRes.LOREM_IPSUM_MARKDOWN)
    }

    override fun layout(layout: CardGameLayout) {
        super.layout(layout)

        layout.centerTable.add().grow()  // Prevent action bar from being centered

        val dialog = object : Dialog(skin) {
            override fun onDismiss() {
                info { "Dialog dismissed" }
            }
        }

        val markdown: Markdown = assetManager[TestRes.LOREM_IPSUM_MARKDOWN]
        val mdView = MarkdownView(skin, markdown)

        val closeBtn = Button(skin, "Close dialog")
        closeBtn.onClick {
            dialog.hide()
        }

        dialog.content.apply {
            add(ScrollView(mdView)).grow().padH(50f).row()
            add(closeBtn).growX().pad(50f)
        }

        // Action buttons
        addActionBtn("Show dialog") {
            dialog.show(this)
        }
        addEnumBtn("Shadow type", Dialog.ShadowType.values().toList(), initialIndex = 2) { _, value ->
            dialog.shadowType = value
        }
        addValueBtn("Width", 400f, 1600f, dialog.dialogWidth, 200f) { _, value, _ ->
            dialog.dialogWidth = value
        }
        addToggleBtn("Debug") { _, debug ->
            dialog.setDebug(debug, true)
        }
    }

}
