/*
 * Copyright 2019 Nicolas Maltais
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.maltaisn.cardgame.widget.menu

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup
import ktx.actors.onClick
import ktx.assets.pool


class MenuDrawerList(skin: Skin) : VerticalGroup() {

    /** The list of text items in the list. */
    var items = emptyList<String>()
        set(value) {
            field = value

            // Clear and free previous items
            selectedIndex = -1
            for (actor in itemActors) {
                itemActorPool.free(actor)
            }
            itemActors.clear()
            clearChildren()

            // Add new items
            for ((i, item) in value.withIndex()) {
                val actor = itemActorPool.obtain()
                actor.text = item
                actor.onClick { selectedIndex = i }
                itemActors += actor
                addActor(actor)
            }
        }

    /** The currently selected index in the list of [items], or `-1` for none. */
    var selectedIndex = -1
        set(value) {
            if (field != value) {
                if (field != -1) itemActors[field].checked = false
                if (value != -1) itemActors[value].checked = true
                field = value
                selectionChangeListener?.invoke(value)
            }
        }

    /** The listener called when the selected item is changed. */
    var selectionChangeListener: ((selectedIndex: Int) -> Unit)? = null

    private val itemActors = mutableListOf<MenuDrawerListItem>()
    private val itemActorPool = pool { MenuDrawerListItem(skin) }

    init {
        grow().space(5f)
    }

}