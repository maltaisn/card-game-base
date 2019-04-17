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

import com.badlogic.gdx.Input
import com.badlogic.gdx.scenes.scene2d.Action
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Stack
import ktx.actors.onKeyDown
import ktx.actors.onKeyboardFocusEvent
import ktx.actors.setKeyboardFocus
import ktx.actors.then


/**
 * The game menu that manages the animation between the main menu and sub menus.
 * It also sets the back arrow action on sub menus and handles the back key press.
 */
open class GameMenu(skin: Skin) : Stack() {

    /** The main menu. A click listener can be set to open submenus and do other actions. */
    val mainMenu = MainMenu(skin)

    /**
     * The menu drawer. Content should always be set before showing.
     * The drawer back button text needs to be set.
     */
    val drawer = MenuDrawer(skin)

    /** The currently shown submenu, `null` if main menu is shown. */
    var subMenu: SubMenu? = null
        private set

    /**
     * Changing this value animates a visibility change by sliding the menu parts in and out of the screen.
     * If changed during an outgoing transition, the previous one will be inverted.
     * The main menu is always shown when showing the game menu. If a submenu was opened before, it gets closed.
     */
    var shown = false
        set(value) {
            setKeyboardFocus(value)

            if (field == value) return
            field = value

            if (mainMenuShown) {
                mainMenu.shown = value
                if (subMenu != null) {
                    removeActor(subMenu)
                    subMenu = null
                }
            } else {
                subMenu?.shown = false
            }
            mainMenuShown = true
        }

    /**
     * Whether the main menu is currently shown.
     * During transitions, this is true until the main menu is completely closed,
     * and false until the submenu is completely closed.
     */
    var mainMenuShown = true
        private set


    private var transitionAction: Action? = null
        set(value) {
            if (field != null) removeAction(field)
            field = value
            if (value != null) addAction(value)
        }

    private val defaultBackListener = { closeSubMenu() }


    init {
        onKeyDown {
            if (it == Input.Keys.BACK || it == Input.Keys.ESCAPE) {
                closeSubMenu()
            }
        }
        onKeyboardFocusEvent { event, _ ->
            if (shown && !event.isFocused && event.relatedActor == null) {
                // When the keyboard focus is set to null and menu is shown, set it to the menu
                event.cancel()
                setKeyboardFocus(true)
            }
        }

        addActor(mainMenu)
        addActor(drawer)
    }

    override fun clearActions() {
        super.clearActions()
        transitionAction = null
    }

    /**
     * Open a [subMenu]. If another submenu is opened or animated on screen, this does nothing.
     * If no back arrow listener was set, a default one that only returns the main menu is set.
     */
    fun openSubMenu(subMenu: SubMenu) {
        if (children.size > 2) {
            // A submenu is still on screen.
            return
        }

        this.subMenu = subMenu
        this.addActorAt(1, subMenu)

        if (subMenu.backArrowClickListener == null) {
            subMenu.backArrowClickListener = defaultBackListener
        }

        mainMenu.shown = false
        transitionAction = mainMenu.transitionAction!! then object : Action() {
            override fun act(delta: Float): Boolean {
                if (shown) {
                    subMenu.shown = true
                    mainMenuShown = false
                }
                return true
            }
        }
    }

    /**
     * If a sub menu is opened, close it.
     */
    fun closeSubMenu() {
        if (subMenu?.shown != true) return
        val menu = subMenu!!

        if (menu.backArrowClickListener === defaultBackListener) {
            menu.backArrowClickListener = null
        }

        menu.shown = false
        transitionAction = menu.transitionAction!! then object : Action() {
            override fun act(delta: Float): Boolean {
                removeActor(menu)
                subMenu = null
                if (shown) {
                    mainMenu.shown = true
                    mainMenuShown = true
                }
                return true
            }
        }
    }

}