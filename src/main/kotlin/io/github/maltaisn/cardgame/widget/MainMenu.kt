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

package io.github.maltaisn.cardgame.widget

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.Action
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import io.github.maltaisn.cardgame.applyBounded
import ktx.actors.alpha
import ktx.actors.plusAssign


/**
 * The main menu of the game, has two rows of buttons that lead to submenus.
 * When the menu is created, it is hidden.
 */
class MainMenu(skin: Skin) : MenuTable(skin) {

    val style = skin.get(MainMenuStyle::class.java)

    private val topRow = Table()
    private val bottomRow = Table()

    override var shown = false
        set(value) {
            if (field == value) return
            field = value

            if (actions.isEmpty) {
                this += TransitionAction()
            }
        }

    private var transitionAction: TransitionAction? = null

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
            val onTopRow = item.position == SIDE_TOP
            val btn = MenuButton(skin, style.itemFontStyle, item.title, item.icon).apply {
                clickListener = btnClickListener
                anchorSide = if (onTopRow) MenuButton.Side.TOP else MenuButton.Side.BOTTOM
                iconSide = MenuButton.Side.LEFT
                iconSize = this@MainMenu.style.itemIconSize
            }
            item.menu = this
            item.button = btn
            (if (onTopRow) topRow else bottomRow).add(btn).grow().pad(0f, 15f, 0f, 15f)
        }
    }

    private inner class TransitionAction : Action() {
        private var elapsed = if (shown) 0f else TRANSITION_DURATION

        var topStartY = topRow.y
        var bottomStartY = bottomRow.y

        init {
            isVisible = true
            alpha = if (shown) 0f else 1f
            renderToFrameBuffer = true
            transitionAction = this
        }

        override fun act(delta: Float): Boolean {
            elapsed += if (shown) delta else -delta
            val progress = TRANSITION_INTERPOLATION.applyBounded(elapsed / TRANSITION_DURATION)

            topRow.y = topStartY + (1 - progress) * MENU_ROW_HEIGHT
            bottomRow.y = bottomStartY + (1 - progress) * -MENU_ROW_HEIGHT
            alpha = progress

            if (shown && progress >= 1 || !shown && progress <= 0) {
                isVisible = shown
                renderToFrameBuffer = false
                transitionAction = null

                // Place all animated widgets to their correct position
                topRow.y = topStartY
                bottomRow.y = bottomStartY

                return true
            }
            return false
        }
    }

    class MainMenuStyle : MenuTableStyle()

    companion object {
        const val SIDE_TOP = 0
        const val SIDE_BOTTOM = 1

        internal const val TRANSITION_DURATION = 2.0f
        private const val MENU_ROW_HEIGHT = 100f

        private val TRANSITION_INTERPOLATION = Interpolation.smooth
    }

}