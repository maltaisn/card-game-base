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
import com.maltaisn.cardgame.widget.card.CardActor
import com.maltaisn.cardgame.widget.card.CardAnimationGroup
import com.maltaisn.cardgame.widget.card.CardContainer
import com.maltaisn.cardgame.widget.card.CardHand


/**
 * Test all possible combinations of options for [CardAnimationGroup.deal].
 */
class CardDealTest(listener: CardGameListener) : ActionBarTest(listener) {

    override fun layout(layout: CardGameLayout) {
        super.layout(layout)

        val topHand = CardHand(pcardStyle).apply {
            cards = PCard.fullDecks().drawTop(16)
            cardSize = CardActor.SIZE_SMALL
        }

        val bottomHand = CardHand(pcardStyle).apply {
            cardSize = CardActor.SIZE_SMALL
        }

        val animGroup = layout.cardAnimationGroup
        animGroup.register(topHand, bottomHand)

        // Action buttons
        var dealToTop = false
        var replaceSrc = false
        var replaceDst = false
        var fromLast = true
        var toLast = true
        var delay = CardAnimationGroup.DEFAULT_UPDATE_DURATION
        var duration = delay

        addActionBtn("Deal") {
            if (animGroup.animationRunning) {
                animGroup.dispatchDelayedMoves()
                animGroup.completeAnimation()
            }

            val src: CardContainer
            val dst: CardContainer
            if (dealToTop) {
                src = bottomHand
                dst = topHand
            } else {
                src = topHand
                dst = bottomHand
            }

            dst.cards = if (replaceDst) {
                arrayOfNulls<PCard>(src.size).toList()
            } else {
                emptyList()
            }
            animGroup.deal(src, dst, src.size,
                    replaceSrc, replaceDst, fromLast, toLast,
                    delay, duration)
            dealToTop = !dealToTop
        }
        addToggleBtn("Replace src", startState = replaceSrc) { _, state -> replaceSrc = state }
        addToggleBtn("Replace dst", startState = replaceDst) { _, state -> replaceDst = state }
        addToggleBtn("Src from last", startState = fromLast) { _, state -> fromLast = state }
        addToggleBtn("Dst to last", startState = toLast) { _, state -> toLast = state }
        addValueBtn("Delay", 0f, 1f, delay, 0.1f) { _, value, _ ->
            delay = value
        }
        addValueBtn("Duration", 0f, 1f, duration, 0.1f) { _, value, _ ->
            duration = value
        }

        // Do the layout
        layout.centerTable.apply {
            add(topHand).pad(60f).grow().row()
            add(bottomHand).pad(60f).grow()
        }
    }

}
