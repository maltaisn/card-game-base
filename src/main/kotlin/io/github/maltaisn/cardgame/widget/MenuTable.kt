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

import com.badlogic.gdx.scenes.scene2d.ui.Skin


abstract class MenuTable(skin: Skin) : FboTable(skin) {

    val items = mutableListOf<MenuItem>()

    /** Whether the items in the menu can be checked or not. */
    protected var checkable = false

    /**
     * Changing this value animates a visibility change by sliding the menu parts in and out of the screen.
     * If changed during an outgoing transition, the previous one will be canceled.
     */
    abstract var shown: Boolean

    /** The listener called when a menu item is clicked, `null` for none. */
    var itemClickListener: ((item: MenuItem) -> Unit)? = null

    protected val btnClickListener = { btn: MenuButton ->
        if (checkable) {
            for (item in items) {
                if (item.button === btn) {
                    item.checked = true
                    itemClickListener?.invoke(item)
                } else {
                    item.checked = false
                }
            }
        }
    }

    /**
     * Invalidate the menu layout, must be called if items are changed.
     */
    abstract fun invalidateLayout()


    abstract class MenuTableStyle {
        lateinit var itemFontStyle: SdfLabel.SdfLabelStyle
        var itemIconSize = 0f
    }

}