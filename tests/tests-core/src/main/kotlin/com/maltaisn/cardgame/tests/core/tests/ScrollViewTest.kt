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
import com.badlogic.gdx.utils.Align
import com.maltaisn.cardgame.tests.core.SubmenuContentTest
import com.maltaisn.cardgame.widget.CardGameLayout
import com.maltaisn.cardgame.widget.FontStyle
import com.maltaisn.cardgame.widget.ScrollView
import com.maltaisn.cardgame.widget.SdfLabel
import ktx.log.info
import java.util.*


class ScrollViewTest : SubmenuContentTest() {

    override fun layoutContent(layout: CardGameLayout, content: Table) {
        // Do the layout
        val scrollView = ScrollView(Table().apply {
            val fontStyle = FontStyle(fontSize = 44f, fontColor = Color.BLACK)
            repeat(30) {
                val label = SdfLabel(coreSkin, fontStyle, UUID.randomUUID().toString())
                label.setAlignment(Align.center)
                add(label).grow().pad(20f).row()
            }
        })

        scrollView.scrollListener = { _, x: Float, y: Float, dx: Float, dy: Float ->
            info { "Scrolled x: $x, y: $y, dx: $dx, dy: $dy" }
        }
        scrollView.clearActions()  // Should do nothing

        content.add(scrollView).grow().pad(40f, 200f, 40f, 200f)

        // Action buttons
        addActionBtn("Scroll to top") {
            scrollView.scrollToTop()
        }
        addActionBtn("Scroll to bottom") {
            scrollView.scrollToBottom()
        }
    }
}
