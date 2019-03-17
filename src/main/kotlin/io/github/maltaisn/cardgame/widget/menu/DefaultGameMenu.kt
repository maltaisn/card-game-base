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
import com.badlogic.gdx.utils.I18NBundle
import com.gmail.blueboxware.libgdxplugin.annotations.GDXAssets
import io.github.maltaisn.cardgame.Resources
import ktx.style.get


class DefaultGameMenu(skin: Skin) : GameMenu(skin) {

    /** Listener called when the continue item is clicked in the main menu. */
    var continueListener: (() -> Unit)? = null

    /** The continue menu item. Can be disabled. */
    val continueItem: MenuItem

    private val newGameMenu = SubMenu(skin)
    private val settingsMenu = SubMenu(skin)
    private val rulesMenu = SubMenu(skin)
    private val statsMenu = SubMenu(skin)
    private val aboutMenu = SubMenu(skin)

    init {
        @GDXAssets(propertiesFiles = ["assets/core/strings.properties"])
        val bundle: I18NBundle = skin[Resources.CORE_STRINGS_NAME]
        val icons = skin.get(MenuIcons::class.java)

        val newGameStr = bundle.get("mainmenu_new_game")
        val continueStr = bundle.get("mainmenu_continue")
        val settingsStr = bundle.get("mainmenu_settings")
        val rulesStr = bundle.get("mainmenu_rules")
        val statsStr = bundle.get("mainmenu_stats")
        val aboutStr = bundle.get("mainmenu_about")

        mainMenu.apply {
            // Add main menu items
            continueItem = MenuItem(ITEM_ID_CONTINUE, continueStr, icons.arrowRight, MenuItem.Position.BOTTOM)
            items += MenuItem(ITEM_ID_NEW_GAME, newGameStr, icons.cards, MenuItem.Position.BOTTOM)
            items += continueItem
            items += MenuItem(ITEM_ID_SETTINGS, settingsStr, icons.settings, MenuItem.Position.BOTTOM)
            items += MenuItem(ITEM_ID_RULES, rulesStr, icons.book, MenuItem.Position.TOP)
            items += MenuItem(ITEM_ID_STATS, statsStr, icons.list, MenuItem.Position.TOP)
            items += MenuItem(ITEM_ID_ABOUT, aboutStr, icons.info, MenuItem.Position.TOP)
            invalidateLayout()

            itemClickListener = {
                when (it.id) {
                    ITEM_ID_CONTINUE -> continueListener?.invoke()
                    ITEM_ID_NEW_GAME -> openSubMenu(newGameMenu)
                    ITEM_ID_SETTINGS -> openSubMenu(settingsMenu)
                    ITEM_ID_RULES -> openSubMenu(rulesMenu)
                    ITEM_ID_STATS -> openSubMenu(statsMenu)
                    ITEM_ID_ABOUT -> openSubMenu(aboutMenu)
                }
            }
        }

        newGameMenu.title = newGameStr
        settingsMenu.title = settingsStr
        rulesMenu.title = rulesStr
        statsMenu.title = statsStr
        aboutMenu.title = aboutStr

        // TEMP SUBMENU ITEMS
        newGameMenu.apply {
            checkable = false
            menuPosition = SubMenu.MenuPosition.RIGHT
            items += MenuItem(0, "Start Game", icons.cards, MenuItem.Position.BOTTOM)
            itemClickListener = {
                if (it.id == 0) {
                    this@DefaultGameMenu.shown = false
                }
            }
            invalidateLayout()
        }

        settingsMenu.apply {
            items += MenuItem(0, "Game", icons.cards)
            items += MenuItem(1, "Interface", icons.book)
            items += MenuItem(2, "Contracts", icons.list)
            invalidateLayout()
        }

        rulesMenu.apply {
            items += MenuItem(0, "Dealing", icons.cards)
            items += MenuItem(1, "Scoring", icons.book)
            items += MenuItem(2, "Contracts", icons.list)
            invalidateLayout()

        }

        statsMenu.invalidateLayout()

        aboutMenu.apply {
            items += MenuItem(0, "About", icons.info)
            items += MenuItem(1, "Donate", icons.arrowRight)
            invalidateLayout()
        }
    }

    companion object {
        private const val ITEM_ID_NEW_GAME = 0
        private const val ITEM_ID_CONTINUE = 1
        private const val ITEM_ID_SETTINGS = 2
        private const val ITEM_ID_RULES = 3
        private const val ITEM_ID_STATS = 4
        private const val ITEM_ID_ABOUT = 5
    }

}