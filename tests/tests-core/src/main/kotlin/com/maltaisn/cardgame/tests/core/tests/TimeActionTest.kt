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

import com.badlogic.gdx.math.Interpolation
import com.maltaisn.cardgame.CardGameListener
import com.maltaisn.cardgame.pcard.PCard
import com.maltaisn.cardgame.tests.core.ActionBarTest
import com.maltaisn.cardgame.widget.CardGameLayout
import com.maltaisn.cardgame.widget.action.TimeAction
import com.maltaisn.cardgame.widget.card.CardActor
import ktx.actors.alpha
import ktx.log.info
import kotlin.math.max
import kotlin.math.min


/**
 * Test time action and its global speed modifier.
 */
class TimeActionTest(listener: CardGameListener) : ActionBarTest(listener) {

    private var fadeAction: TimeAction? = null

    override fun layout(layout: CardGameLayout) {
        super.layout(layout)

        val card = CardActor(pcardStyle, PCard(8, PCard.CLUB))
        card.setOrigin(card.prefWidth / 2, card.prefHeight / 2)
        layout.centerTable.add(card).expand()

        var cardShown = true
        var intpIn = INTERPOLATIONS.first()
        var intpOut = INTERPOLATIONS.first()

        addActionBtn("Fade") {
            cardShown = !cardShown
            if (fadeAction == null) {
                fadeAction = object : TimeAction(1f, intpIn, intpOut,
                        reversed = !cardShown) {

                    override fun begin() {
                        card.isVisible = true
                        card.alpha = if (cardShown) 0f else 1f
                        info { "Animation began" }
                    }

                    override fun update(progress: Float) {
                        reversed = !cardShown
                        card.alpha = progress
                    }

                    override fun end() {
                        card.isVisible = cardShown
                        fadeAction = null
                        info { "Animation ended" }
                    }
                }
                card.addAction(fadeAction)
            }
        }

        // Interpolation
        addEnumBtn("In", INTERPOLATIONS, INTERPOLATION_NAMES) { _, intp ->
            intpIn = intp
            fadeAction?.end()
        }
        addEnumBtn("Out", INTERPOLATIONS, INTERPOLATION_NAMES) { _, intp ->
            intpOut = intp
            fadeAction?.end()
        }

        // Speed
        addActionBtn("Speed -") {
            fadeAction?.end()
            TimeAction.SPEED_MULTIPLIER = max(0.125f, TimeAction.SPEED_MULTIPLIER / 2)
            info { "Speed: ${TimeAction.SPEED_MULTIPLIER}" }
        }
        addActionBtn("Speed +") {
            fadeAction?.end()
            TimeAction.SPEED_MULTIPLIER = min(8f, TimeAction.SPEED_MULTIPLIER * 2)
            info { "Speed: ${TimeAction.SPEED_MULTIPLIER}" }
        }
    }

    companion object {
        private val INTERPOLATION_NAMES = listOf("Linear", "Smooth", "Pow", "Bounce")
        private val INTERPOLATIONS = listOf(Interpolation.linear,
                Interpolation.smooth, Interpolation.pow4, Interpolation.bounce)
    }

}
