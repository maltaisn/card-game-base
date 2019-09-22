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

import com.badlogic.gdx.math.Vector2
import com.maltaisn.cardgame.CardGameListener
import com.maltaisn.cardgame.game.drawTop
import com.maltaisn.cardgame.pcard.PCard
import com.maltaisn.cardgame.tests.core.CardGameTest
import com.maltaisn.cardgame.widget.CardGameLayout
import com.maltaisn.cardgame.widget.card.CardActor
import com.maltaisn.cardgame.widget.card.CardContainer
import com.maltaisn.cardgame.widget.card.CardStack


/**
 * Test for [CardStack] drag and play animations and slot drawing.
 */
class CardStackTest(listener: CardGameListener) : CardGameTest(listener) {

    override fun layout(layout: CardGameLayout) {
        super.layout(layout)

        val deck = PCard.fullDecks(shuffled = true)

        val animGroup = layout.cardAnimationGroup

        val stacks = List(3) {
            CardStack(pcardStyle).apply {
                cards = deck.drawTop(3)
                dragListener = { animGroup.dragCards(it) }
                drawSlot = true
                playListener = object : CardContainer.PlayListener {
                    override fun canCardsBePlayed(actors: List<CardActor>, src: CardContainer, pos: Vector2) = true

                    override fun onCardsPlayed(actors: List<CardActor>, src: CardContainer, pos: Vector2) {
                        animGroup.moveCard(src, this@apply, src.size - 1, this@apply.size)
                    }
                }
            }
        }

        animGroup.register(stacks[0], stacks[1])

        layout.centerTable.apply {
            add(stacks[0]).grow()
            add(stacks[1]).grow()
        }
    }

}
