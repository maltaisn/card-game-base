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

import com.badlogic.gdx.scenes.scene2d.ui.Container
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.maltaisn.cardgame.CardGameListener
import com.maltaisn.cardgame.markdown.Markdown
import com.maltaisn.cardgame.tests.core.SubmenuContentTest
import com.maltaisn.cardgame.tests.core.TestRes
import com.maltaisn.cardgame.utils.padH
import com.maltaisn.cardgame.widget.CardGameLayout
import com.maltaisn.cardgame.widget.ScrollView
import com.maltaisn.cardgame.widget.markdown.MarkdownView
import ktx.assets.load


/**
 * Test for markdown views, parsing and inflating.
 */
class MarkdownViewTest(listener: CardGameListener) : SubmenuContentTest(listener) {

    override fun load() {
        super.load()
        assetManager.load<Markdown>(TestRes.LOREM_IPSUM_MARKDOWN)
    }

    override fun layoutContent(layout: CardGameLayout, content: Table) {
        val markdown: Markdown = assetManager[TestRes.LOREM_IPSUM_MARKDOWN]
        val mdView = MarkdownView(skin, markdown)

        // Do the layout
        val mdContainer = Container(mdView)
        mdContainer.fill().padH(40f)

        content.add(ScrollView(mdContainer)).grow()

        // Action buttons
        addToggleBtn("Debug") { _, debug ->
            mdView.setDebug(debug, true)
        }
    }

}
