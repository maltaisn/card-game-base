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

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Container
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Value
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.I18NBundle
import com.gmail.blueboxware.libgdxplugin.annotations.GDXAssets
import io.github.maltaisn.cardgame.Resources
import io.github.maltaisn.cardgame.prefs.GamePref
import io.github.maltaisn.cardgame.prefs.GamePrefs
import io.github.maltaisn.cardgame.prefs.PrefCategory
import io.github.maltaisn.cardgame.widget.SdfLabel
import io.github.maltaisn.cardgame.widget.prefs.PrefCategoryView
import io.github.maltaisn.cardgame.widget.prefs.PrefsGroup
import ktx.style.get


/**
 * The default implementation of the [GameMenu], with 6 items in main menu:
 * new game, continue, settings, rules, statistics and about.
 */
class DefaultGameMenu(private val skin: Skin) : GameMenu(skin) {

    /** Listener called when the continue item is clicked in the main menu. */
    var continueListener: (() -> Unit)? = null

    /**
     * Listener called when the start game item is clicked in the new game submenu.
     * When the item is clicked, the options are automatically saved and the game menu is closed.
     */
    var startGameListener: (() -> Unit)? = null

    /** The continue menu item. Can be disabled. */
    val continueItem: MenuItem

    /** The game preferences, shown in the setting submenu. */
    var settings: GamePrefs? = null
        set(value) {
            field = value
            settingsMenu.items.clear()
            if (value != null) {
                settingsView = PrefsGroup(skin, value)
                settingsView?.helpListener = prefsHelpListener
                settingsMenu.content.actor = settingsView

                // Add menu items from settings categories
                var id = 0
                for (entry in value.entries) {
                    if (entry is PrefCategory) {
                        settingsMenu.items += MenuItem(id, entry.title,
                                skin.getDrawable(entry.icon ?: MenuIcons.CARDS))
                        id++
                    }
                }
            } else {
                settingsView = null
                settingsMenu.content.actor = null
            }
            settingsMenu.invalidateLayout()
        }

    /** The new game game options, shown in the new game submenu. */
    var newGameOptions: GamePrefs? = null
        set(value) {
            field = value
            if (value != null) {
                newGameView = PrefsGroup(skin, value)
                newGameView?.helpListener = prefsHelpListener
                newGameMenu.content.actor = newGameView
            } else {
                newGameView = null
                newGameMenu.content.actor = null
            }
            newGameMenu.invalidateLayout()
        }

    private val style = skin[DefaultGameMenuStyle::class.java]

    private val newGameMenu = SubMenu(skin)
    private val settingsMenu = SubMenu(skin)
    private val rulesMenu = SubMenu(skin)
    private val statsMenu = SubMenu(skin)
    private val aboutMenu = SubMenu(skin)

    private var newGameView: PrefsGroup? = null
    private var settingsView: PrefsGroup? = null

    private val prefsHelpLabel = SdfLabel(null, skin, style.settingsHelpFontStyle)
    private val prefsHelpContent = Container<Actor>(prefsHelpLabel)
    private val prefsHelpListener: (GamePref) -> Unit = { pref ->
        // When a help icon is clicked, show drawer with help text
        drawer.content = prefsHelpContent
        drawer.drawerWidth = Value.percentWidth(0.5f, drawer)
        drawer.title = pref.helpTitle
        prefsHelpLabel.setText(pref.help)
        drawer.shown = true
    }

