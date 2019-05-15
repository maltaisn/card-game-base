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

import com.maltaisn.cardgame.CardGameLayout
import com.maltaisn.cardgame.core.Card
import com.maltaisn.cardgame.core.PCard
import com.maltaisn.cardgame.tests.core.SingleActionTest
import com.maltaisn.cardgame.widget.card.CardHand


/**
 * Test null card slots in [CardHand].
 * Cards are exchanged between two card hands on keypress.
 */
class CardNullDealTest : SingleActionTest() {

    override fun layout(layout: CardGameLayout) {
        super.layout(layout)

        val deck = PCard.fullDeck(false)
        deck.shuffle()

        val count = 16

        val hand1 = CardHand(coreSkin, cardSkin)
        hand1.cards = deck.drawTop(count)

        val hand2 = CardHand(coreSkin, cardSkin)
        hand2.cards = arrayOfNulls<Card>(count).toList()

        val animLayer = layout.cardAnimationLayer
        animLayer.register(hand1, hand2)

        action = {
            if (animLayer.animationRunning) {
                animLayer.completeAnimation(true)
            }
            if (hand1.actors.first() != null) {
                animLayer.deal(hand1, hand2, count, replaceSrc = true, replaceDst = true)
            } else {
                animLayer.deal(hand2, hand1, count, replaceSrc = true, replaceDst = true)
            }
        }

        // Do the layout
        layout.gameLayer.centerTable.apply {
            add(hand1).pad(30f).grow().row()
            add(hand2).pad(30f).grow()
        }
    }

}
