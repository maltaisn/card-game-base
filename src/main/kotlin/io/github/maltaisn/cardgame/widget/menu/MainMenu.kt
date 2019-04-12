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

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import io.github.maltaisn.cardgame.widget.TimeAction
import ktx.actors.alpha
import ktx.actors.onClick


/**
 * The main menu of the game, has two rows of buttons that lead to submenus.
 * When the menu is created, it is hidden.
 */
class MainMenu(skin: Skin) : MenuTable(skin) {

    val style = skin[MainMenuStyle::class.java]

    private val topRow = Table()
    private val bottomRow = Table()

    override var shown = false
        set(value) {
            if (field == value) return
            field = value

            if (transitionAction == null) {
                transitionAction = TransitionAction()
            }
        }

    internal var transitionAction: TransitionAction? = null
        set(value) {
            if (field != null) removeAction(field)
            field = value
            if (value != null) addAction(value)
        }

    init {
        isVisible = false
        checkable = false

        // Do the layout
        add(topRow).growX().expandY().height(MENU_ROW_HEIGHT)
                .align(Align.top).pad(0f, 10f, 0f, 10f).row()
        add(bottomRow).growX().expandY().height(MENU_ROW_HEIGHT)
                .align(Align.bottom).pad(0f, 10f, 0f, 10f).growX()
    }

    override fun layout() {
        super.layout()

        transitionAction?.let {
            it.topStartY = topRow.y
            it.bottomStartY = bottomRow.y
        }
    }

    override fun invalidateLayout() {
        topRow.clearChildren()
        bottomRow.clearChildren()

        for (item in items) {
            val onTopRow = item.position == MenuItem.Position.TOP
            val btn = MenuButton(skin, style.itemFontStyle, item.title, item.icon).apply {
                onClick { btnClickListener(this) }
                anchorSide = if (onTopRow) MenuButton.Side.TOP else MenuButton.Side.BOTTOM
                iconSide = MenuButton.Side.LEFT
                iconSize = this@MainMenu.style.itemIconSize
            }
            item.menu = this
            item.button = btn
            (if (onTopRow) topRow else bottomRow).add(btn).grow().pad(0f, 15f, 0f, 15f)
        }
    }

    override fun clearActions() {
        super.clearActions()
        transitionAction = null
    }


    internal inner class TransitionAction :
            TimeAction(0.3f, Interpolation.smooth, reversed = !shown) {

        var topStartY = topRow.y
        var bottomStartY = bottomRow.y

        init {
            isVisible = true
            alpha = if (shown) 0f else 1f
            renderToFrameBuffer = true
        }

        override fun update(progress: Float) {
            reversed = !shown

            topRow.y = topStartY + (1 - progress) * MENU_ROW_HEIGHT
            bottomRow.y = bottomStartY + (1 - progress) * -MENU_ROW_HEIGHT
            alpha = progress
        }

        override fun end() {
            isVisible = shown
            renderToFrameBuffer = false
            transitionAction = null

            // Place all animated widgets to their correct position
            topRow.y = topStartY
            bottomRow.y = bottomStartY
        }
    }

    class MainMenuStyle : MenuTableStyle()

    companion object {
        private const val MENU_ROW_HEIGHT = 100f
    }

}