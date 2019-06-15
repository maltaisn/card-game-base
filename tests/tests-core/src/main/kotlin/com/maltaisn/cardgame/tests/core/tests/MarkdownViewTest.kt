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

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.ui.Container
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.maltaisn.cardgame.markdown.Markdown
import com.maltaisn.cardgame.tests.core.CardGameTest
import com.maltaisn.cardgame.widget.CardGameLayout
import com.maltaisn.cardgame.widget.ScrollView
import com.maltaisn.cardgame.widget.markdown.MarkdownView
import ktx.actors.setScrollFocus
import ktx.assets.load


/**
 * Test for markdown views, parsing and inflating.
 */
class MarkdownViewTest : CardGameTest() {

    override fun load() {
        super.load()
        assetManager.load<Markdown>(MARKDOWN_FILE)
    }

    override fun layout(layout: CardGameLayout) {
        super.layout(layout)

        val markdown: Markdown = assetManager.get(MARKDOWN_FILE)
        val mdView = MarkdownView(coreSkin, markdown)

        // Do the layout
        val content = Container(mdView)
        content.fill().pad(0f, 20f, 0f, 20f)

        ScrollView(content, ScrollPane.ScrollPaneStyle(
                coreSkin.getDrawable("submenu-content-background"),
                null, null, null, null)).apply contentPane@{
            setScrollingDisabled(true, false)
            setOverscroll(false, false)
            setCancelTouchFocus(false)
            layout.gameLayer.centerTable.add(this).grow()
                    .pad(20f, 20f, 0f, 20f)
            addListener(object : InputListener() {
                override fun enter(event: InputEvent, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
                    this@contentPane.setScrollFocus(true)
                }

                override fun exit(event: InputEvent?, x: Float, y: Float, pointer: Int, toActor: Actor?) {
                    this@contentPane.setScrollFocus(false)
                }
            })
        }
    }

    companion object {
        private const val MARKDOWN_FILE = "lorem-ipsum"
    }

}