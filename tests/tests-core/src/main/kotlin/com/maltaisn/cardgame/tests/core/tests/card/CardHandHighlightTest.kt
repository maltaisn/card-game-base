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

package com.maltaisn.cardgame.tests.core.tests.card

import com.badlogic.gdx.utils.Align
import com.maltaisn.cardgame.CardGameListener
import com.maltaisn.cardgame.game.Card
import com.maltaisn.cardgame.game.drawTop
import com.maltaisn.cardgame.pcard.PCard
import com.maltaisn.cardgame.tests.core.ActionBarTest
import com.maltaisn.cardgame.widget.CardGameLayout
import com.maltaisn.cardgame.widget.card.CardHand
import kotlin.random.Random


/**
 * Test card highlighting methods of [CardHand].
 */
class CardHandHighlightTest(listener: CardGameListener) : ActionBarTest(listener) {

    override fun layout(layout: CardGameLayout) {
        super.layout(layout)

        val deck = PCard.fullDecks(shuffled = true)

        val hand = CardHand(pcardStyle).apply {
            cards = deck.drawTop(13)
            align = Align.bottom
            clipPercent = 0.3f
            highlightable = true
            highlightListener = { actor, highlighted ->
                // Only allow unhighlighting or highlighting red cards.
                !highlighted || (actor.card as PCard).color == PCard.RED
            }
        }
        layout.centerTable.add(hand).grow().pad(60f)

        addActionBtn("Highlight all") {
            // Test highlightAllCards method.
            var hasHighlighted = false
            for (actor in hand.actors) {
                if (actor?.highlighted == true) {
                    hasHighlighted = true
                    break
                }
            }
            hand.highlightAllCards(!hasHighlighted)
        }

        addActionBtn("Toggle random") {
            // Test highlightCards method.
            val cards = mutableListOf<Card>()
            for (actor in hand.actors) {
                if (actor != null && Random.nextBoolean()) {
                    cards += actor.card!!
                }
            }
            hand.highlightCards(cards, Random.nextBoolean())
        }

        addActionBtn("Toggle indirect") {
            // Test changing highlight state directly on actor then update with updateHighlight.
            val actor = hand.actors.random()!!
            actor.highlighted = !actor.highlighted
            hand.updateHighlight()
        }
    }

}
