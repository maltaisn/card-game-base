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

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.scenes.scene2d.Action
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Container
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Stack
import ktx.actors.onKeyDown
import ktx.actors.onKeyboardFocusEvent
import ktx.actors.setKeyboardFocus
import ktx.actors.then


/**
 * The game menu that manages the animation between the main menu, the sub menu and the in-game menu.
 * It also sets the back arrow action on sub menus and handles the back key press.
 */
open class GameMenu(skin: Skin) : Stack() {

    /** The main menu. A click listener can be set to open submenus and do other actions. */
    val mainMenu = MainMenu(skin)

    /** The menu shown in game. */
    val inGameMenu = InGameMenu(skin)

    /** The currently shown menu. */
    var shownMenu: MenuTable = mainMenu
        private set

    /** The menu that will be shown after animation. */
    private var nextShownMenu: MenuTable? = null

    private val subMenuContainer = Container<SubMenu>()

    /**
     * The menu drawer. Content should always be set before showing.
     * The drawer back button text needs to be set.
     */
    val drawer = MenuDrawer(skin)

    private var transitionAction: Action? = null
        set(value) {
            if (field != null) removeAction(field)
            field = value
            if (value != null) addAction(value)
        }


    init {
        onKeyDown(true) {
            if (it == Input.Keys.BACK || it == Input.Keys.ESCAPE) {
                if (shownMenu is MainMenu && nextShownMenu == null) {
                    // Exit if main menu is shown but not in animation
                    Gdx.app.exit()
                } else {
                    showMainMenu()
                }
            }
        }
        onKeyboardFocusEvent { event, _ ->
            if (!event.isFocused && event.relatedActor == null) {
                // When the keyboard focus is set to null and menu is shown, set it to the menu
                event.cancel()
                setKeyboardFocus(true)
            }
        }

        add(inGameMenu)
        add(mainMenu)
        add(subMenuContainer)
        add(drawer)

        subMenuContainer.fill()

        mainMenu.shown = true
    }


    override fun setStage(stage: Stage?) {
        super.setStage(stage)
        setKeyboardFocus()
    }

    override fun clearActions() {
        super.clearActions()
        transitionAction = null
    }

    /**
     * Show a [subMenu].
     */
    fun showSubMenu(subMenu: SubMenu) = showMenu(subMenu)

    /**
     * Show the main menu.
     */
    fun showMainMenu() = showMenu(mainMenu)

    /**
     * Show the in-game menu.
     */
    fun showInGameMenu() = showMenu(inGameMenu)

    /**
     * Show any [menu], hiding the previous one then showing the new one in a sequence animation.
     * If no back arrow listener was set on submenu, a default one that only returns the main menu is set.
     */
    private fun showMenu(menu: MenuTable) {
        if (shownMenu !== menu && nextShownMenu !== menu) {
            nextShownMenu = menu

            if (menu is SubMenu && menu.backArrowClickListener == null) {
                menu.backArrowClickListener = { showMainMenu() }
            }

            shownMenu.shown = false
            transitionAction = shownMenu.transitionAction?.then(object : Action() {
                override fun act(delta: Float): Boolean {
                    nextShownMenu = null
                    subMenuContainer.actor = menu as? SubMenu
                    menu.shown = true
                    shownMenu = menu
                    return true
                }
            })
        }
    }

}