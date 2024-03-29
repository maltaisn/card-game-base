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

import com.maltaisn.cardgame.CardGameListener
import com.maltaisn.cardgame.game.drawTop
import com.maltaisn.cardgame.pcard.PCard
import com.maltaisn.cardgame.tests.core.ActionBarTest
import com.maltaisn.cardgame.widget.CardGameLayout
import com.maltaisn.cardgame.widget.card.CardContainer
import com.maltaisn.cardgame.widget.card.CardHand
import ktx.log.info


class CardHandTest(listener: CardGameListener) : ActionBarTest(listener) {

    override fun layout(layout: CardGameLayout) {
        super.layout(layout)

        val deck = PCard.fullDecks(shuffled = true)

        val hand = CardHand(pcardStyle).apply {
            cards = deck.drawTop(6)
            visibility = CardContainer.Visibility.MIXED
        }
        layout.centerTable.add(hand).pad(30f).grow()

        // Action buttons
        addTwoStateActionBtn("Disable", "Enable") { _, enabled ->
            hand.enabled = enabled
        }
        addTwoStateActionBtn("Vertical", "Horizontal") { _, isHorizontal ->
            hand.horizontal = isHorizontal
            hand.invalidateHierarchy()
        }
        addToggleBtn("Enable click listener") { _, enabled ->
            hand.clickListener = if (enabled) {
                { actor, index ->
                    info { "Card ${actor.card} at index $index was clicked." }
                    actor.shown = !actor.shown
                }
            } else null
        }
        addToggleBtn("Enable long click listener") { _, enabled ->
            hand.longClickListener = if (enabled) {
                { actor, index ->
                    info { "Card ${actor.card} at index $index was long clicked." }
                }
            } else null
        }
    }
}
