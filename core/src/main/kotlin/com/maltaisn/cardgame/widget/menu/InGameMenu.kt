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
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.Align
import com.maltaisn.cardgame.widget.action.TimeAction
import ktx.actors.alpha
import ktx.actors.onClick
import ktx.style.get


/**
 * A menu shown during the game, with a top bar for buttons.
 */
class InGameMenu(skin: Skin) : MenuTable(skin) {

    val style: InGameMenuStyle = skin.get()

    override var shown
        get() = super.shown
        set(value) {
            if (super.shown == value) return
            super.shown = value

            if (transitionAction == null) {
                transitionAction = TransitionAction()
            }
        }

    private val leftGroup = HorizontalGroup()
    private val rightGroup = HorizontalGroup()


    init {
        checkable = false

        // Do the layout
        pad(50f)
        add(leftGroup).expandY().align(Align.top)
        add().minWidth(200f).grow()
        add(rightGroup).expandY().align(Align.top)
        leftGroup.space(30f)
        rightGroup.space(30f)
    }

    override fun doMenuLayout() {
        leftGroup.clearChildren()
        rightGroup.clearChildren()

        for (item in items) {
            val fontStyle = if (item.important) style.importantItemFontStyle else style.itemFontStyle
            val btn = MenuButton(skin, fontStyle, item.title, item.icon).apply {
                pad(30f)
                add(iconImage).size(style.itemIconSize)
                if (!title.isNullOrBlank()) {
                    add(titleLabel).padLeft(30f)
                }
                onClick { onItemBtnClicked(item) }
            }
            item.button = btn
            (if (item.position == ITEM_POS_LEFT) leftGroup else rightGroup).addActor(btn)
        }
    }


    internal inner class TransitionAction :
            TimeAction(0.3f, Interpolation.smooth, reversed = !shown) {

        init {
            isVisible = true
            alpha = if (shown) 0f else 1f
            renderToFrameBuffer = true
        }

        override fun update(progress: Float) {
            reversed = !shown
            alpha = progress
        }

        override fun end() {
            isVisible = shown
            renderToFrameBuffer = false
            transitionAction = null
        }
    }

    class InGameMenuStyle : MenuTableStyle()

    companion object {
        const val ITEM_POS_LEFT = 0
        const val ITEM_POS_RIGHT = 1
    }

}
