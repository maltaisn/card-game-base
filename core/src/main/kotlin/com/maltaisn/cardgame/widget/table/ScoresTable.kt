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

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.utils.Align
import com.maltaisn.cardgame.defaultSize
import com.maltaisn.cardgame.widget.text.FontStyle
import com.maltaisn.cardgame.widget.text.SdfLabel
import ktx.style.get


/**
 * A table view displaying a list of scores for each round for all players.
 * Each header can contain a title and a subtitle, for the player name and difficulty for example.
 * The footer can display the sum of scores, but it has to be calculated and set separatedly.
 * Each cell displaying a score can have a positive or negative highlight.
 */
class ScoresTable(skin: Skin, playerCount: Int) : TableView(skin, List(playerCount) { 1f }) {

    private val style: ScoresTableStyle = skin.get()

    /**
     * The headers for each column.
     */
    var headers = List(playerCount) { Header("", null) }
        set(value) {
            require(value.size == columnCount) { "Wrong number of headers" }
            field = value
            headerAdapter?.notifyChanged()
        }

    /**
     * The list of scores for each column.
     * The cell adapter must be updated when changed.
     */
    val scores = mutableListOf<List<Score>>()

    /**
     * The scores shown in the footer for each column.
     * The footer adapter must be updated when changed.
     * Can be `null` for no footer.
     */
    var footerScores: List<Score>? = null
        set(value) {
            require(value == null || value.size == columnCount) { "Wrong number of footers" }
            field = value
            footerAdapter = if (value == null) null else scoreFooterAdapter
            footerAdapter?.notifyChanged()
        }

    private val scoreFooterAdapter = object : FooterAdapter() {
        override fun createViewHolder(column: Int) = ScoreViewHolder()

        override fun bindViewHolder(viewHolder: ViewHolder, column: Int) {
            if (footerScores != null) {
                (viewHolder as ScoreViewHolder).bind(footerScores!![column])
            }
        }
    }

    init {
        alternateColors = true

        cellAdapter = object : CellAdapter() {

            override val rowCount: Int
                get() = scores.size

            override fun createViewHolder(column: Int) = ScoreViewHolder()

            override fun bindViewHolder(viewHolder: ViewHolder, row: Int, column: Int) {
                (viewHolder as ScoreViewHolder).bind(scores[row][column])
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

        private val titleLabel = SdfLabel(skin, style.headerTitleFontStyle)
        private val subtitleLabel = SdfLabel(skin, style.headerSubtitleFontStyle)

        init {
            table.add(titleLabel).growX().row()
            titleLabel.setWrap(true)
            titleLabel.setAlignment(Align.center)

            table.add(subtitleLabel).growX()
            subtitleLabel.setWrap(true)
            subtitleLabel.setAlignment(Align.center)

            table.pad(20f)
        }

        fun bind(header: Header) {
            titleLabel.setText(header.title)
            subtitleLabel.setText(header.subtitle)

            val subtitleCell = table.getCell(subtitleLabel)
            if (header.subtitle == null) {
                subtitleCell.size(0f, 0f)
            } else {
                subtitleCell.defaultSize()
            }
        }
    }

    private inner class ScoreViewHolder : ViewHolder() {

        private val scoreLabel = SdfLabel(skin, style.scoreFontStyle)

        init {
            table.add(scoreLabel).expand()
            table.pad(10f)
        }

        fun bind(score: Score) {
            cell.pad(15f, 25f, 15f, 25f)
            table.background = when (score.highlight) {
                Score.Highlight.NONE -> null
                Score.Highlight.POSITIVE -> style.scoreHighlightPositive
                Score.Highlight.NEGATIVE -> style.scoreHighlightNegative
            }

            scoreLabel.setText(score.value)
            scoreLabel.fontStyle = if (score.highlight == Score.Highlight.NONE) {
                style.scoreFontStyle
            } else {
                style.scoreFontStyleHighlighted
            }
        }
    }


    class Header(val title: String, val subtitle: String?)

    class Score(val value: String, val highlight: Highlight = Highlight.NONE) {

        enum class Highlight {
            NONE,
            POSITIVE,
            NEGATIVE
        }
    }

    class ScoresTableStyle {
        lateinit var headerTitleFontStyle: FontStyle
        lateinit var headerSubtitleFontStyle: FontStyle
        lateinit var scoreFontStyle: FontStyle
        lateinit var scoreFontStyleHighlighted: FontStyle
        lateinit var scoreHighlightPositive: Drawable
        lateinit var scoreHighlightNegative: Drawable
    }

}
