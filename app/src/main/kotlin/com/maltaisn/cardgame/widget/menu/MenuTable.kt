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
import com.maltaisn.cardgame.widget.FboTable
import com.maltaisn.cardgame.widget.FontStyle
import com.maltaisn.cardgame.widget.TimeAction


/**
 * The base class for a menu widget that can be shown and hidden with an animation.
 * Do update the layout after items are changed, call [invalidateLayout].
 */
abstract class MenuTable(skin: Skin) : FboTable(skin) {

    val items = mutableListOf<MenuItem>()

    /** Whether the items in the menu can be checked or not. */
    var checkable = false

    /**
     * Changing this value animates a visibility change by sliding the menu parts in and out of the screen.
     * If changed during an outgoing transition, the previous one will be inverted.
     */
    open var shown = false

    internal open var transitionAction: TimeAction? = null
        set(value) {
            if (field != null) removeAction(field)
            field = value
            if (value != null) addAction(value)
        }

    /** The listener called when a menu item is clicked, `null` for none. */
    var itemClickListener: ((item: MenuItem) -> Unit)? = null

    protected val btnClickListener = { btn: MenuButton ->
        for (item in items) {
            if (item.button === btn) {
                item.checked = checkable
                itemClickListener?.invoke(item)
            } else {
                item.checked = false
            }
        }
    }

    init {
        isVisible = false
    }

    override fun clearActions() {
        super.clearActions()
        transitionAction?.end()
    }

    /**
     * Invalidate the menu layout, must be called if items are changed.
     */
    abstract fun invalidateLayout()


    /** Check an item by [id] and call the listener. */
    fun checkItem(id: Int, callListener: Boolean = true) {
        for (item in items) {
            if (item.id == id) {
                checkItem(item, callListener)
                break
            }
        }
    }

    /** Check an [item] and call the listener. */
    fun checkItem(item: MenuItem, callListener: Boolean = true) {
        if (checkable && item.menu === this) {
            for (menuItem in items) {
                if (menuItem === item) {
                    if (!menuItem.checked && menuItem.checkable) {
                        menuItem.checked = true
                        if (callListener) {
                            itemClickListener?.invoke(menuItem)
                        }
                    }
                } else {
                    menuItem.checked = false
                }
            }
        }
    }


    abstract class MenuTableStyle {
        lateinit var itemFontStyle: FontStyle
        var itemIconSize = 0f
    }

}