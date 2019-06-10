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

import com.badlogic.gdx.math.Vector2
import com.maltaisn.cardgame.game.PCard
import com.maltaisn.cardgame.game.drawTop
import com.maltaisn.cardgame.tests.core.CardGameTest
import com.maltaisn.cardgame.widget.CardGameLayout
import com.maltaisn.cardgame.widget.card.CardActor
import com.maltaisn.cardgame.widget.card.CardContainer
import com.maltaisn.cardgame.widget.card.CardStack


/**
 * Test for [CardStack] drag and play animations and slot drawing.
 */
class CardStackTest : CardGameTest() {

    override fun layout(layout: CardGameLayout) {
        super.layout(layout)

        val deck = PCard.fullDecks(shuffled = true)

        val animLayer = layout.cardAnimationLayer

        val leftStack = CardStack(coreSkin, cardSkin).apply {
            cards = deck.drawTop(20)
            dragListener = { animLayer.dragCards(it) }
            drawSlot = true
            playListener = object : CardContainer.PlayListener {
                override fun canCardsBePlayed(actors: List<CardActor>, src: CardContainer, pos: Vector2) = true

                override fun onCardsPlayed(actors: List<CardActor>, src: CardContainer, pos: Vector2) {
                    animLayer.moveCard(src, this@apply, src.size - 1, this@apply.size)
                }
            }
        }

        val rightStack = CardStack(coreSkin, cardSkin).apply {
            cards = deck.drawTop(20)
            dragListener = { animLayer.dragCards(it) }
            playListener = object : CardContainer.PlayListener {
                override fun canCardsBePlayed(actors: List<CardActor>, src: CardContainer, pos: Vector2) = true

                override fun onCardsPlayed(actors: List<CardActor>, src: CardContainer, pos: Vector2) {
                    animLayer.moveCard(src, this@apply, src.size - 1, this@apply.size)
                }
            }
        }

        animLayer.register(leftStack, rightStack)

        layout.gameLayer.centerTable.apply {
            add(leftStack).grow()
            add(rightStack).grow()
        }
    }

}