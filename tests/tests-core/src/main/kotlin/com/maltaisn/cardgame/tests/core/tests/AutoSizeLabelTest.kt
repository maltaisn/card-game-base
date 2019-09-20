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
import com.badlogic.gdx.scenes.scene2d.Action
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.maltaisn.cardgame.CardGameListener
import com.maltaisn.cardgame.tests.core.SubmenuContentTest
import com.maltaisn.cardgame.tests.core.fontStyle
import com.maltaisn.cardgame.widget.AutoSizeLabel
import com.maltaisn.cardgame.widget.CardGameLayout
import kotlin.math.max
import kotlin.math.min


class AutoSizeLabelTest(listener: CardGameListener) : SubmenuContentTest(listener) {

    override fun layoutContent(layout: CardGameLayout, content: Table) {
        val label = AutoSizeLabel("The quick brown fox\njumped over the lazy dog",
                skin, fontStyle(color = Color.BLACK)).apply {
            setAlignment(Align.center)
            minTextSize = 20f
            maxTextSize = 120f
            granularity = 5f
        }
        val labelCell = content.add(label).size(800f, 300f).expand()
        label.debug = true
        debugColor.set(Color.DARK_GRAY)

        // Action buttons
        val sizeBtn = addActionBtn("") {}
        sizeBtn.enabled = false
        addAction(object : Action() {
            override fun act(delta: Float): Boolean {
                sizeBtn.title = "Size: ${label.fontStyle.size.toInt()}"
                return false
            }
        })
        addValueBtn("Width", 200f, 1600f, labelCell.prefWidth, 100f) { _, width, _ ->
            labelCell.width(width)
            content.invalidate()
        }
        addValueBtn("Height", 50f, 500f, labelCell.prefHeight, 25f) { _, height, _ ->
            labelCell.height(height)
            content.invalidate()
        }
        addValueBtn("Min size", 20f, 160f, label.minTextSize, 20f) { _, size, _ ->
            label.minTextSize = min(size, label.maxTextSize)
        }
        addValueBtn("Max size", 20f, 160f, label.maxTextSize, 20f) { _, size, _ ->
            label.maxTextSize = max(size, label.minTextSize)
        }
        addValueBtn("Granularity", 1f, 10f, label.granularity, 1f) { _, size, _ ->
            label.granularity = size
        }
        addToggleBtn("Wrap") { _, wrap ->
            label.setWrap(wrap)
        }
        addToggleBtn("Ellipsis") { _, ellipsis ->
            label.setEllipsis(ellipsis)
        }
    }

}
