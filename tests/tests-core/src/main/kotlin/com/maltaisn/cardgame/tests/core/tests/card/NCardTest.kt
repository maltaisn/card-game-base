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

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.maltaisn.cardgame.CardGameListener
import com.maltaisn.cardgame.game.Card
import com.maltaisn.cardgame.game.drawTop
import com.maltaisn.cardgame.tests.core.CardGameTest
import com.maltaisn.cardgame.tests.core.TestRes
import com.maltaisn.cardgame.widget.CardGameLayout
import com.maltaisn.cardgame.widget.card.CardActor
import com.maltaisn.cardgame.widget.card.CardHand
import ktx.assets.load
import ktx.style.get


/**
 * A test to demonstrate a custom [Card] class being used.
 */
class NCardTest(listener: CardGameListener) : CardGameTest(listener) {

    private lateinit var ncardStyle: CardActor.CardStyle


    override fun load() {
        super.load()
        assetManager.load<TextureAtlas>(TestRes.NCARD_ATLAS)
    }

    override fun start() {
        super.start()

        addSkin(TestRes.NCARD_SKIN, TestRes.NCARD_ATLAS)
        ncardStyle = skin["ncard"]
    }

    override fun layout(layout: CardGameLayout) {
        super.layout(layout)

        val animGroup = layout.cardAnimationGroup

        val deck = NCard.spiteAndMaliceDeck(shuffled = true)

        val hand = CardHand(ncardStyle)
        hand.apply {
            cards = deck.drawTop(8)
            highlightable = true
            maxCardSpacing = 200f
            dragListener = { actor ->
                val dragger = animGroup.dragCards(actor)
                dragger?.rearrangeable = true
                dragger
            }
            cardSize = CardActor.SIZE_NORMAL
        }
        layout.centerTable.add(hand).grow().pad(80f)
    }

    class NCard private constructor(value: Int) : Card(value) {

        override fun toString(): String = if (value == 0) "Wild" else value.toString()

        companion object {
            private val cards = List(13) { NCard(it) }

            val WILD = cards[0]

            operator fun invoke(value: Int) = requireNotNull(cards.getOrNull(value)) {
                "No card exist with this value."
            }

            fun spiteAndMaliceDeck(shuffled: Boolean = false): MutableList<NCard> {
                val cards = ArrayList<NCard>(162)
                repeat(3) {
                    repeat(12) { value ->
                        repeat(4) {
                            cards += NCard(value + 1)
                        }
                    }
                    repeat(6) {
                        cards += WILD
                    }
                }
                if (shuffled) {
                    cards.shuffle()
                }
                return cards
            }
        }
    }

}
