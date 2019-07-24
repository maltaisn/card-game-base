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
import com.maltaisn.cardgame.markdown.Markdown
import com.maltaisn.cardgame.tests.core.SubmenuContentTest
import com.maltaisn.cardgame.widget.CardGameLayout
import com.maltaisn.cardgame.widget.ScrollView
import com.maltaisn.cardgame.widget.markdown.MarkdownView
import ktx.assets.load


/**
 * Test for markdown views, parsing and inflating.
 */
class MarkdownViewTest : SubmenuContentTest() {

    override fun load() {
        super.load()
        assetManager.load<Markdown>(MARKDOWN_FILE)
    }

    override fun layoutContent(layout: CardGameLayout, content: Table) {
        val markdown: Markdown = assetManager.get(MARKDOWN_FILE)
        val mdView = MarkdownView(coreSkin, markdown)

        // Do the layout
        val mdContainer = Container(mdView)
        mdContainer.fill().pad(0f, 40f, 0f, 40f)

        content.add(ScrollView(mdContainer)).grow()

        // Action buttons
        addToggleBtn("Debug") { _, debug ->
            mdView.setDebug(debug, true)
        }
    }

    companion object {
        private const val MARKDOWN_FILE = "lorem-ipsum"
    }

}
