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
import com.maltaisn.cardgame.utils.post
import com.maltaisn.cardgame.widget.FboTable
import com.maltaisn.cardgame.widget.action.ActionDelegate
import com.maltaisn.cardgame.widget.action.TimeAction
import com.maltaisn.cardgame.widget.text.FontStyle


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

    /**
     * Changing this value animates a visibility change by sliding the menu parts in and out of the screen.
     * If changed during an outgoing transition, the previous one will be inverted.
     */
    open var shown = false

    /** Whether the items in the menu can be checked or not. */
    var checkable = false

    /** Returns the first checked item in the menu, `null` if none are checked */
    val checkedItem: MenuItem?
        get() {
            for (item in items) {
                if (item.checked) {
                    return item
                }
            }
            return null
        }

    /** The listener called when a menu item is clicked or checked, `null` for none. */
    open var itemClickListener: ((MenuItem) -> Unit)? = null

    internal var transitionAction by ActionDelegate<TimeAction>()

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

    /** Add [items] to the menu. */
    fun addItems(vararg items: MenuItem) {
        for (item in items) {
            addItem(item)
        }
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

    /**
     * Check an [item] in the menu. The menu and the item must be checkable.
     * The last checked item is unchecked and the item click listener is called for the new item.
     */
    open fun checkItem(item: MenuItem) {
        require(item.menu === this) { "Item doesn't belong to this menu." }
        require(checkable && item.checkable) { "Menu or item isn't checkable." }

        // Uncheck last checked item
        checkedItem?.checked = false

        // Check new item
        item.checked = true
        itemClickListener?.invoke(item)
    }

    protected fun onItemBtnClicked(item: MenuItem) {
        if (checkable && item.checkable) {
            checkItem(item)
        } else {
            itemClickListener?.invoke(item)
        }
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
