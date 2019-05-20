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
import com.maltaisn.cardgame.Resources
import com.maltaisn.cardgame.markdown.Markdown
import com.maltaisn.cardgame.markdown.MdElement
import com.maltaisn.cardgame.prefs.GamePref
import com.maltaisn.cardgame.prefs.GamePrefs
import com.maltaisn.cardgame.prefs.ListPref
import com.maltaisn.cardgame.prefs.PrefCategory
import com.maltaisn.cardgame.widget.FontStyle
import com.maltaisn.cardgame.widget.ScrollView
import com.maltaisn.cardgame.widget.SdfLabel
import com.maltaisn.cardgame.widget.markdown.MarkdownView
import com.maltaisn.cardgame.widget.prefs.PrefsGroup
import ktx.style.get


/**
 * The default implementation of the [GameMenu], with 6 items in main menu:
 * new game, continue, settings, rules, statistics and about.
 * The in game menu has a back button and a scoreboard button.
 */
class DefaultGameMenu(private val skin: Skin) : GameMenu(skin) {

    /** Listener called when the continue item is clicked in the main menu. */
    var continueListener: (() -> Unit)? = null

    /**
     * Listener called when the start game item is clicked in the new game submenu.
     * When the item is clicked, the options are automatically saved and the game menu is closed.
     */
    var startGameListener: (() -> Unit)? = null

    /** Listener called when the back button is clicked in game. */
    var exitGameListener: (() -> Unit)? = null

    /** Listener called when the scoreboard button is clicked in game. */
    var scoreboardListener: (() -> Unit)? = null


    /** The continue menu item. Can be disabled. */
    val continueItem: MenuItem

    /** The game preferences, shown in the setting submenu. */
    var settings: GamePrefs? = null
        set(value) {
            field = value
            settingsView?.detachListeners()
            settingsMenu.items.clear()
            settingsView = if (value != null) {
                createPreferenceView(settingsMenu, value)
            } else {
                null
            }
            settingsMenu.content.actor = settingsView
            settingsMenu.invalidateLayout()
        }

    /** The new game game options, shown in the new game submenu. */
    var newGameOptions: GamePrefs? = null
        set(value) {
            field = value
            newGameView?.detachListeners()
            newGameMenu.items.subList(0, newGameMenu.items.size - 1).clear()
            newGameView = if (value != null) {
                createPreferenceView(newGameMenu, value)
            } else {
                null
            }
            newGameMenu.content.actor = newGameView
            newGameMenu.invalidateLayout()
        }

    /** The game rules in markdown. */
    var rules: Markdown? = null
        set(value) {
            field = value
            rulesMenu.items.clear()
            rulesView = if (value != null) {
                createMarkdownView(rulesMenu, value)
            } else {
                null
            }
            rulesMenu.content.actor = rulesView
            rulesMenu.invalidateLayout()
        }

    private val style = skin[DefaultGameMenuStyle::class.java]

    // The submenus
    private val newGameMenu = SubMenu(skin)
    private val settingsMenu = SubMenu(skin)
    private val rulesMenu = SubMenu(skin)
    private val statsMenu = SubMenu(skin)
    private val aboutMenu = SubMenu(skin)

    // Submenu views
    private var newGameView: PrefsGroup? = null
    private var settingsView: PrefsGroup? = null
    private var rulesView: Table? = null

