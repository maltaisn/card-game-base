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
import com.maltaisn.cardgame.tests.core.ActionBarTest
import com.maltaisn.cardgame.tests.core.CenterLayout
import com.maltaisn.cardgame.widget.CardGameLayout
import com.maltaisn.cardgame.widget.FontStyle
import com.maltaisn.cardgame.widget.SdfLabel
import ktx.actors.alpha


/**
 * Test distance field font rendering at different sizes.
 */
class SdfLabelTest : ActionBarTest() {

    override fun layout(layout: CardGameLayout) {
        super.layout(layout)

        var selectedText = 0
        var selectedColors = 0
        val fontStyle = FontStyle().apply {
            bold = false
            drawShadow = false
            fontColor = COLORS[selectedColors].first
            shadowColor = COLORS[selectedColors].second
        }
        var fontAlpha = 100

        val labels = List(10) {
            SdfLabel(coreSkin, fontStyle.copy(fontSize = 24f + it * 8f), TEXTS[selectedText]).apply {
                alpha = fontAlpha / 100f
            }
        }

        fun updateFontStyle() {
            for (label in labels) {
                label.fontStyle = fontStyle.copy(fontSize = label.fontStyle.fontSize)
            }
        }

        // Add all labels to the layout
        layout.gameLayer.centerTable.apply {
            clearChildren()
            add(btnTable).growX().colspan(100).pad(50f, 40f, 50f, 40f).row()

            val labelTable = Table()
            for (label in labels) {
                labelTable.add(label).expand().center().row()
            }
            add(CenterLayout(labelTable)).grow()
        }

        // Action buttons
        addActionBtn("Change text") {
            selectedText = (selectedText + 1) % TEXTS.size
            val text = TEXTS[selectedText]
            for (label in labels) {
                label.setText(text)
            }
        }
        addActionBtn("Change colors") {
            selectedColors = (selectedColors + 1) % COLORS.size
            val colors = COLORS[selectedColors]
            fontStyle.fontColor = colors.first
            fontStyle.shadowColor = colors.second
            updateFontStyle()
        }

        addToggleBtn("Bold") { _, bold ->
            fontStyle.bold = bold
            updateFontStyle()
        }
        addToggleBtn("Shadow") { _, drawShadow ->
            fontStyle.drawShadow = drawShadow
            updateFontStyle()
        }
        addToggleBtn("All caps") { _, allCaps ->
            fontStyle.allCaps = allCaps
            updateFontStyle()
        }
        addToggleBtn("Enabled", true) { _, enabled ->
            for (label in labels) {
                label.enabled = enabled
            }
        }

        addActionBtn("Alpha: 100%") {
            fontAlpha = (fontAlpha + 100) % 110
            it.title = "Alpha: $fontAlpha%"
            for (label in labels) {
                label.alpha = fontAlpha / 100f
            }
        }
    }

    companion object {
        private val TEXTS = listOf(
                "The quick brown fox jumps over a lazy dog.",
                "!\"#\$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz",
                "{|}~\u007F¡¢£¤¥¦§¨©ª«¬\u00AD®¯°±²³´µ¶·¸¹º»¼½¾¿ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿ")

        private val COLORS = listOf(
                Color.WHITE to Color.BLACK,
                Color.BLACK to Color.WHITE,
                Color.RED to Color.WHITE,
                Color.BLUE to Color.WHITE)
    }

}
