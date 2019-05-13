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

import com.badlogic.gdx.utils.Align
import com.maltaisn.cardgame.CardGameLayout
import com.maltaisn.cardgame.core.PCard
import com.maltaisn.cardgame.tests.core.SingleActionTest
import com.maltaisn.cardgame.widget.card.CardAnimationLayer
import com.maltaisn.cardgame.widget.card.CardHand
import com.maltaisn.cardgame.widget.card.CardStack


/**
 * Test [CardAnimationLayer.deal] from a hidden card stack to a card hand.
 * Test [CardAnimationLayer.completeAnimation] with delayed moves dispatch.
 */
class CardDealTest : SingleActionTest() {

    override fun layout(layout: CardGameLayout) {
        super.layout(layout)

        val deck = PCard.fullDeck(true)
        deck.shuffle()

        val animLayer = layout.cardAnimationLayer

        val hand = CardHand(coreSkin, cardSkin)
        hand.align = Align.bottom
        hand.clipPercent = 0.3f

        val stack = CardStack(coreSkin, cardSkin)
        stack.isVisible = false
        stack.cards = deck

        layout.gameLayer.centerTable.add(hand).grow()
        layout.gameLayer.bottomTable.add(stack).grow()

        action = {
            if (animLayer.animationRunning) {
                // Force complete last deal
                animLayer.completeAnimation(true)
            }
            animLayer.deal(stack, hand, 12)
        }
    }

}