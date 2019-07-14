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

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.maltaisn.cardgame.post
import com.maltaisn.cardgame.widget.action.ActionDelegate
import com.maltaisn.cardgame.widget.FboTable
import com.maltaisn.cardgame.widget.FontStyle
import com.maltaisn.cardgame.widget.action.TimeAction


/**
 * The base class for a menu widget that can be shown and hidden with an animation.
 * Do update the layout after items are changed, call [invalidateMenuLayout].
 */
abstract class MenuTable(skin: Skin) : FboTable(skin) {

    /**
     * The items in the menu.
     */
    val items: List<MenuItem>
        get() = _items

    private val _items = mutableListOf<MenuItem>()

    /** Whether the items in the menu can be checked or not. */
    var checkable = false

    /**
     * Changing this value animates a visibility change by sliding the menu parts in and out of the screen.
     * If changed during an outgoing transition, the previous one will be inverted.
     */
    open var shown = false

    internal open var transitionAction by ActionDelegate<TimeAction>()

    /** The listener called when a menu item is clicked, `null` for none. */
    open var itemClickListener: ((item: MenuItem) -> Unit)? = null

    protected val btnClickListener = { btn: MenuButton ->
        // Check the new item if needed, call listener
        lateinit var clickedItem: MenuItem
        for (item in items) {
            if (item.button === btn) {
                item.checked = true
                itemClickListener?.invoke(item)
                clickedItem = item
                break
            }
        }

        // If new item was checked, uncheck the last one
        if (clickedItem.checked) {
            for (item in items) {
                if (item !== clickedItem) {
                    item.checked = false
                }
            }
        }
    }

    private var invalidLayout = false


    init {
        isVisible = false
    }


    override fun clearActions() {
        super.clearActions()
        transitionAction?.end()
    }

    /** Add an [item] to the menu. */
    open fun addItem(item: MenuItem) {
        item.menu = this
        _items += item
        invalidateMenuLayout()
    }

    /** Remove an [item] in the menu. */
    open fun removeItem(item: MenuItem) {
        if (_items.remove(item)) {
            item.menu = null
            invalidateMenuLayout()
        }
    }

    /** Remove all items in the menu. */
    open fun clearItems() {
        _items.clear()
        invalidateMenuLayout()
    }

    /** Invalidate the menu layout, must be called if items are changed. */
    internal fun invalidateMenuLayout() {
        if (!invalidLayout) {
            invalidLayout = true
            post {
                doMenuLayout()
                invalidLayout = false
            }
        }
    }

    /** Do the menu layout. */
    protected abstract fun doMenuLayout()


    abstract class MenuTableStyle {
        lateinit var itemFontStyle: FontStyle
        var itemIconSize = 0f
    }

}
