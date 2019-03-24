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

import com.badlogic.gdx.scenes.scene2d.Action
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Container
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.I18NBundle
import com.gmail.blueboxware.libgdxplugin.annotations.GDXAssets
import io.github.maltaisn.cardgame.Resources
import io.github.maltaisn.cardgame.widget.prefs.GamePrefs
import io.github.maltaisn.cardgame.widget.prefs.PrefCategory
import ktx.actors.plusAssign
import ktx.style.get


/**
 * The default implementation of the [GameMenu], with 6 items in main menu:
 * new game, continue, settings, rules, statistics and about.
 */
class DefaultGameMenu(private val skin: Skin) : GameMenu(skin) {

    /** Listener called when the continue item is clicked in the main menu. */
    var continueListener: (() -> Unit)? = null

    /** The continue menu item. Can be disabled. */
    val continueItem: MenuItem

    /** The game preferences, shown in the setting submenu. */
    var settings: GamePrefs? = null
        set(value) {
            field = value
            settingsMenu.items.clear()
            if (value != null) {
                settingsView = value.createView(skin)
                settingsMenu.content.actor = settingsView

                // Add menu items from settings categories
                var id = 0
                for (entry in value.entries) {
                    if (entry is PrefCategory) {
                        settingsMenu.items += MenuItem(id, entry.title,
                                menuIcons[entry.icon ?: MenuIcons.CARDS])
                        id++
                    }
                }
            } else {
                settingsView = null
                settingsMenu.content.actor = null
            }
            settingsMenu.invalidateLayout()
        }

    private var settingsView: Table? = null

    private val newGameMenu = SubMenu(skin)
    private val settingsMenu = SubMenu(skin)
    private val rulesMenu = SubMenu(skin)
    private val statsMenu = SubMenu(skin)
    private val aboutMenu = SubMenu(skin)

    private val menuIcons = skin[MenuIcons::class.java]

    init {
        @GDXAssets(propertiesFiles = ["assets/core/strings.properties"])
        val bundle: I18NBundle = skin[Resources.CORE_STRINGS_NAME]

        val newGameStr = bundle.get("mainmenu_new_game")
        val continueStr = bundle.get("mainmenu_continue")
        val settingsStr = bundle.get("mainmenu_settings")
        val rulesStr = bundle.get("mainmenu_rules")
        val statsStr = bundle.get("mainmenu_stats")
        val aboutStr = bundle.get("mainmenu_about")

        mainMenu.apply {
            // Add main menu items
            continueItem = MenuItem(ITEM_ID_CONTINUE, continueStr, menuIcons[MenuIcons.ARROW_RIGHT], MenuItem.Position.BOTTOM)
            items += MenuItem(ITEM_ID_NEW_GAME, newGameStr, menuIcons[MenuIcons.CARDS], MenuItem.Position.BOTTOM)
            items += continueItem
            items += MenuItem(ITEM_ID_SETTINGS, settingsStr, menuIcons[MenuIcons.SETTINGS], MenuItem.Position.BOTTOM)
            items += MenuItem(ITEM_ID_RULES, rulesStr, menuIcons[MenuIcons.BOOK], MenuItem.Position.TOP)
            items += MenuItem(ITEM_ID_STATS, statsStr, menuIcons[MenuIcons.LIST], MenuItem.Position.TOP)
            items += MenuItem(ITEM_ID_ABOUT, aboutStr, menuIcons[MenuIcons.INFO], MenuItem.Position.TOP)
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

        // New game menu
        newGameMenu.title = newGameStr
        newGameMenu.apply {
            checkable = false
            menuPosition = SubMenu.MenuPosition.RIGHT
            items += MenuItem(0, "Start Game", menuIcons[MenuIcons.CARDS], MenuItem.Position.BOTTOM)
            itemClickListener = {
                if (it.id == 0) {
                    this@DefaultGameMenu.shown = false
                }
            }
            invalidateLayout()
        }

        // Settings menu
        settingsMenu.title = settingsStr
        settingsMenu.backArrowClickListener = {
            settings?.save()
            closeSubMenu()
        }
        settingsMenu.itemClickListener = {
            // When a settings menu item is clicked, scroll the content pane to the category header.
            // Category headers are the only Container children and are recognized this way.
            var id = 0
            val scrollPane = settingsMenu.contentPane
            for (child in settingsView!!.children) {
                if (child is Container<*>) {
                    if (id == it.id) {
                        val top = child.y + child.height + 20f
                        val height = scrollPane.height
                        scrollPane.scrollTo(0f, top - scrollPane.height, 0f, height)
                        break
                    }
                    id++
                }
            }
        }
        settingsMenu.contentPane += object : Action() {
            private var lastScrollY = 0f

            override fun act(delta: Float): Boolean {
                // There's no scroll change listener, so this action constantly check if the scroll pane has scrolled.
                val scrollPane = settingsMenu.contentPane
                val scrollY = scrollPane.scrollY
                if (scrollY != lastScrollY) {
                    lastScrollY = scrollY

                    // Change the checked menu item if needed
                    var newId = MenuItem.NO_ID
                    val oldId = settingsMenu.checkedItem?.id ?: MenuItem.NO_ID
                    when {
                        scrollPane.isTopEdge -> {
                            // Scrolled to top edge, always check first category
                            newId = 0
                        }
                        scrollPane.isBottomEdge -> {
                            // Scrolled to bottom edge, always check last category
                            newId = settingsMenu.items.size - 1
                        }
                        else -> {
                            // Find the top and bottom limits of the currently checked category
                            var id = 0
                            var currCatg: Actor? = null
                            var nextCatg: Actor? = null
                            val children = settingsView!!.children
                            for (child in children) {
                                if (child is Container<*>) {
                                    if (currCatg != null) {
                                        nextCatg = child
                                    } else if (id == oldId) {
                                        currCatg = child
                                    }
                                    id++
                                }
                            }
                            if (nextCatg == null) nextCatg = children.last()
                            val top = currCatg!!.y + currCatg.height
                            val bottom = nextCatg!!.y + nextCatg.height

                            // If currently checked category is completely hidden, check another
                            val maxY = settingsView!!.height - scrollY
                            val minY = maxY - scrollPane.height
                            val tooHigh = top > maxY && bottom > maxY
                            val tooLow = top < minY && bottom < minY
                            if (tooHigh || tooLow) {
                                // If too high check next category, otherwise check previous.
                                newId = (oldId + if (tooHigh) 1 else -1).coerceIn(settingsMenu.items.indices)
                            }
                        }
                    }
                    if (newId != -1 && newId != oldId) {
                        settingsMenu.checkItem(newId, false)
                    }
                }
                return false
            }
        }

        // Rules menu
        rulesMenu.title = rulesStr
        rulesMenu.apply {
            items += MenuItem(0, "Dealing", menuIcons[MenuIcons.CARDS])
            items += MenuItem(1, "Scoring", menuIcons[MenuIcons.BOOK])
            items += MenuItem(2, "Contracts", menuIcons[MenuIcons.LIST])
            invalidateLayout()
        }

        // Statistics menu
        statsMenu.title = statsStr
        statsMenu.invalidateLayout()

        // About menu
        aboutMenu.title = aboutStr
        aboutMenu.apply {
            items += MenuItem(0, "About", menuIcons[MenuIcons.INFO])
            items += MenuItem(1, "Donate", menuIcons[MenuIcons.ARROW_RIGHT])
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