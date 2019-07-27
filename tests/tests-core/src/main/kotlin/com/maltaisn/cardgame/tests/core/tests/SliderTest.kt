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
import com.maltaisn.cardgame.widget.Slider
import com.maltaisn.cardgame.widget.text.FontStyle
import com.maltaisn.cardgame.widget.text.SdfLabel
import kotlin.random.Random


class SliderTest : SubmenuContentTest() {

    override fun layoutContent(layout: CardGameLayout, content: Table) {
        val sliders = mutableListOf<Slider>()

        repeat(5) { n ->
            val label = SdfLabel(skin, FontStyle(fontSize = 44f, fontColor = Color.BLACK))
            val slider = Slider(skin).apply {
                minProgress = n * 10f
                maxProgress = 100 - minProgress
                progress = randomValue()
                step = (n + 1).toFloat() / 2f
                changeListener = {
                    label.setText(it.toString())
                }
            }
            label.setText(slider.progress.toString())
            content.add(slider).width(1000f).expand().align(Align.right).pad(20f)
            content.add(label).width(200f).expand().align(Align.left).row()
            sliders += slider
        }

        // Action buttons
        addTwoStateActionBtn("Disable", "Enable") { _, enabled ->
            for (slider in sliders) {
                slider.enabled = enabled
            }
        }
        addActionBtn("Instant change") {
            for (slider in sliders) {
                slider.progress = slider.randomValue()
            }
        }
        addActionBtn("Slide change") {
            for (slider in sliders) {
                slider.slideTo(slider.randomValue())
            }
        }
        addToggleBtn("Debug") { _, debug ->
            content.setDebug(debug, true)
        }
    }

    private fun Slider.randomValue() = Random.nextDouble(
            minProgress.toDouble(), maxProgress.toDouble()).toFloat()

}
