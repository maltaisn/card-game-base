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
import com.badlogic.gdx.scenes.scene2d.Action
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Stack
import com.maltaisn.cardgame.widget.action.ActionDelegate
import ktx.actors.setKeyboardFocus
import ktx.actors.then
import java.util.*


/**
 * A game menu that manages the animation between different menu tables.
 * It also sets the back arrow action on sub menus and handles the back key press.
 */
open class GameMenu(skin: Skin) : Stack() {

    /** The currently shown menu. */
    var shownMenu: MenuTable? = null
        private set

    /** The stack of previously shown menus to go back to. */
    private var backStack = LinkedList<MenuTable>()

    /** The menu that will be shown after animation. */
    private var nextShownMenu: MenuTable? = null

    /** The menu drawer. The drawer back button text needs to be set. */
    val drawer = MenuDrawer(skin)


    private var transitionAction by ActionDelegate<Action>()


    init {
        add(drawer)
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
     * Go back to previous menu, or exit app if there was no previous menu.
     */
    fun goBack() {
        if (backStack.isNotEmpty()) {
            showMenu(backStack.pop(), false)
        } else {
            Gdx.app.exit()
        }
    }

    /**
     * Add a [menu] to the game menu, without showing it.
     * If the menu is already added, nothing will happen.
     */
    fun addMenu(menu: MenuTable) {
        if (menu !in children) {
            // Add the menu if not added yet.
            addActorBefore(drawer, menu)
        }
    }

    /**
     * Show any [menu], hiding the previous one then showing the new one in a sequence animation.
     * If no back arrow listener was set on [SubMenu], a default one that only returns the previous menu is set.
     * If [saveLast], the last menu will be added to the back stack.
     */
    fun showMenu(menu: MenuTable, saveLast: Boolean = true) {
        if (shownMenu !== menu && nextShownMenu !== menu) {
            addMenu(menu)

            if (saveLast && shownMenu != null) {
                backStack.push(shownMenu)
            }

            nextShownMenu = menu

            // Set or unset the default back arrow listener if none was set
            if (menu is SubMenu && menu.backArrowClickListener == null) {
                menu.backArrowClickListener = ::goBack
            }
            (shownMenu as? SubMenu)?.let {
                if (it.backArrowClickListener === ::goBack) {
                    it.backArrowClickListener = null
                }
            }

            if (menu is ScrollSubMenu) {
                menu.scrollToTop()
            }

            shownMenu?.shown = false

            val newMenuAction = object : Action() {
                override fun act(delta: Float): Boolean {
                    nextShownMenu = null
                    menu.shown = true
                    shownMenu = menu
                    transitionAction = null
                    return true
                }
            }
            transitionAction = shownMenu?.transitionAction?.then(newMenuAction) ?: newMenuAction
        }
    }

}
