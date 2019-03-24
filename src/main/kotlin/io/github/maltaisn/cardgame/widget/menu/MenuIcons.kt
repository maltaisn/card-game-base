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

package io.github.maltaisn.cardgame.widget.menu

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.Drawable


class MenuIcons(skin: Skin) {

    /** The menu icons map. More icons can be added. */
    val icons = mutableMapOf<String, Drawable>()

    init {
        for (name in ICON_NAMES) {
            icons[name] = skin.getDrawable(name)
        }
    }

    operator fun get(name: String) = icons[name]!!

    companion object {
        // Names of default icons
        const val CARDS = "icon-cards"
        const val CHEVRON_LEFT = "icon-chevron-left"
        const val ARROW_RIGHT = "icon-arrow-right"
        const val BOOK = "icon-book"
        const val SETTINGS = "icon-settings"
        const val LIST = "icon-list"
        const val INFO = "icon-info"

        private val ICON_NAMES = arrayOf(CARDS, CHEVRON_LEFT, ARROW_RIGHT, BOOK, SETTINGS, LIST, INFO)
    }

}