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
import com.maltaisn.cardgame.core.PCard
import com.maltaisn.cardgame.tests.core.CenterLayout
import com.maltaisn.cardgame.tests.core.SingleActionTest
import com.maltaisn.cardgame.widget.CardGameLayout
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

        val deck = PCard.fullDecks(shuffled = true)

        val hand = CardHand(coreSkin, cardSkin).apply {
            align = Align.bottom
            clipPercent = 0.3f
        }

        val stack = CardStack(coreSkin, cardSkin).apply {
            isVisible = false
            cards = deck
        }

        val animLayer = layout.cardAnimationLayer
        animLayer.register(hand, stack)

        action = {
            if (animLayer.animationRunning) {
                // Force complete last deal
                animLayer.completeAnimation(true)
            }
            if (stack.size >= 12) {
                animLayer.deal(stack, hand, 12)
            }
        }

        // Do the layout
        layout.gameLayer.centerTable.add(CenterLayout(hand)).grow()
        layout.gameLayer.bottomTable.add(stack).grow()

        isDebugAll = true
    }

}
