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

package com.maltaisn.cardgame.widget.stats

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.I18NBundle
import com.gmail.blueboxware.libgdxplugin.annotations.GDXAssets
import com.maltaisn.cardgame.CoreRes
import com.maltaisn.cardgame.stats.Statistics
import com.maltaisn.cardgame.widget.AlertDialog
import com.maltaisn.cardgame.widget.CoreIcons
import com.maltaisn.cardgame.widget.Dialog
import com.maltaisn.cardgame.widget.ScrollView
import com.maltaisn.cardgame.widget.menu.MenuItem
import com.maltaisn.cardgame.widget.menu.PagedSubMenu.Page
import com.maltaisn.cardgame.widget.menu.SubMenu
import ktx.actors.onClick
import ktx.style.get


/**
 * A sub menu with different pages, where items change pages.
 * Pages can be added with [addItem] where the item is a [Page].
 */
class StatsSubMenu(skin: Skin) : SubMenu(skin) {

    override var shown: Boolean
        get() = super.shown
        set(value) {
            super.shown = value

            if (value) {
                // Check first item and scroll to top.
                scrollView?.scrollToTop()
                if (stats?.variants != null) {
                    checkItem(items.first())
                }

                // Update stats values.
                refresh()
            }
        }

    override var itemClickListener: ((MenuItem) -> Unit)?
        get() = super.itemClickListener
        set(value) {
            super.itemClickListener = {
                if (it.id == ITEM_ID_RESET) {
                    resetDialog.show(stage!!)
                }

                value?.invoke(it)
            }
        }

    /**
     * The statistics shown by this menu.
     * A menu item will be created for each variant if there are variants.
     */
    var stats: Statistics? = null
        set(value) {
            field = value

            content.clearChildren()
            clearItems()

            if (value != null) {
                // Add items
                value.variants?.let { variants ->
                    // If there are variants, add a menu item for each of them.
                    val icon = skin.getDrawable(CoreIcons.CHEVRON_RIGHT)
                    for ((i, variant) in variants.withIndex()) {
                        addItem(MenuItem(i, variant, icon, ITEM_POS_TOP))
                    }
                    checkItem(items.first())
                }
                addItem(resetItem)

                // Set content
                statsGroup = StatsGroup(skin, value)
                scrollView = ScrollView(statsGroup)
                content.add(scrollView).grow()
            }
        }

    private var statsGroup: StatsGroup? = null
    private var scrollView: ScrollView? = null

    private val resetItem: MenuItem
    private val resetDialog: AlertDialog


    init {
        itemClickListener = null

        @GDXAssets(propertiesFiles = ["assets/core/strings.properties"])
        val strings: I18NBundle = skin[CoreRes.CORE_STRINGS_NAME]

        title = strings["menu_stats"]

        // Reset item
        resetItem = MenuItem(ITEM_ID_RESET, strings["stats_reset"],
                skin.getDrawable(CoreIcons.REFRESH), ITEM_POS_BOTTOM)
        resetItem.checkable = false

        // Reset confirm dialog
        resetDialog = AlertDialog(skin).apply {
            title = strings["stats_reset_confirm_title"]
            message = strings["stats_reset_confirm_message"]
            shadowType = Dialog.ShadowType.DISMISSABLE
            addButton(strings["action_no"]).onClick {
                // Do nothing
                dismiss()
            }
            addButton(strings["action_yes"]).onClick {
                // Reset and save stats
                stats?.reset()
                stats?.save()
                refresh()
                dismiss()
            }
        }

        doMenuLayout()
    }

    /**
     * Refresh the stats group values.
     */
    fun refresh() {
        statsGroup?.refresh()
    }


    override fun checkItem(item: MenuItem) {
        super.checkItem(item)

        // Show the item corresponding variant.
        val variants = stats?.variants ?: return
        if (item.id in variants.indices) {
            statsGroup?.shownVariant = item.id
        }
    }


    companion object {
        private const val ITEM_ID_RESET = 1000
    }

}
