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
import com.maltaisn.cardgame.game.drawTop
import com.maltaisn.cardgame.pcard.PCard
import com.maltaisn.cardgame.tests.core.ActionBarTest
import com.maltaisn.cardgame.tests.core.CenterLayout
import com.maltaisn.cardgame.widget.CardGameLayout
import com.maltaisn.cardgame.widget.card.CardActor
import com.maltaisn.cardgame.widget.card.CardContainer
import com.maltaisn.cardgame.widget.card.CardStack
import com.maltaisn.cardgame.widget.card.CardTrick
import ktx.log.info
import java.text.DecimalFormat
import kotlin.math.PI
import kotlin.random.Random


/**
 * Test all the [CardTrick] container options.
 */
class CardTrickTest : ActionBarTest() {

    override fun layout(layout: CardGameLayout) {
        super.layout(layout)

        val deck = PCard.fullDecks(shuffled = true)

        val animGroup = layout.cardAnimationGroup

        // Create card containers
        val trick = CardTrick(pcardStyle, 4)
        trick.apply {
            cards = deck.drawTop(4)
            dragListener = { actor ->
                val dragger = animGroup.dragCards(actor)
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
                    animGroup.moveCard(src, trick, src.actors.indexOf(actors.first()), index, replaceDst = true)
                }
            }
        }

        val stack = CardStack(pcardStyle).apply {
            cards = deck
            dragListener = { animGroup.dragCards(it) }
        }

        animGroup.register(trick, stack)

        // Action buttons
        addActionBtn("Clear") {
            // Move cards from trick to stack
            for ((i, card) in trick.cards.withIndex()) {
                if (card != null) {
                    animGroup.moveCard(trick, stack, i, 0, true)
                }
            }
            animGroup.update()
        }
        addValueBtn("Capacity", 1f, 10f,
                4f, 1f) { _, newCapacity, oldCapacity ->
            if (newCapacity < oldCapacity) {
                // Decrease capacity, moving extra cards from trick to stack.
                val cards = trick.cards
                for (i in newCapacity.toInt() until cards.size) {
                    if (cards[i] != null) {
                        animGroup.moveCard(trick, stack, i, 0, true)
                    }
                }
            }
            trick.capacity = newCapacity.toInt()
            trick.requestUpdate()
            animGroup.update()
        }
        addTwoStateActionBtn("Cc", "Cw") { _, state ->
            // Toggle clockwise placement
            trick.clockwisePlacement = state
            trick.requestUpdate()
            animGroup.update()
        }
        addActionBtn("Rnd") {
            // Randomize card angles
            val angles = MutableList(trick.capacity) { Random.nextDouble(PI * 2).toFloat() }
            angles.sort()
            trick.cardAngles = angles
            trick.requestUpdate()
            animGroup.update()
            info { "Random angles: ${trick.cardAngles}" }
        }
        val rxBtn = addValueBtn("Radius X", 40f, 300f,
                trick.radius.x, 20f, null) { _, value, _ ->
            trick.radius.x = value
            trick.requestUpdate()
            animGroup.update()
        }
        val ryBtn = addValueBtn("Radius Y", 40f, 300f,
                trick.radius.y, 20f, null) { _, value, _ ->
            trick.radius.y = value
            trick.requestUpdate()
            animGroup.update()
        }
        addActionBtn("Auto R") {
            // Set auto radius
            trick.setAutoRadius()
            rxBtn.value = trick.radius.x
            ryBtn.value = trick.radius.y
        }
        addValueBtn("Start angle", 0f, 340f, 0f, 20f,
                DecimalFormat().apply { positiveSuffix = "°" }) { _, value, _ ->
            trick.startAngle = value / 180f * PI.toFloat()
            trick.requestUpdate()
            animGroup.update()
        }
        addToggleBtn("Debug") { _, debug ->
            trick.debug = debug
        }

        // Do the layout
        layout.centerTable.apply {
            add(stack).width(400f).growY()
            add(CenterLayout(trick)).grow().row()
        }
    }

}
