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
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.utils.I18NBundle
import com.gmail.blueboxware.libgdxplugin.annotations.GDXAssets
import com.maltaisn.cardgame.CoreRes
import com.maltaisn.cardgame.markdown.Markdown
import com.maltaisn.cardgame.markdown.MdElement
import com.maltaisn.cardgame.prefs.GamePrefs
import com.maltaisn.cardgame.prefs.PrefCategory
import com.maltaisn.cardgame.stats.Statistics
import com.maltaisn.cardgame.widget.card.CardActor
import com.maltaisn.cardgame.widget.markdown.MarkdownView
import com.maltaisn.cardgame.widget.prefs.ConfirmCallback
import com.maltaisn.cardgame.widget.prefs.PrefsGroup
import com.maltaisn.cardgame.widget.stats.StatsSubMenu
import ktx.style.get


/**
 * The default implementation of the [GameMenu], with 6 items in main menu:
 * new game, continue, settings, rules, statistics and about.
 * The in game menu has a back button and a scoreboard button.
 */
open class DefaultGameMenu(private val skin: Skin,
                           cardStyle: CardActor.CardStyle) : GameMenu(skin) {

    private val style: DefaultGameMenuStyle = skin.get()


    /** The callback interface called when an event happens in the menu. */
    var callback: Callback? = null

    /** See [PrefsGroup.confirmCallback] */
    var confirmCallback: ConfirmCallback? = null
        set(value) {
            field = value
            settingsView?.confirmCallback = value
            newGameView?.confirmCallback = value
        }

    /** The continue menu item. Can be disabled. */
    val continueItem: MenuItem

    /** The game preferences, shown in the setting submenu. */
    var settings: GamePrefs? = null
        set(value) {
            field = value
            settingsMenu.clearItems()
            settingsView = if (value != null) {
                createPreferenceView(settingsMenu, value)
            } else {
                null
            }
            settingsMenu.scrollContent.actor = settingsView
        }

    /** The new game game options, shown in the new game submenu. */
    var newGameOptions: GamePrefs? = null
        set(value) {
            field = value
            newGameMenu.clearItems()
            newGameMenu.addItem(startGameItem)
            newGameView = if (value != null) {
                createPreferenceView(newGameMenu, value)
            } else {
                null
            }
            newGameMenu.scrollContent.actor = newGameView
        }

    private val startGameItem: MenuItem

    /** The game rules in markdown. */
    var rules: Markdown? = null
        set(value) {
            field = value
            rulesMenu.clearItems()
            rulesView = if (value != null) {
                createMarkdownView(rulesMenu, value)
            } else {
                null
            }
            rulesMenu.scrollContent.actor = rulesView
        }

    /** The game statistics. */
    var stats: Statistics? = null
        set(value) {
            field = value
            statsMenu.stats = value
        }

    /** The main menu. */
    val mainMenu = MainMenu(skin, cardStyle)

    /** The menu shown in game. */
    val inGameMenu = InGameMenu(skin)

    /** The menu used as the scoreboard. */
    val scoreboardMenu = PagedSubMenu(skin)

    // The submenus
    val newGameMenu = ScrollSubMenu(skin)
    private var newGameView: PrefsGroup? = null

    val settingsMenu = ScrollSubMenu(skin)
    private var settingsView: PrefsGroup? = null

    val rulesMenu = ScrollSubMenu(skin)
    private var rulesView: Table? = null

    val statsMenu = StatsSubMenu(skin)

    val aboutMenu = PagedSubMenu(skin)


    init {
        @GDXAssets(propertiesFiles = ["assets/core/strings.properties"])
        val strings: I18NBundle = skin[CoreRes.CORE_STRINGS_NAME]

        drawer.backBtnText = strings["menu_drawer_back"]

        val newGameStr = strings["menu_new_game"]
        val continueStr = strings["menu_continue"]
        val settingsStr = strings["menu_settings"]
        val rulesStr = strings["menu_rules"]
        val statsStr = strings["menu_stats"]
        val aboutStr = strings["menu_about"]

        mainMenu.apply {
            // Add main menu items
            val icons = this@DefaultGameMenu.style
            addItems(MenuItem(ITEM_ID_RULES, rulesStr, icons.rulesIcon, MainMenu.ITEM_POS_LEFT),
                    MenuItem(ITEM_ID_SETTINGS, settingsStr, icons.settingsIcon, MainMenu.ITEM_POS_LEFT),
                    MenuItem(ITEM_ID_NEW_GAME, newGameStr, icons.newGameIcon, MainMenu.ITEM_POS_LEFT, true),
                    MenuItem(ITEM_ID_ABOUT, aboutStr, icons.aboutIcon, MainMenu.ITEM_POS_RIGHT),
                    MenuItem(ITEM_ID_STATS, statsStr, icons.statsIcon, MainMenu.ITEM_POS_RIGHT),
                    MenuItem(ITEM_ID_CONTINUE, continueStr, icons.continueIcon, MainMenu.ITEM_POS_RIGHT, true))

            continueItem = items.last()

            itemClickListener = {
                when (it.id) {
                    ITEM_ID_CONTINUE -> callback?.onContinueClicked()
                    ITEM_ID_NEW_GAME -> showMenu(newGameMenu)
                    ITEM_ID_SETTINGS -> showMenu(settingsMenu)
                    ITEM_ID_RULES -> showMenu(rulesMenu)
                    ITEM_ID_STATS -> showMenu(statsMenu)
                    ITEM_ID_ABOUT -> showMenu(aboutMenu)
                }
            }
        }
        addMenu(mainMenu)

        // New game menu
        newGameMenu.apply {
            title = newGameStr
            menuPosition = SubMenu.MenuPosition.RIGHT

            startGameItem = MenuItem(1000, strings["menu_start_game"],
                    this@DefaultGameMenu.style.startGameIcon, SubMenu.ITEM_POS_BOTTOM)
            startGameItem.checkable = false
            addItem(startGameItem)

            backArrowClickListener = {
                newGameOptions?.save()
                goBack()
            }

            itemClickListener = {
                if (it === startGameItem) {
                    newGameOptions?.save()
                    callback?.onStartGameClicked()
                }
            }
        }
        addMenu(newGameMenu)

        // Settings menu
        settingsMenu.apply {
            title = settingsStr
            backArrowClickListener = {
                settings?.save()
                goBack()
            }
        }
        addMenu(settingsMenu)

        // Rules menu
        rulesMenu.title = rulesStr
        addMenu(rulesMenu)

        // About menu
        aboutMenu.title = aboutStr
        addMenu(aboutMenu)

        // In game menu
        inGameMenu.apply {
            val icons = this@DefaultGameMenu.style
            addItems(MenuItem(ITEM_ID_BACK, null, icons.backBtnIcon, InGameMenu.ITEM_POS_LEFT),
                    MenuItem(ITEM_ID_SCOREBOARD, null, icons.scoreboardBtnIcon, InGameMenu.ITEM_POS_RIGHT))

            itemClickListener = {
                when (it.id) {
                    ITEM_ID_BACK -> callback?.onExitGameClicked()
                    ITEM_ID_SCOREBOARD -> callback?.onScoreboardOpenClicked()
                    else -> callback?.onInGameMenuItemClicked(it)
                }
            }
        }
        addMenu(inGameMenu)

        // Scoreboard
        scoreboardMenu.title = strings["scoreboard"]
        scoreboardMenu.backArrowClickListener = { callback?.onScoreboardCloseClicked() }
        addMenu(scoreboardMenu)

        // Show main menu at first
        showMenu(mainMenu)
    }

    /**
     * Create a preference group view for a [menu] with [prefs].
     */
    private fun createPreferenceView(menu: ScrollSubMenu, prefs: GamePrefs): PrefsGroup {
        // Create preference group view and set listeners
        val view = PrefsGroup(skin, prefs, drawer)
        view.confirmCallback = confirmCallback

        // Add menu items for each preference category
        var id = 0
        for (entry in prefs.prefs.values) {
            if (entry is PrefCategory) {
                val icon = if (entry.icon == null) style.defaultIcon else skin.getDrawable(entry.icon)
                menu.addItem(MenuItem(id, entry.title, icon, SubMenu.ITEM_POS_TOP))
                id++
            }
        }

        return view
    }

    private fun createMarkdownView(menu: ScrollSubMenu, markdown: Markdown): MarkdownView {
        // Create preference group view and set listeners
        val view = MarkdownView(skin, markdown)
        menu.scrollContent.actor = view

        // Add menu items for each markdown header
        var id = 0
        for (entry in markdown.elements) {
            if (entry is MdElement.Header && entry.text != null) {
                val icon = if (entry.icon == null) style.defaultIcon else skin.getDrawable(entry.icon)
                menu.addItem(MenuItem(id, entry.text!!, icon, SubMenu.ITEM_POS_TOP))
                id++
            }
        }

        return view
    }


    interface Callback {
        /** Called when the continue item in main menu is clicked. */
        fun onContinueClicked() = Unit

        /** Called when the start game item of the new game submenu is clicked. */
        fun onStartGameClicked() = Unit

        /** Called when the back button is clicked in the in-game menu. */
        fun onExitGameClicked() = Unit

        /** Called when the scoreboard is opened. */
        fun onScoreboardOpenClicked() = Unit

        /** Called when the scoreboard is closed. */
        fun onScoreboardCloseClicked() = Unit

        /** Called when an item is clicked in the in game menu, except back and scoreboard items. */
        fun onInGameMenuItemClicked(item: MenuItem) = Unit
    }


    class DefaultGameMenuStyle(
            val defaultIcon: Drawable,
            val newGameIcon: Drawable,
            val continueIcon: Drawable,
            val settingsIcon: Drawable,
            val rulesIcon: Drawable,
            val statsIcon: Drawable,
            val aboutIcon: Drawable,
            val startGameIcon: Drawable,
            val backBtnIcon: Drawable,
            val scoreboardBtnIcon: Drawable)


    companion object {
        private const val ITEM_ID_NEW_GAME = 1000
        private const val ITEM_ID_CONTINUE = 1001
        private const val ITEM_ID_SETTINGS = 1002
        private const val ITEM_ID_RULES = 1003
        private const val ITEM_ID_STATS = 1004
        private const val ITEM_ID_ABOUT = 1005

        private const val ITEM_ID_BACK = 1000
        private const val ITEM_ID_SCOREBOARD = 1001
    }

}