    // Preference help drawer
    private val prefsHelpLabel = SdfLabel(skin, style.prefsHelpFontStyle)
    private val prefsHelpListener: (GamePref) -> Unit = { pref ->
        // When a help icon is clicked, show drawer with help text
        drawer.apply {
            content.actor = prefsHelpLabel
            content.pad(0f, 30f, 0f, 30f)
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
        val bundle: I18NBundle = skin[Resources.CORE_STRINGS_NAME]

        drawer.backBtnText = bundle["menu_drawer_back"]

        val newGameStr = bundle["menu_new_game"]
        val continueStr = bundle["menu_continue"]
        val settingsStr = bundle["menu_settings"]
        val rulesStr = bundle["menu_rules"]
        val statsStr = bundle["menu_stats"]
        val aboutStr = bundle["menu_about"]

        mainMenu.apply {
            // Add main menu items
            val icons = this@DefaultGameMenu.style
            items += MenuItem(ITEM_ID_NEW_GAME, newGameStr, icons.newGameIcon, MainMenu.ITEM_POS_BOTTOM)
            items += MenuItem(ITEM_ID_CONTINUE, continueStr, icons.continueIcon, MainMenu.ITEM_POS_BOTTOM)
            items += MenuItem(ITEM_ID_SETTINGS, settingsStr, icons.settingsIcon, MainMenu.ITEM_POS_BOTTOM)
            items += MenuItem(ITEM_ID_RULES, rulesStr, icons.rulesIcon, MainMenu.ITEM_POS_TOP)
            items += MenuItem(ITEM_ID_STATS, statsStr, icons.statsIcon, MainMenu.ITEM_POS_TOP)
            items += MenuItem(ITEM_ID_ABOUT, aboutStr, icons.aboutIcon, MainMenu.ITEM_POS_TOP)
            invalidateLayout()

            continueItem = items[1]

            itemClickListener = {
                when (it.id) {
                    ITEM_ID_CONTINUE -> continueListener?.invoke()
                    ITEM_ID_NEW_GAME -> showSubMenu(newGameMenu)
                    ITEM_ID_SETTINGS -> showSubMenu(settingsMenu)
                    ITEM_ID_RULES -> showSubMenu(rulesMenu)
                    ITEM_ID_STATS -> showSubMenu(statsMenu)
                    ITEM_ID_ABOUT -> showSubMenu(aboutMenu)
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

            val startGameItem = MenuItem(1000, bundle["menu_start_game"],
                    this@DefaultGameMenu.style.startGameIcon, SubMenu.ITEM_POS_BOTTOM)
            startGameItem.checkable = false
            items += startGameItem

            backArrowClickListener = {
                newGameOptions?.save()
                showMainMenu()
            }

            val sectionClickListener = SectionClickListener(newGameMenu)
            itemClickListener = {
                if (it === startGameItem) {
                    showInGameMenu()
                    newGameOptions?.save()
                    startGameListener?.invoke()
                } else {
                    sectionClickListener(it)
                }
            }
            contentPane.scrollListener = SectionScrollListener(newGameMenu)

            invalidateLayout()
        }

        // Settings menu
        settingsMenu.apply {
            title = settingsStr
            backArrowClickListener = {
                settings?.save()
                showMainMenu()
            }
            itemClickListener = SectionClickListener(settingsMenu)
            contentPane.scrollListener = SectionScrollListener(settingsMenu)
            invalidateLayout()
        }

        // Rules menu
        rulesMenu.apply {
            title = rulesStr
            itemClickListener = SectionClickListener(rulesMenu)
            contentPane.scrollListener = SectionScrollListener(rulesMenu)
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
            items += MenuItem(0, "About", skin.getDrawable(MenuIcons.INFO), SubMenu.ITEM_POS_TOP)
            items += MenuItem(1, "Donate", skin.getDrawable(MenuIcons.ARROW_RIGHT), SubMenu.ITEM_POS_TOP)
            invalidateLayout()
        }

        // In game menu
        inGameMenu.apply {
            val icons = this@DefaultGameMenu.style
            items += MenuItem(ITEM_ID_BACK, null, icons.backBtnIcon, InGameMenu.ITEM_POS_LEFT)
            items += MenuItem(ITEM_ID_SCOREBOARD, null, icons.scoreboardBtnIcon, InGameMenu.ITEM_POS_RIGHT)
            invalidateLayout()

            itemClickListener = {
                when (it.id) {
                    ITEM_ID_BACK -> {
                        // Exit game
                        showMainMenu()
                        exitGameListener?.invoke()
                    }
                    ITEM_ID_SCOREBOARD -> {
                        // Show scoreboard maybe?
                        scoreboardListener?.invoke()
                    }
                }
            }
        }
    }

    /**
     * Create a preference group view for a [menu] with [prefs].
     */
    private fun createPreferenceView(menu: SubMenu, prefs: GamePrefs): PrefsGroup {
        // Create preference group view and set listeners
        val view = PrefsGroup(skin, prefs)
        view.helpListener = prefsHelpListener
        view.listClickListener = prefsListListener

        // Add menu items for each preference category
        var id = 0
        for (entry in prefs.prefs.values) {
            if (entry is PrefCategory) {
                val icon = if (entry.icon == null) style.defaultIcon else skin.getDrawable(entry.icon)
                menu.items += MenuItem(id, entry.title, icon, SubMenu.ITEM_POS_TOP)
                id++
            }
        }

        return view
    }

    private fun createMarkdownView(menu: SubMenu, markdown: Markdown): Table {
        // Create preference group view and set listeners
        val view = MarkdownView(skin, markdown)
        menu.content.actor = view

        // Add menu items for each markdown header
        var id = 0
        for (entry in markdown.elements) {
            if (entry is MdElement.Header && entry.text != null) {
                val icon = if (entry.icon == null) style.defaultIcon else skin.getDrawable(entry.icon)
                menu.items += MenuItem(id, entry.text!!, icon, SubMenu.ITEM_POS_TOP)
                id++
            }
        }

        return view
    }

    /**
     * A menu item click listener for scrolling to a section when items are clicked.
     */
    private class SectionClickListener(private val menu: SubMenu) : (MenuItem) -> Unit {

        override fun invoke(item: MenuItem) {
            // When a menu item is clicked, scroll the content pane to the section top.
            var id = 0
            val scrollPane = menu.contentPane
            for (child in menu.content.actor.children) {
                if (child is MenuContentSection) {
                    if (id == item.id) {
                        val top = child.y + child.height + 20f - scrollPane.height
                        val height = scrollPane.height
                        scrollPane.scrollTo(0f, top, 0f, height)
                        break
                    }
                    id++
                }
            }
        }
    }

    /**
     * A scroll listener for checking a submenu section depending on the scroll position.
     */
    private class SectionScrollListener(private val menu: SubMenu) :
            (ScrollView, Float, Float, Float, Float) -> Unit {

        override fun invoke(scrollView: ScrollView, x: Float, y: Float, dx: Float, dy: Float) {
            // Change the checked menu item if needed
            var newId = MenuItem.NO_ID
            val oldId = menu.checkedItem?.id ?: MenuItem.NO_ID
            when {
                scrollView.isTopEdge -> {
                    // Scrolled to top edge, always check first checkable item
                    for (item in menu.items) {
                        if (item.checkable) {
                            newId = item.id
                            break
                        }
                    }
                }
                scrollView.isBottomEdge -> {
                    // Scrolled to bottom edge, always check last checkable item
                    for (item in menu.items) {
                        if (item.checkable) {
                            newId = item.id
                        }
                    }
                }
                else -> {
                    // Find the view of the currently checked category
                    var id = 0
                    val content = menu.content.actor
                    for (child in content.children) {
                        if (child is MenuContentSection) {
                            if (id == oldId) {
                                val bottom = child.y
                                val top = bottom + child.height

                                // If currently checked category is completely hidden, check another
                                val maxY = content.height - y
                                val minY = maxY - scrollView.height
                                val tooHigh = top > maxY && bottom > maxY
                                val tooLow = top < minY && bottom < minY
                                if (tooHigh || tooLow) {
                                    // If too high check next category, otherwise check previous.
                                    newId = (oldId + if (tooHigh) 1 else -1).coerceIn(0, menu.items.size - 1)
                                }
                                break
                            }
                            id++
                        }
                    }
                }
            }
            if (newId != MenuItem.NO_ID && newId != oldId) {
                menu.checkItem(newId, false)
            }
        }
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
        private const val ITEM_ID_NEW_GAME = 0
        private const val ITEM_ID_CONTINUE = 1
        private const val ITEM_ID_SETTINGS = 2
        private const val ITEM_ID_RULES = 3
        private const val ITEM_ID_STATS = 4
        private const val ITEM_ID_ABOUT = 5

        private const val ITEM_ID_BACK = 0
        private const val ITEM_ID_SCOREBOARD = 1
    }

}