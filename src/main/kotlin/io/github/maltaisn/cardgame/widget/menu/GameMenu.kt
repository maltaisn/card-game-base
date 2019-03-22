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
import ktx.actors.*


/**
 * The game menu that manages the animation between the main menu and sub menus.
 * It also sets the back arrow action on sub menus and handles the back key press.
 */
open class GameMenu(skin: Skin) : Stack() {

    /** The main menu. A click listener can be set to open submenus and do other actions. */
    val mainMenu = MainMenu(skin)

    /** The currently shown submenu, `null` if main menu is shown. */
    private var subMenu: SubMenu? = null

    /**
     * Changing this value animates a visibility change by sliding the menu parts in and out of the screen.
     * If changed during an outgoing transition, the previous one will be inverted.
     * The main menu is always shown when showing the game menu. If a submenu was opened before it gets closed.
     */
    var shown = false
        set(value) {
            if (field == value) return
            field = value

            setKeyboardFocus(value)  // Needed to catch back key press

            if (mainMenuShown) {
                mainMenu.shown = value
                if (subMenu != null) {
                    this -= subMenu!!
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
    private var mainMenuShown = true


    init {
        onKeyDown { keycode ->
            if (keycode == Input.Keys.BACK || keycode == Input.Keys.ESCAPE) {
                // Back arrow closes the submenu
                closeSubMenu()
            }
        }

        this += mainMenu
    }

    /**
     * Open a [subMenu]. If another submenu is opened or animated on screen, this does nothing.
     */
    fun openSubMenu(subMenu: SubMenu) {
        if (children.size > 1) {
            // A submenu is still on screen.
            return
        }

        this.subMenu = subMenu
        this += subMenu

        subMenu.backArrowClickListener = { closeSubMenu() }

        mainMenu.shown = false
        val newAction = mainMenu.actions.first() then object : Action() {
            override fun act(delta: Float): Boolean {
                if (shown) {
                    subMenu.shown = true
                    mainMenuShown = false
                }
                return true
            }
        }
        mainMenu.clearActions()
        mainMenu += newAction
    }

    /**
     * If a sub menu is opened, close it.
     */
    fun closeSubMenu() {
        if (subMenu?.shown != true) return
        val menu = subMenu!!

        menu.shown = false
        val newAction = menu.actions.first() then object : Action() {
            override fun act(delta: Float): Boolean {
                this@GameMenu -= menu
                subMenu = null
                if (shown) {
                    mainMenu.shown = true
                    mainMenuShown = true
                }
                return true
            }
        }
        menu.clearActions()
        menu += newAction
    }

}