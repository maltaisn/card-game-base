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

package io.github.maltaisn.cardgame.widget

import com.badlogic.gdx.scenes.scene2d.utils.Drawable


/**
 * An item in a menu, with a [title], an [icon] and a [position] in the menu.
 * Each item should have an unique ID to identify them in click listener.
 */
class MenuItem(val id: Int,
               val title: CharSequence,
               val icon: Drawable,
               val position: Position = Position.TOP) {

    /** The menu this is in. */
    var menu: MenuTable? = null
        internal set

    /** The button this item is attached to. */
    var button: MenuButton? = null
        internal set

    /** Whether this item is checked or not. */
    var checked: Boolean
        set(value) {
            button?.checked = value && checkable
        }
        get() = button?.checked == true

    /** If the [menu] is checkable, whether this item can be checked. */
    var checkable = true

    /** Whether this item is enabled or not. */
    var enabled: Boolean
        set(value) {
            button?.enabled = value
        }
        get() = button?.enabled == true


    override fun toString() = "[id: $id, title: $title" +
            (if (checked) ", checked" else "") +
            (if (!enabled) ", disabled" else "")


    enum class Position {
        TOP, BOTTOM
    }

}