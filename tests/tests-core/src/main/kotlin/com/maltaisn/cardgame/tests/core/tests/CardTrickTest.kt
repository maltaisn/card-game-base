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
import com.maltaisn.cardgame.core.PCard
import com.maltaisn.cardgame.tests.core.ActionBarTest
import com.maltaisn.cardgame.tests.core.CenterLayout
import com.maltaisn.cardgame.widget.CardGameLayout
import com.maltaisn.cardgame.widget.card.CardActor
import com.maltaisn.cardgame.widget.card.CardContainer
import com.maltaisn.cardgame.widget.card.CardStack
import com.maltaisn.cardgame.widget.card.CardTrick
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random


/**
 * Test all the [CardTrick] container options.
 */
class CardTrickTest : ActionBarTest() {

    override fun layout(layout: CardGameLayout) {
        super.layout(layout)

        val deck = PCard.fullDeck(false)
        deck.shuffle()

        val animLayer = layout.cardAnimationLayer

        // Create card containers
        val trick = CardTrick(coreSkin, cardSkin, 4)
        trick.apply {
            cards = deck.drawTop(4)
            dragListener = { actor ->
                val dragger = animLayer.dragCards(actor)
                dragger?.rearrangeable = true
                dragger
            }
            playListener = object : CardContainer.PlayListener {
                override fun canCardsBePlayed(actors: List<CardActor>, src: CardContainer, pos: Vector2): Boolean {
                    val index = trick.findInsertPositionForCoordinates(pos.x, pos.y)
                    return trick.actors[index] == null
                }

                override fun onCardsPlayed(actors: List<CardActor>, src: CardContainer, pos: Vector2) {
                    val index = trick.findInsertPositionForCoordinates(pos.x, pos.y)
                    animLayer.moveCard(src, trick, src.actors.indexOf(actors.first()), index, replaceDst = true)
                }
            }
        }

        val stack = CardStack(coreSkin, cardSkin).apply {
            cards = deck
            dragListener = { animLayer.dragCards(it) }
        }

        animLayer.register(trick, stack)

        // Action buttons
        addActionBtn("Clear") {
            // Move cards from trick to stack
            for ((i, card) in trick.cards.withIndex()) {
                if (card != null) {
                    animLayer.moveCard(trick, stack, i, 0, true)
                }
            }
            animLayer.update()
        }
        addActionBtn("-") {
            // Decrease capacity
            val newCapacity = max(1, trick.capacity - 1)
            if (newCapacity != trick.capacity) {
                val cards = trick.cards

                // Move extra cards to stack
                for (i in newCapacity until cards.size) {
                    if (cards[i] != null) {
                        animLayer.moveCard(trick, stack, i, 0, true)
                    }
                }
                animLayer.update()

                trick.capacity = newCapacity
                trick.requestUpdate()
                animLayer.update()
            }
        }
        addActionBtn("+") {
            // Increase capacity
            val newCapacity = min(10, trick.capacity + 1)
            if (newCapacity != trick.capacity) {
                trick.capacity = newCapacity
                trick.requestUpdate()
                animLayer.update()
            }
        }
        addTwoStateActionBtn("Cc", "Cw") { _, state ->
            // Toggle clockwise placement
            trick.clockwisePlacement = state
            trick.requestUpdate()
            animLayer.update()
        }
        addActionBtn("Rnd") {
            // Randomize card angles
            val angles = MutableList(trick.capacity) { Random.nextDouble(PI * 2).toFloat() }
            angles.sort()
            trick.cardAngles = angles
            trick.requestUpdate()
            animLayer.update()
        }
        addActionBtn("Rx-") {
            // Decrease horizontal radius
            trick.radius.x = max(trick.radius.x - 10f, 20f)
            trick.requestUpdate()
            animLayer.update()
        }
        addActionBtn("Rx+") {
            // Increase horizontal radius
            trick.radius.x = min(trick.radius.x + 10f, 200f)
            trick.requestUpdate()
            animLayer.update()
        }
        addActionBtn("Ry-") {
            // Decrease vertical radius
            trick.radius.y = max(trick.radius.y - 10f, 20f)
            trick.requestUpdate()
            animLayer.update()
        }
        addActionBtn("Ry+") {
            // Increase vertical radius
            trick.radius.y = min(trick.radius.y + 10f, 200f)
            trick.requestUpdate()
            animLayer.update()
        }
        addActionBtn("Auto R") {
            // Set auto radius
            trick.setAutoRadius()
            trick.requestUpdate()
            animLayer.update()
        }
        addActionBtn("Angle+") {
            // Increase start angle
            trick.startAngle += (PI / 8).toFloat()
            trick.requestUpdate()
            animLayer.update()
        }
        addToggleBtn("Debug") { _, debug ->
            trick.debug = debug
        }

        // Do the layout
        layout.gameLayer.centerTable.apply {
            add(stack).width(200f).growY()
            add(CenterLayout(trick)).grow().row()
        }
    }

}