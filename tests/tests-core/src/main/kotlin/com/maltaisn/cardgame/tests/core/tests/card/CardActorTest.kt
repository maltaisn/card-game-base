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

import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.maltaisn.cardgame.game.drawTop
import com.maltaisn.cardgame.pcard.PCard
import com.maltaisn.cardgame.tests.core.ActionBarTest
import com.maltaisn.cardgame.widget.CardGameLayout
import com.maltaisn.cardgame.widget.card.CardActor
import ktx.log.info


/**
 * Test for [CardActor] options and layout.
 */
class CardActorTest : ActionBarTest() {

    override fun layout(layout: CardGameLayout) {
        super.layout(layout)

        val deck = PCard.fullDecks(shuffled = true)

        val cardTable = Table()
        layout.centerTable.add(cardTable).grow()

        val cardActors = List(4) {
            val actor = CardActor(pcardStyle, deck.drawTop())
            actor.clickListener = { info { "Card ${actor.card} clicked." } }
            actor.longClickListener = { info { "Card ${actor.card} long clicked." } }
            actor.size = it * 40f + 160f
            cardTable.add(actor).expand()
            actor
        }

        // Action buttons
        addTwoStateActionBtn("Disable", "Enable") { _, enabled ->
            for (actor in cardActors) {
                actor.enabled = enabled
            }
        }
        addTwoStateActionBtn("Hide", "Show") { _, shown ->
            for (actor in cardActors) {
                actor.shown = shown
            }
        }
        addToggleBtn("Debug") { _, debug ->
            layout.centerTable.setDebug(debug, true)
        }
    }
}
