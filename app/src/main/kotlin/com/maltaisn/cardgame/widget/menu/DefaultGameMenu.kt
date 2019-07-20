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
import com.badlogic.gdx.scenes.scene2d.ui.Value
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.I18NBundle
import com.gmail.blueboxware.libgdxplugin.annotations.GDXAssets
import com.maltaisn.cardgame.CoreRes
import com.maltaisn.cardgame.markdown.Markdown
import com.maltaisn.cardgame.markdown.MdElement
import com.maltaisn.cardgame.prefs.GamePref
import com.maltaisn.cardgame.prefs.GamePrefs
import com.maltaisn.cardgame.prefs.ListPref
import com.maltaisn.cardgame.prefs.PrefCategory
import com.maltaisn.cardgame.widget.FontStyle
import com.maltaisn.cardgame.widget.SdfLabel
import com.maltaisn.cardgame.widget.markdown.MarkdownView
import com.maltaisn.cardgame.widget.prefs.PrefsGroup
import ktx.style.get


/**
 * The default implementation of the [GameMenu], with 6 items in main menu:
 * new game, continue, settings, rules, statistics and about.
 * The in game menu has a back button and a scoreboard button.
 */
open class DefaultGameMenu(private val skin: Skin) : GameMenu(skin) {

    /** The callback interface called when an event happens in the menu. */
    var callback: Callback? = null

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

    private val style: DefaultGameMenuStyle = skin.get()


    /** The main menu. */
    val mainMenu = MainMenu(skin)

    /** The menu shown in game. */
    val inGameMenu = InGameMenu(skin)

    /** The menu used as the scoreboard. */
    val scoreboardMenu = PagedSubMenu(skin)

    // The submenus
    private val newGameMenu = ScrollSubMenu(skin)
    private var newGameView: PrefsGroup? = null

    private val settingsMenu = ScrollSubMenu(skin)
    private var settingsView: PrefsGroup? = null

    private val rulesMenu = ScrollSubMenu(skin)
    private var rulesView: Table? = null

    private val statsMenu = ScrollSubMenu(skin)

    private val aboutMenu = ScrollSubMenu(skin)


    // Preference help drawer
    private val prefsHelpLabel = SdfLabel(skin, style.prefsHelpFontStyle)
    private val prefsHelpListener: (GamePref) -> Unit = { pref ->
        // When a help icon is clicked, show drawer with help text
        drawer.apply {
            content.actor = prefsHelpLabel
            content.pad(0f, 60f, 0f, 60f)
            drawerWidth = Value.percentWidth(0.5f, drawer)
            title = pref.shortTitle ?: pref.title
            shown = true
        }
        prefsHelpLabel.setText(pref.help)
    }

    // Preference list drawer
    private var prefsListCurrentPref: ListPref? = null
    private val prefsList = MenuDrawerList(skin)
    private val prefsListListener: (ListPref) -> Unit = { pref ->
        // When a list preference value is clicked, show drawer with the list items.
        prefsListCurrentPref = pref
        drawer.apply {
            content.actor = prefsList
            content.pad(0f, 0f, 0f, 0f)
            drawerWidth = Value.percentWidth(0.4f, drawer)
            title = pref.shortTitle ?: pref.title
            shown = true
        }
        prefsList.items = pref.values
        prefsList.selectedIndex = pref.keys.indexOf(pref.value)
    }

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
            addItem(MenuItem(ITEM_ID_NEW_GAME, newGameStr, icons.newGameIcon, MainMenu.ITEM_POS_BOTTOM))
            addItem(MenuItem(ITEM_ID_CONTINUE, continueStr, icons.continueIcon, MainMenu.ITEM_POS_BOTTOM))
            addItem(MenuItem(ITEM_ID_SETTINGS, settingsStr, icons.settingsIcon, MainMenu.ITEM_POS_BOTTOM))
            addItem(MenuItem(ITEM_ID_RULES, rulesStr, icons.rulesIcon, MainMenu.ITEM_POS_TOP))
            addItem(MenuItem(ITEM_ID_STATS, statsStr, icons.statsIcon, MainMenu.ITEM_POS_TOP))
            addItem(MenuItem(ITEM_ID_ABOUT, aboutStr, icons.aboutIcon, MainMenu.ITEM_POS_TOP))

            continueItem = items[1]

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

        // Drawer widgets
        prefsHelpLabel.setWrap(true)
        prefsHelpLabel.setAlignment(Align.topLeft)

        prefsList.selectionChangeListener = { index ->
            if (index != -1) {
                prefsListCurrentPref!!.value = prefsListCurrentPref!!.keys[index]
            }
        }

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
                    callback?.onStartGameClicked()
                }
            }
        }

        // Settings menu
        settingsMenu.apply {
            title = settingsStr
            backArrowClickListener = {
                settings?.save()
                goBack()
            }
        }

        // Rules menu
        rulesMenu.apply {
            title = rulesStr
        }

        // Statistics menu
        statsMenu.apply {
            title = statsStr
        }

        // About menu
        aboutMenu.apply {
            title = aboutStr
            addItem(MenuItem(0, "About", skin.getDrawable(MenuIcons.INFO), SubMenu.ITEM_POS_TOP))
            addItem(MenuItem(1, "Donate", skin.getDrawable(MenuIcons.ARROW_RIGHT), SubMenu.ITEM_POS_TOP))
        }

        // In game menu
        inGameMenu.apply {
            val icons = this@DefaultGameMenu.style
            addItem(MenuItem(ITEM_ID_BACK, null, icons.backBtnIcon, InGameMenu.ITEM_POS_LEFT))
            addItem(MenuItem(ITEM_ID_SCOREBOARD, null, icons.scoreboardBtnIcon, InGameMenu.ITEM_POS_RIGHT))

            itemClickListener = {
                when (it.id) {
                    ITEM_ID_BACK -> callback?.onExitGameClicked()
                    ITEM_ID_SCOREBOARD -> callback?.onScoreboardOpenClicked()
                    else -> callback?.onInGameMenuItemClicked(it)
                }
            }
        }

        // Scoreboard
        scoreboardMenu.title = strings["scoreboard"]
        scoreboardMenu.backArrowClickListener = { callback?.onScoreboardCloseClicked() }

        // Show main menu at first
        showMenu(mainMenu)
    }

    /**
     * Create a preference group view for a [menu] with [prefs].
     */
    private fun createPreferenceView(menu: ScrollSubMenu, prefs: GamePrefs): PrefsGroup {
        // Create preference group view and set listeners
        val view = PrefsGroup(skin, prefs)
        view.helpListener = prefsHelpListener
        view.listClickListener = prefsListListener

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

    private fun createMarkdownView(menu: ScrollSubMenu, markdown: Markdown): Table {
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


    class DefaultGameMenuStyle {
        lateinit var defaultIcon: Drawable

        lateinit var prefsHelpFontStyle: FontStyle

        lateinit var newGameIcon: Drawable
        lateinit var continueIcon: Drawable
        lateinit var settingsIcon: Drawable
        lateinit var rulesIcon: Drawable
        lateinit var statsIcon: Drawable
        lateinit var aboutIcon: Drawable

        lateinit var startGameIcon: Drawable
        lateinit var backBtnIcon: Drawable

        lateinit var scoreboardBtnIcon: Drawable
    }

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
