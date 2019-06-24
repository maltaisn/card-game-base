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

package com.maltaisn.cardgame.widget.menu.table

import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.utils.Align
import com.maltaisn.cardgame.game.Card
import com.maltaisn.cardgame.widget.FontStyle
import com.maltaisn.cardgame.widget.SdfLabel
import com.maltaisn.cardgame.widget.card.CardActor
import ktx.style.get


/**
 * A table view displaying a list of the card played by each player in the tricks of a round.
 * Each card can show a checkbox to indicate which player took the trick for example.
 * The headers have only a title to show player names.
 * @property cardSkin The skin used to create the card actors.
 */
class TricksTable(coreSkin: Skin, private val cardSkin: Skin, playerCount: Int) :
        TableView(coreSkin, List(playerCount) { 1f }) {

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
     * The list of tricks for each column.
     * The cell adapter must be updated when changed.
     */
    val cards = mutableListOf<List<TrickCard>>()


    init {
        alternateColors = false

        cellAdapter = object : CellAdapter() {

            override val rowCount: Int
                get() = cards.size

            override fun createViewHolder(column: Int) = TrickCardViewHolder()

            override fun bindViewHolder(viewHolder: ViewHolder, row: Int, column: Int) {
                (viewHolder as TrickCardViewHolder).bind(cards[row][column])
            }

        }

        headerAdapter = object : HeaderAdapter() {
            override fun createViewHolder(column: Int) = HeaderViewHolder(skin)

            override fun bindViewHolder(viewHolder: ViewHolder, column: Int) {
                (viewHolder as HeaderViewHolder).bind(headers[column])
            }
        }
    }


    /**
     * Append a new [trick] to the list.
     * @param checkedPos The checked position, eg: the player who took the trick. Use `-1` for none.
     */
    fun addTrick(trick: List<Card>, checkedPos: Int) {
        require(trick.size == columnCount) { "Trick size must match column count." }
        cards += List(columnCount) { TrickCard(trick[it], it == checkedPos) }
        cellAdapter?.notifyChanged()
    }


    private inner class HeaderViewHolder(skin: Skin) : ViewHolder() {

        private val titleLabel = SdfLabel(skin, style.headerFontStyle)

        init {
            table.add(titleLabel).growX().pad(10f).row()
            titleLabel.setWrap(true)
            titleLabel.setAlignment(Align.center)
        }

        fun bind(header: String) {
            titleLabel.setText(header)
        }
    }

    private inner class TrickCardViewHolder : ViewHolder() {

        private val cardActor = CardActor(skin, cardSkin)
        private val checkIcon = Image(style.checkIcon)

        init {
            cardActor.apply {
                size = CardActor.SIZE_NORMAL
                enabled = false
                add().expand().row()
                add(checkIcon).size(60f, 60f).expand()
            }
            table.add(cardActor).expand().pad(10f)
        }

        fun bind(trickCard: TrickCard) {
            cardActor.card = trickCard.card
            checkIcon.isVisible = trickCard.checked
        }
    }

    class TrickCard(val card: Card, val checked: Boolean)

    class TricksTableStyle {
        lateinit var headerFontStyle: FontStyle
        lateinit var checkIcon: Drawable
    }

}