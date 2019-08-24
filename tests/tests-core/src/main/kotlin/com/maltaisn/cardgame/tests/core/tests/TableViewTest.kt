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

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.maltaisn.cardgame.tests.core.SubmenuContentTest
import com.maltaisn.cardgame.widget.CardGameLayout
import com.maltaisn.cardgame.widget.table.TableView
import com.maltaisn.cardgame.widget.text.FontStyle
import com.maltaisn.cardgame.widget.text.SdfLabel
import ktx.actors.onClick
import ktx.log.info
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random


class TableViewTest : SubmenuContentTest() {

    private lateinit var tableView: TableView
    private var selectedColumn = -1

    override fun layoutContent(layout: CardGameLayout, content: Table) {
        val items = MutableList(8) { createPerson() }
        val headers = listOf("ID", "Name", "Date of birth", "Sex")

        // Set up the table view
        tableView = TableView(skin, listOf(1f, 3f, 2f, 1f))

        tableView.cellAdapter = object : TableView.CellAdapter() {
            override val rowCount: Int
                get() = items.size

            override fun createViewHolder(column: Int): TextViewHolder {
                info { "Created view holder for column $column" }
                return TextViewHolder()
            }

            override fun bindViewHolder(viewHolder: TableView.ViewHolder, row: Int, column: Int) {
                assert(row == viewHolder.row)
                assert(column == viewHolder.column)

                val person = items[row]
                (viewHolder as TextViewHolder).bind(when (column) {
                    0 -> person.uuid.toString().substring(0, 4)
                    1 -> person.name
                    2 -> DATE_FORMAT.format(person.dateOfBirth)
                    else -> person.sex.toString()
                }, selectedColumn == column)
            }
        }

        val headerAdapter = object : TableView.HeaderAdapter() {
            override fun createViewHolder(column: Int) = HeaderViewHolder()

            override fun bindViewHolder(viewHolder: TableView.ViewHolder, column: Int) {
                (viewHolder as HeaderViewHolder).bind(headers[column])
            }
        }
        tableView.headerAdapter = headerAdapter

        val footerAdapter = object : TableView.FooterAdapter() {
            override fun createViewHolder(column: Int) = TextViewHolder()

            override fun bindViewHolder(viewHolder: TableView.ViewHolder, column: Int) {
                (viewHolder as TextViewHolder).bind(column.toString(), selectedColumn == column)
            }
        }
        tableView.footerAdapter = footerAdapter

        // Do the layout
        content.add(tableView).grow().pad(40f, 200f, 40f, 200f)


        // Action buttons
        addActionBtn("Add row") {
            items += createPerson()
            tableView.itemScrollView.scrollToBottom()
            tableView.cellAdapter?.notifyChanged()
        }
        addActionBtn("Remove row") {
            if (items.isNotEmpty()) {
                items.removeAt(items.indices.random())
            }
            tableView.cellAdapter?.notifyChanged()
        }
        addActionBtn("Scroll to bottom") {
            tableView.itemScrollView.scrollToBottom()
        }
        addToggleBtn("Show header", startState = true) { _, show ->
            tableView.headerAdapter = if (show) headerAdapter else null
        }
        addToggleBtn("Show footer", startState = true) { _, show ->
            tableView.footerAdapter = if (show) footerAdapter else null
        }
        addToggleBtn("Alternate row colors") { _, alternate ->
            tableView.alternateColors = alternate
        }
        addToggleBtn("Debug") { _, debug ->
            tableView.setDebug(debug, true)
        }
    }

    private fun createPerson() = Person(UUID.randomUUID(),
            FIRST_NAMES.random() + " " + LAST_NAMES.random(),
            Random.nextLong(1262322000000),
            if (Random.nextBoolean()) 'M' else 'F')

    private data class Person(val uuid: UUID, val name: String, val dateOfBirth: Long, val sex: Char)

    private inner class TextViewHolder : TableView.ViewHolder() {

        private val label = SdfLabel(skin, FONT_STYLE)

        init {
            label.setAlignment(Align.center)
            label.setWrap(true)

            table.add(label).pad(20f).grow()
            table.touchable = Touchable.enabled
            table.onClick {
                info { "Clicked on cell at ($column, $row) with text '${label.text}'" }
            }
        }

        fun bind(text: String, bold: Boolean) {
            cell.pad(20f)
            label.setText(text)
            label.fontStyle = if (bold) FONT_STYLE_BOLD else FONT_STYLE
        }

    }

    private inner class HeaderViewHolder : TableView.ViewHolder() {

        private val label = SdfLabel(skin, FONT_STYLE_BOLD)

        init {
            label.setAlignment(Align.center)
            label.setWrap(true)

            table.add(label).pad(20f).grow()
            table.touchable = Touchable.enabled
            table.onClick {
                selectedColumn = if (selectedColumn == column) -1 else column
                tableView.notifyChanged()
            }
        }

        fun bind(text: String) {
            cell.pad(20f)
            label.setText(text)
        }

    }

    companion object {
        private val FONT_STYLE = FontStyle(fontSize = 44f, fontColor = Color.BLACK)
        private val FONT_STYLE_BOLD = FONT_STYLE.copy(bold = true)
        private val DATE_FORMAT = SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH)

        private val FIRST_NAMES = listOf("Ned", "Robert", "Jon", "Daenerys", "Gendry")
        private val LAST_NAMES = listOf("Stark", "Baretheon", "Snow", "Targaryen", "Waters")
    }

}
