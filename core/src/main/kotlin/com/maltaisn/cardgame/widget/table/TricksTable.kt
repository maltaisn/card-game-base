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

package com.maltaisn.cardgame.widget.table

import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.utils.Align
import com.maltaisn.cardgame.game.Card
import com.maltaisn.cardgame.widget.card.CardActor
import com.maltaisn.msdfgdx.FontStyle
import com.maltaisn.msdfgdx.widget.MsdfLabel
import ktx.style.get


/**
 * A table view displaying a list of the card played by each player in the tricks of a round.
 * Each card can show a checkbox to indicate which player took the trick for example.
 * The headers have only a title to show player names.
 */
class TricksTable(skin: Skin, private val cardStyle: CardActor.CardStyle, playerCount: Int) :
        TableView(skin, List(playerCount) { 1f }) {

    private val style: TricksTableStyle = skin.get()

    /**
     * The headers for each column.
     */
    var headers = List(playerCount) { "" }
        set(value) {
            field = value
            headerAdapter?.notifyChanged()
        }

    /**
     * The lists of trick cards in each row.
     */
    var cards = emptyList<List<TrickCard>>()
        set(value) {
            field = value
            cellAdapter?.notifyChanged()
        }

    /**
     * The size of the cards shown in each row.
     * See [CardActor.size].
     */
    var cardSize = CardActor.SIZE_SMALL
        set(value) {
            field = value
            cellAdapter?.notifyChanged()
        }


    init {
        alternateColors = false
        itemGroup.pad(10f, 0f, 10f, 0f)

        cellAdapter = object : CellAdapter() {

            override val rowCount: Int
                get() = cards.size

            override fun createViewHolder(column: Int) = TrickCardViewHolder()

            override fun bindViewHolder(viewHolder: ViewHolder, row: Int, column: Int) {
                (viewHolder as TrickCardViewHolder).bind(cards[row][column])
            }

        }

        headerAdapter = object : HeaderAdapter() {
            override fun createViewHolder(column: Int) = HeaderViewHolder()

            override fun bindViewHolder(viewHolder: ViewHolder, column: Int) {
                (viewHolder as HeaderViewHolder).bind(headers[column])
            }
        }
    }


    private inner class HeaderViewHolder : ViewHolder() {

        private val titleLabel = MsdfLabel(null, skin, style.headerFontStyle)

        init {
            table.add(titleLabel).growX().pad(20f)
            titleLabel.setWrap(true)
            titleLabel.setAlignment(Align.center)
        }

        fun bind(header: String) {
            titleLabel.txt = header
        }
    }

    private inner class TrickCardViewHolder : ViewHolder() {

        private val cardActor = CardActor(cardStyle)
        private val checkIcon = Image(style.checkIcon)

        init {
            cardActor.apply {
                enabled = false
                add().expand().row()
                add(checkIcon).size(120f, 120f).expand()
            }
            table.add(cardActor).expand().pad(20f)
        }

        fun bind(trickCard: TrickCard) {
            cardActor.card = trickCard.card
            cardActor.size = cardSize
            checkIcon.isVisible = trickCard.checked
        }
    }

    class TrickCard(val card: Card, val checked: Boolean)

    class TricksTableStyle {
        lateinit var headerFontStyle: FontStyle
        lateinit var checkIcon: Drawable
    }

}
