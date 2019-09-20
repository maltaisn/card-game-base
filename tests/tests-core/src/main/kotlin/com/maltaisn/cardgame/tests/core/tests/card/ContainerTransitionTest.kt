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


/**
 * Test for all of [CardContainer] show and hide transitions.
 */
class ContainerTransitionTest(listener: CardGameListener) : ActionBarTest(listener) {

    override fun layout(layout: CardGameLayout) {
        super.layout(layout)

        val deck = PCard.fullDecks(shuffled = true)

        val hand = CardHand(pcardStyle)
        hand.cards = deck.drawTop(13)
        layout.centerTable.add(hand).grow()

        addActionBtn("Instant") { hand.isVisible = !hand.shown }
        addActionBtn("Fade") { hand.fade(!hand.shown) }
        addActionBtn("Up") { hand.slide(!hand.shown, CardContainer.Direction.UP) }
        addActionBtn("Down") { hand.slide(!hand.shown, CardContainer.Direction.DOWN) }
        addActionBtn("Left") { hand.slide(!hand.shown, CardContainer.Direction.LEFT) }
        addActionBtn("Right") { hand.slide(!hand.shown, CardContainer.Direction.RIGHT) }
    }

}
