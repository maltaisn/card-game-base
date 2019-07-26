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
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.maltaisn.cardgame.game.drawTop
import com.maltaisn.cardgame.pcard.PCard
import com.maltaisn.cardgame.tests.core.SingleActionTest
import com.maltaisn.cardgame.widget.CardGameLayout
import com.maltaisn.cardgame.widget.card.*


/**
 * Test [CardAnimationLayer] animations between card containers,
 * test play listeners and dragging, etc.
 */
class CardAnimationTest : SingleActionTest() {

    override fun layout(layout: CardGameLayout) {
        super.layout(layout)

        val deck = PCard.fullDecks(shuffled = true)

        val animLayer = layout.cardAnimationLayer
        val centerTable = layout.gameLayer.centerTable

        val group1 = CardHand(pcardStyle)
        val group2 = CardHand(pcardStyle)
        val stack1 = CardStack(pcardStyle)
        val stack2 = CardStack(pcardStyle)

        animLayer.register(group1, group2, stack1, stack2)

        // Do the layout
        val table = Table()
        table.add(stack1).pad(40f).grow()
        table.add(stack2).pad(40f).grow()
        table.row()
        table.add(group2).colspan(2).pad(40f).padBottom(0f).grow()
        centerTable.add(group1).pad(40f).fill()
        centerTable.add(table).grow()

        group1.apply {
            sorter = PCard.DEFAULT_SORTER
            cardSize = CardActor.SIZE_SMALL
            horizontal = false
            cards = deck.drawTop(3)
            sort()
            clickListener = { _, index ->
                animLayer.moveCard(group1, stack2, index, stack2.size)
                group1.sort()
                animLayer.update()
            }
            longClickListener = { _, index ->
                animLayer.moveCard(group1, group2, index, 0)
                group1.sort()
                group2.sort()
                animLayer.update()
            }
            dragListener = { actor ->
                if ((actor.card as PCard).color == PCard.RED) {
                    animLayer.dragCards(actor)
                } else {
                    null
                }
            }
            playListener = object : CardContainer.PlayListener {
                override fun canCardsBePlayed(actors: List<CardActor>, src: CardContainer, pos: Vector2): Boolean {
                    return src === group2 && (actors.first().card as PCard).color == PCard.BLACK
                }

                override fun onCardsPlayed(actors: List<CardActor>, src: CardContainer, pos: Vector2) {
                    animLayer.moveCard(src, group1,
                            src.actors.indexOf(actors.first()), 0)
                    group1.sort()
                    (src as? CardHand)?.sort()
                }
            }
        }

        group2.apply {
            sorter = PCard.DEFAULT_SORTER
            cardSize = CardActor.SIZE_BIG
            align = Align.bottom
            clipPercent = 0.3f
            cards = deck.drawTop(6)
            sort()
            clickListener = { _, index ->
                animLayer.moveCard(group2, group1, index, 0)
                group1.sort()
                group2.sort()
                animLayer.update()
            }
            dragListener = { actor ->
                animLayer.dragCards(actor)
            }
        }

        stack1.apply {
            visibility = CardContainer.Visibility.NONE
            cards = PCard.fullDecks(shuffled = true)
            clickListener = { _, index ->
                animLayer.moveCard(stack1, group2, index, 0)
                group2.sort()
                animLayer.update()
            }
            dragListener = { actor -> animLayer.dragCards(actor) }
            playListener = object : CardContainer.PlayListener {
                override fun canCardsBePlayed(actors: List<CardActor>, src: CardContainer, pos: Vector2): Boolean {
                    return src === group1
                }

                override fun onCardsPlayed(actors: List<CardActor>, src: CardContainer, pos: Vector2) {
                    animLayer.moveCard(src, stack1,
                            src.actors.indexOf(actors.first()), stack1.size)
                    (src as? CardHand)?.sort()
                }
            }
        }

        stack2.apply {
            visibility = CardContainer.Visibility.ALL
            drawSlot = true
            cardSize = CardActor.SIZE_NORMAL
            cards = deck.drawTop(1)
            clickListener = { _, index ->
                animLayer.moveCard(stack2, stack1, index, stack1.size)
                animLayer.update()
            }
            playListener = object : CardContainer.PlayListener {
                override fun canCardsBePlayed(actors: List<CardActor>, src: CardContainer, pos: Vector2): Boolean {
                    return src === group1 || src === stack1
                }

                override fun onCardsPlayed(actors: List<CardActor>, src: CardContainer, pos: Vector2) {
                    animLayer.moveCard(src, stack2,
                            src.actors.indexOf(actors.first()), stack2.size)
                    (src as? CardHand)?.sort()
                }
            }
        }

        action = {
            animLayer.deal(stack1, group2, 10) {
                group2.sort()
            }
        }
    }

}
