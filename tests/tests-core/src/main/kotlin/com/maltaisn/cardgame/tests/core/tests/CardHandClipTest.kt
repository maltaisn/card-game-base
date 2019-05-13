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

import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.maltaisn.cardgame.CardGameLayout
import com.maltaisn.cardgame.core.PCard
import com.maltaisn.cardgame.tests.core.CardGameTest
import com.maltaisn.cardgame.widget.card.CardContainer
import com.maltaisn.cardgame.widget.card.CardHand


class CardHandClipTest : CardGameTest() {

    override fun layout(layout: CardGameLayout) {
        super.layout(layout)

        val deck = PCard.fullDeck(false)
        deck.shuffle()

        val bottom = CardHand(coreSkin, cardSkin).apply {
            cards = deck.drawTop(7)
            clipPercent = 0.3f
            align = Align.bottom
        }
        val top = CardHand(coreSkin, cardSkin).apply {
            cards = deck.drawTop(7)
            clipPercent = 0.3f
            align = Align.bottom
            rotation = 180f
        }
        val left = CardHand(coreSkin, cardSkin).apply {
            horizontal = false
            cards = deck.drawTop(7)
            clipPercent = 0.3f
            align = Align.left
        }
        val right = CardHand(coreSkin, cardSkin).apply {
            horizontal = false
            cards = deck.drawTop(7)
            clipPercent = -0.3f
            align = Align.right
            visibility = CardContainer.Visibility.NONE
        }

        layout.gameLayer.centerTable.apply {
            add(left).pad(30f, 0f, 30f, 30f).growY()
            add(Table().apply {
                add(top).growX().row()
                add().grow().row()
                add(bottom).growX()
            }).grow()
            add(right).pad(30f, 30f, 30f, 0f).growY()
        }
    }
}