    init {
        @GDXAssets(propertiesFiles = ["assets/core/strings.properties"])
        val bundle: I18NBundle = skin[Resources.CORE_STRINGS_NAME]

        drawer.backBtnText = bundle.get("menu_drawer_back")

        val newGameStr = bundle.get("menu_new_game")
        val continueStr = bundle.get("menu_continue")
        val settingsStr = bundle.get("menu_settings")
        val rulesStr = bundle.get("menu_rules")
        val statsStr = bundle.get("menu_stats")
        val aboutStr = bundle.get("menu_about")

        mainMenu.apply {
            // Add main menu items
            val icons = this@DefaultGameMenu.style
            items += MenuItem(ITEM_ID_NEW_GAME, newGameStr, icons.newGameIcon, MenuItem.Position.BOTTOM)
            items += MenuItem(ITEM_ID_CONTINUE, continueStr, icons.continueIcon, MenuItem.Position.BOTTOM)
            items += MenuItem(ITEM_ID_SETTINGS, settingsStr, icons.settingsIcon, MenuItem.Position.BOTTOM)
            items += MenuItem(ITEM_ID_RULES, rulesStr, icons.rulesIcon, MenuItem.Position.TOP)
            items += MenuItem(ITEM_ID_STATS, statsStr, icons.statsIcon, MenuItem.Position.TOP)
            items += MenuItem(ITEM_ID_ABOUT, aboutStr, icons.aboutIcon, MenuItem.Position.TOP)
            invalidateLayout()

            continueItem = items[1]

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

        // Preference help widgets
        prefsHelpContent.fill().pad(0f, 30f, 0f, 30f)
        prefsHelpLabel.setWrap(true)
        prefsHelpLabel.setAlignment(Align.topLeft)

        // New game menu
        newGameMenu.apply {
            title = newGameStr
            checkable = false
            menuPosition = SubMenu.MenuPosition.RIGHT
            items += MenuItem(0, bundle.get("menu_start_game"),
                    this@DefaultGameMenu.style.startGameIcon, MenuItem.Position.BOTTOM)
            itemClickListener = {
                newGameOptions?.save()
                this@DefaultGameMenu.shown = false
                startGameListener?.invoke()
            }
            backArrowClickListener = {
                newGameOptions?.save()
                closeSubMenu()
            }
            invalidateLayout()
        }

        // Settings menu
        settingsMenu.apply {
            title = settingsStr
            backArrowClickListener = {
                settings?.save()
                closeSubMenu()
            }
            itemClickListener = {
                // When a settings menu item is clicked, scroll the content pane to the category header.
                var id = 0
                val scrollPane = settingsMenu.contentPane
                for (child in settingsView!!.children) {
                    if (child is PrefCategoryView) {
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
            contentPane.scrollListener = { _, _, y, _, _ ->
                // Change the checked menu item if needed
                var newId = MenuItem.NO_ID
                val oldId = settingsMenu.checkedItem?.id ?: MenuItem.NO_ID
                when {
                    contentPane.isTopEdge -> {
                        // Scrolled to top edge, always check first category
                        newId = 0
                    }
                    contentPane.isBottomEdge -> {
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
                            if (child is PrefCategoryView) {
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
                        val maxY = settingsView!!.height - y
                        val minY = maxY - contentPane.height
                        val tooHigh = top > maxY && bottom > maxY
                        val tooLow = top < minY && bottom < minY
                        if (tooHigh || tooLow) {
                            // If too high check next category, otherwise check previous.
                            newId = (oldId + if (tooHigh) 1 else -1).coerceIn(0, settingsMenu.items.size - 1)
                        }
                    }
                }
                if (newId != -1 && newId != oldId) {
                    settingsMenu.checkItem(newId, false)
                }
            }
        }

        // Rules menu
        rulesMenu.apply {
            title = rulesStr
            items += MenuItem(0, "Dealing", skin.getDrawable(MenuIcons.CARDS))
            items += MenuItem(1, "Scoring", skin.getDrawable(MenuIcons.BOOK))
            items += MenuItem(2, "Contracts", skin.getDrawable(MenuIcons.LIST))
            invalidateLayout()
        }

        // Statistics menu
        statsMenu.apply {
            title = statsStr
            invalidateLayout()
        }

        // About menu
        aboutMenu.apply {
            title = aboutStr
            items += MenuItem(0, "About", skin.getDrawable(MenuIcons.INFO))
            items += MenuItem(1, "Donate", skin.getDrawable(MenuIcons.ARROW_RIGHT))
            invalidateLayout()
        }
    }

    class DefaultGameMenuStyle {
        lateinit var settingsHelpFontStyle: SdfLabel.FontStyle
        lateinit var newGameIcon: Drawable
        lateinit var continueIcon: Drawable
        lateinit var settingsIcon: Drawable
        lateinit var rulesIcon: Drawable
        lateinit var statsIcon: Drawable
        lateinit var aboutIcon: Drawable
        lateinit var startGameIcon: Drawable
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