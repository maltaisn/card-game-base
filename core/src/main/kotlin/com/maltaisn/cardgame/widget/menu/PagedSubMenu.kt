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

package com.maltaisn.cardgame.widget.menu

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.maltaisn.cardgame.utils.findScrollFocus
import com.maltaisn.cardgame.widget.menu.PagedSubMenu.Page


/**
 * A sub menu with different pages, where items change pages.
 * Pages can be added with [addItem] where the item is a [Page].
 */
class PagedSubMenu(skin: Skin) : SubMenu(skin) {

    private var selectedPage: Page? = null


    init {
        doMenuLayout()
    }


    override fun checkItem(item: MenuItem) {
        super.checkItem(item)

        // Unselect last page and select new page
        if (item is Page && selectedPage !== item) {
            selectedPage?.onPageSelectionChanged(false)
            selectedPage = item
            item.onPageSelectionChanged(true)

            // Change content
            content.clearChildren()
            content.add(item.content).grow()
            content.findScrollFocus()
        }
    }

    override fun clearItems() {
        super.clearItems()
        content.clearChildren()
    }

    override fun removeItem(item: MenuItem) {
        super.removeItem(item)
        if (item.checked) {
            content.clearChildren()
        }
    }

    open class Page(id: Int, title: String, icon: Drawable?, position: Int,
                    var content: Actor? = null, important: Boolean = false) :
            MenuItem(id, title, icon, position, important) {

        /**
         * Called when a page is selected or unselected.
         */
        open fun onPageSelectionChanged(selected: Boolean) = Unit

    }

}
