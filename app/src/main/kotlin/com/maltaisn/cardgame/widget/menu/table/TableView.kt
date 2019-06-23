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

import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.utils.Scaling
import com.maltaisn.cardgame.post
import com.maltaisn.cardgame.widget.ScrollView
import ktx.assets.pool
import ktx.style.get


open class TableView(skin: Skin, columnWidths: List<Float>) : Table(skin) {

    private val style: TableViewStyle = skin.get()

    /** The cell adapter. If `null`, no rows will be displayed. */
    var cellAdapter: CellAdapter? = null
        set(value) {
            field = value
            value?.let {
                it.tableView = this
                it.notifyChanged()
            }
        }

    /** The header adapter. If `null`, no header will be displayed. */
    var headerAdapter: HeaderAdapter? = null
        set(value) {
            field = value
            value?.let {
                it.tableView = this
                it.notifyChanged()
            }
            updateLayout()
        }

    /** The footer adapter. If `null`, no footer will be displayed. */
    var footerAdapter: FooterAdapter? = null
        set(value) {
            field = value
            value?.let {
                it.tableView = this
                it.notifyChanged()
            }
            updateLayout()
        }

    val columnCount: Int
        get() = columnWidths.size

    /** The width of each columns in percent. */
    val columnWidths: List<Float>

    /** Whether to alternate row colors or used the same color for all rows. */
    var alternateColors = false
        set(value) {
            field = value
            cellAdapter?.notifyChanged()
        }


    val headerTable = Table()
    val headerSeparator = Image(style.separator, Scaling.stretchX)

    val footerTable = Table()

    val itemGroup = VerticalGroup()
    val itemScrollView = ScrollView(itemGroup)

    private val topTable = Table()


    init {
        // Set column widths in percent
        val sum = columnWidths.sum()
        this.columnWidths = columnWidths.map { it / sum }

        topTable.background = style.background
        footerTable.background = style.background

        itemGroup.grow()

        populateTableRow(headerTable)
        populateTableRow(footerTable)

        updateLayout()
    }


    /** Update all adapters. */
    fun notifyChanged() {
        headerAdapter?.notifyChanged()
        cellAdapter?.notifyChanged()
        footerAdapter?.notifyChanged()
    }

    private fun populateTableRow(table: Table) {
        table.clearChildren()
        for (columnWidth in columnWidths) {
            // The minWidth CAN'T be set to a percent value because the table
            // will get larger and larger on each layout. This might be caused by the
            // background image, the rounding, or something else, but I don't know what.
            val width = Value.percentWidth(columnWidth, table)
            table.add().grow().maxWidth(width).prefWidth(width)
        }
    }

    private fun updateLayout() {
        clearChildren()
        topTable.clearChildren()

        if (headerAdapter != null) {
            topTable.add(headerTable).growX().row()
            topTable.add(headerSeparator).growX().row()
        }

        topTable.add(itemScrollView).grow().row()
        add(topTable).grow().row()

        if (footerAdapter != null) {
            add(footerTable).growX().padTop(20f)
        }
    }

    abstract class ViewHolder {

        lateinit var cell: Cell<*>
            internal set

        val table = Table()

        var row = POSITION_NONE
            internal set

        var column = POSITION_NONE
            internal set

    }

    abstract class Adapter {

        lateinit var tableView: TableView
            internal set

        private var hasChanged = false

        /**
         * Notify the adapter that its data set was changed.
         * All view holders will be rebound on next frame.
         */
        fun notifyChanged() {
            check(::tableView.isInitialized) { "Adapter must be attached to a table view before updating." }
            hasChanged = true
            tableView.post {
                hasChanged = false
                update()
            }
        }

        protected abstract fun update()
    }

    abstract class CellAdapter : Adapter() {
        /** The number of rows in the table. */
        abstract val rowCount: Int

        private val viewHolders by lazy {
            List(tableView.columnCount) { mutableListOf<ViewHolder>() }
        }

        private val viewHolderPools by lazy {
            List(tableView.columnCount) { column ->
                pool {
                    val vh = createViewHolder(column)
                    viewHolders[column] += vh
                    vh
                }
            }
        }

        private val tableRowPool = pool {
            val tableRow = Table()
            tableView.populateTableRow(tableRow)
            tableRow
        }

        abstract fun createViewHolder(column: Int): ViewHolder

        abstract fun bindViewHolder(viewHolder: ViewHolder, row: Int, column: Int)

        override fun update() {
            // Free old view holders
            for (column in 0 until tableView.columnCount) {
                for (vh in viewHolders[column]) {
                    viewHolderPools[column].free(vh)
                }
                viewHolders[column].clear()
            }
            for (child in tableView.itemGroup.children) {
                tableRowPool.free(child as Table)
            }

            // Bind new items to a view holder and add them to the item group
            tableView.itemGroup.clearChildren()
            for (row in 0 until rowCount) {
                val tableRow = tableRowPool.obtain()
                tableView.itemGroup.addActor(tableRow)

                tableRow.background = if (tableView.alternateColors) {
                    if (row % 2 == 0) {
                        tableView.style.evenRowBackground
                    } else {
                        tableView.style.oddRowBackground
                    }
                } else {
                    null
                }

                for (column in 0 until tableView.columnCount) {
                    val vh = viewHolderPools[column].obtain()
                    vh.column = column
                    vh.row = row
                    vh.cell = tableRow.cells[column]
                    vh.cell.setActor(vh.table)
                    bindViewHolder(vh, row, column)
                }
            }
        }
    }

    abstract class RowAdapter : Adapter() {

        internal abstract val tableRow: Table

        private val viewHolders by lazy {
            val cells = tableRow.cells
            List(tableView.columnCount) { column ->
                val vh = createViewHolder(column)
                vh.column = column
                vh.cell = cells[column]
                vh.cell.setActor(vh.table)
                vh
            }
        }

        abstract fun createViewHolder(column: Int): ViewHolder

        abstract fun bindViewHolder(viewHolder: ViewHolder, column: Int)

        override fun update() {
            for (column in 0 until tableView.columnCount) {
                bindViewHolder(viewHolders[column], column)
            }
        }

    }

    abstract class HeaderAdapter : RowAdapter() {
        override val tableRow
            get() = tableView.headerTable
    }

    abstract class FooterAdapter : RowAdapter() {
        override val tableRow
            get() = tableView.footerTable
    }


    class TableViewStyle {
        lateinit var background: Drawable
        lateinit var separator: Drawable
        lateinit var evenRowBackground: Drawable
        lateinit var oddRowBackground: Drawable
    }

    companion object {
        const val POSITION_NONE = -1
    }

}