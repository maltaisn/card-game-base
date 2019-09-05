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
import com.maltaisn.cardgame.pcard.PCard
import com.maltaisn.cardgame.pcard.PCardLabel
import com.maltaisn.cardgame.tests.core.ActionBarTest
import com.maltaisn.cardgame.tests.core.fontStyle
import com.maltaisn.cardgame.widget.CardGameLayout


class PCardLabelTest : ActionBarTest() {

    override fun layout(layout: CardGameLayout) {
        super.layout(layout)

        val deck = PCard.fullDecks(withJokers = false)

        val fontStyle = fontStyle(size = 64f, color = Color.WHITE, shadowColor = Color.BLACK)
        val pcardLabel = PCardLabel(skin, deck.random(), fontStyle)

        layout.centerTable.add(pcardLabel).expand()

        var useSuitColor = false
        val pcardRed = skin.getColor("pcardRed")
        val pcardBlack = skin.getColor("pcardBlack")

        fun setLabelColor() {
            fontStyle.color = if (useSuitColor) {
                if (pcardLabel.card.color == PCard.RED) pcardRed else pcardBlack
            } else {
                Color.WHITE
            }
            pcardLabel.fontStyle = fontStyle
        }

        // Add action buttons
        addActionBtn("Change card") {
            pcardLabel.card = deck.random()
            setLabelColor()
        }
        addToggleBtn("Shadow") { _, shadow ->
            fontStyle.shadowColor.a = if (shadow) 1f else 0f
            pcardLabel.fontStyle = fontStyle
        }
        addToggleBtn("Use suit color") { _, state ->
            useSuitColor = state
            setLabelColor()
        }
        addValueBtn("Size", 32f, 128f, fontStyle.size, 8f) { _, size, _ ->
            fontStyle.size = size
            pcardLabel.fontStyle = fontStyle
        }
        addToggleBtn("Debug") { _, debug ->
            pcardLabel.setDebug(debug, true)
        }
    }

}
