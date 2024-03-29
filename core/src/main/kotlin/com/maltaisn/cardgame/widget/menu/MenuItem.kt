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

import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.maltaisn.cardgame.widget.menu.MenuItem.Companion.NO_ID


/**
 * An item in a menu, with an [id], a [title], an [icon] and a [position] in the menu.
 * Each item should have an unique ID to identify them in click listener, different than [NO_ID].
 * Menu implementations should have item position constants.
 */
open class MenuItem(val id: Int,
                    val title: CharSequence?,
                    val icon: Drawable?,
                    val position: Int,
                    val important: Boolean = false) {

    init {
        require(id != NO_ID) { "A menu item cannot have an ID of $NO_ID." }
    }

    /**
     * The menu this is in, or `null` if not added yet.
     */
    var menu: MenuTable? = null
        internal set

    /**
     * The button this item is attached to.
     */
    internal var button: MenuButton? = null

    /**
     * Whether this item is shown or hidden.
     */
    var shown = true
        set(value) {
            field = value
            menu?.invalidateMenuLayout()
        }

    /** Whether this item is checked or not. */
    var checked = false
        internal set(value) {
            field = value
            button?.checked = value
        }

    /**
     * If the [menu] is checkable, whether this item can be checked.
     */
    var checkable = true

    /**
     * Whether this item is enabled or not.
     */
    var enabled = true
        set(value) {
            field = value
            button?.enabled = value
        }


    override fun toString() = "[id: $id, title: $title" +
            (if (checked) ", checked" else "") +
            (if (!enabled) ", disabled" else "") + "]"



    companion object {
        const val NO_ID = -1
    }

}
