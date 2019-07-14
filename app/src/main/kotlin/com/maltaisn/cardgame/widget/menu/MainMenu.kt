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

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.maltaisn.cardgame.widget.action.TimeAction
import ktx.actors.alpha
import ktx.actors.onClick
import ktx.style.get


/**
 * The main menu of the game, has a row of items on top and one on bottom.
 */
class MainMenu(skin: Skin) : MenuTable(skin) {

    val style: MainMenuStyle = skin.get()

    private val topRow = Table()
    private val bottomRow = Table()

    override var shown
        get() = super.shown
        set(value) {
            if (super.shown == value) return
            super.shown = value

            if (transitionAction == null) {
                transitionAction = TransitionAction()
            }
        }


    init {
        checkable = false

        // Do the layout
        add(topRow).growX().expandY().height(MENU_ROW_HEIGHT)
                .align(Align.top).pad(0f, 10f, 0f, 10f).row()
        add(bottomRow).growX().expandY().height(MENU_ROW_HEIGHT)
                .align(Align.bottom).pad(0f, 10f, 0f, 10f).growX()
    }

    override fun layout() {
        super.layout()

        (transitionAction as TransitionAction?)?.let {
            it.topStartY = topRow.y
            it.bottomStartY = bottomRow.y
        }
    }

    override fun doMenuLayout() {
        topRow.clearChildren()
        bottomRow.clearChildren()

        for (item in items) {
            val onTopRow = (item.position == ITEM_POS_TOP)
            val btn = MenuButton(skin, style.itemFontStyle, item.title, item.icon).apply {
                onClick { btnClickListener(this) }
                anchorSide = if (onTopRow) MenuButton.Side.TOP else MenuButton.Side.BOTTOM
                iconSide = MenuButton.Side.LEFT
                iconSize = this@MainMenu.style.itemIconSize
                enabled = item.enabled
            }
            item.button = btn

            if (item.shown) {
                (if (onTopRow) topRow else bottomRow).add(btn).grow().pad(0f, 15f, 0f, 15f)
            }
        }
    }


    private inner class TransitionAction :
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
        const val ITEM_POS_TOP = 0
        const val ITEM_POS_BOTTOM = 1

        private const val MENU_ROW_HEIGHT = 100f
    }

}
