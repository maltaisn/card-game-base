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
import com.badlogic.gdx.utils.Align
import com.maltaisn.cardgame.CardGameListener
import com.maltaisn.cardgame.game.drawTop
import com.maltaisn.cardgame.pcard.PCard
import com.maltaisn.cardgame.tests.core.CardGameTest
import com.maltaisn.cardgame.tests.core.CenterLayout
import com.maltaisn.cardgame.widget.CardGameLayout
import com.maltaisn.cardgame.widget.card.CardActor
import com.maltaisn.cardgame.widget.card.CardContainer
import com.maltaisn.cardgame.widget.card.CardHand


/**
 * Test to show feasibility of a solitaire-like game.
 * Cards of multiple vertical card hands can be moved to other hands or dragged in hand.
 * Also to test drag and play animations and listeners in [CardHand].
 */
class SolitaireTest(listener: CardGameListener) : CardGameTest(listener) {

    override fun layout(layout: CardGameLayout) {
        super.layout(layout)

        val deck = PCard.fullDecks(shuffled = true)

        val animGroup = layout.cardAnimationGroup

        repeat(4) {
            val column = CardHand(pcardStyle)
            layout.centerTable.add(CenterLayout(column))
                    .pad(60f, 40f, 60f, 40f).grow()
            animGroup.register(column)

            column.apply {
                horizontal = false
                cardSize = CardActor.SIZE_NORMAL
                align = Align.top
                cards = deck.drawTop(5)
                dragListener = { actor ->
                    val start = column.actors.indexOf(actor)
                    val actors = mutableListOf<CardActor>()
                    for (i in start until column.size) {
                        column.actors[i]?.let { actors += it }
                    }
                    val dragger = animGroup.dragCards(*actors.toTypedArray())
                    dragger?.rearrangeable = true
                    dragger
                }
                playListener = object : CardContainer.PlayListener {
                    override fun canCardsBePlayed(actors: List<CardActor>, src: CardContainer, pos: Vector2) = true

                    override fun onCardsPlayed(actors: List<CardActor>, src: CardContainer, pos: Vector2) {
                        var insertPos = column.findInsertPositionForCoordinates(pos.x, pos.y)
                        for (actor in actors) {
                            animGroup.moveCard(src, column,
                                    src.actors.indexOf(actor), insertPos)
                            insertPos++
                        }
                    }
                }
            }
        }
    }

}
