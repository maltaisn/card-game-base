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
import com.badlogic.gdx.scenes.scene2d.ui.Value
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.utils.Align
import com.maltaisn.cardgame.findScrollFocus
import com.maltaisn.cardgame.widget.action.TimeAction
import com.maltaisn.cardgame.widget.action.applyBounded
import com.maltaisn.cardgame.widget.text.FontStyle
import com.maltaisn.cardgame.widget.text.SdfLabel
import ktx.actors.alpha
import ktx.actors.onClick
import ktx.style.get
import kotlin.math.max


/**
 * A sub menu of the game, has a title, a back arrow, a list of menu items and a content pane.
 * The list of items is optional and can be either on the left or the right side.
 *
 * FIXME if item has a title too long, menu layout breaks.
 *  Add an option in MenuButton to allow ellipsis.
 */
open class SubMenu(skin: Skin) : MenuTable(skin) {

    val style: SubMenuStyle = skin.get()

    /** The submenu title, shown at the top. May be `null` for none. */
    var title: CharSequence?
        get() = titleLabel.text
        set(value) {
            titleLabel.setText(value)
        }

    /** The position of the menu pane. */
    var menuPosition = MenuPosition.LEFT
        set(value) {
            field = value
            invalidateMenuLayout()
        }

    /** The sub menu content table. */
    val content = Table()

    /** Should be shown after being added to the stage to properly gain scroll focus. */
    override var shown
        get() = super.shown
        set(value) {
            if (super.shown == value) return
            super.shown = value

            content.findScrollFocus()

            if (transitionAction == null) {
                transitionAction = TransitionAction()
            }
        }

    /** Returns the first checked item in the menu, `null` if none are checked */
    val checkedItem: MenuItem?
        get() {
            for (item in items) {
                if (item.checked) {
                    return item
                }
            }
            return null
        }

    /** The listener called when the back arrow is clicked. */
    var backArrowClickListener: (() -> Unit)? = null

    private val headerTable = Table()
    private val titleLabel = SdfLabel(skin, style.titleStyle)
    private val menuTable = Table()

    init {
        checkable = true

        val backBtn = MenuButton(skin, style.titleStyle, null, style.backArrowIcon).apply {
            pad(30f)
            iconSize = this@SubMenu.style.backArrowSize
            onClick { backArrowClickListener?.invoke() }
        }

        headerTable.pad(50f, 50f, 0f, 50f)
        headerTable.add(backBtn)
        headerTable.add(titleLabel).padLeft(30f).grow()

        content.background = style.contentBackground
        content.padLeft(content.padLeft + 40f)
        content.padRight(content.padRight + 40f)
    }

    override fun layout() {
        super.layout()

        (transitionAction as TransitionAction?)?.contentStartY = content.y
    }

    override fun doMenuLayout() {
        // Do the submenu layout. If there are no menu items, hide the menu pane.
        clearChildren()
        add(headerTable).growX().colspan(2).row()

        when {
            items.isEmpty() -> {
                // No menu items, center content with 70% with.
                getCell(headerTable).padBottom(20f)
                add(content).pad(-style.contentBackground.topHeight, 0f, 0f, 0f)
                        .width(Value.percentWidth(0.7f, this)).grow()
            }
            menuPosition == MenuPosition.LEFT -> {
                // Menu pane on the left
                menuTable.pad(40f, 50f, 50f, 0f)
                add(menuTable).width(Value.percentWidth(0.3f, this)).growY()
                add(content).pad(-style.contentBackground.topHeight,
                        -style.contentBackground.leftWidth, 0f, 20f).grow()
            }
            else -> {
                // Menu pane on the right
                menuTable.pad(70f, 0f, 50f, 50f)
                add(content).pad(-style.contentBackground.topHeight + 30f, 20f,
                        0f, -style.contentBackground.rightWidth).grow()
                add(menuTable).width(Value.percentWidth(0.3f, this)).growY()
            }
        }

        // Create the menu buttons and add them the menu table
        menuTable.clearChildren()
        for (item in items) {
            if (item.position == ITEM_POS_TOP) {
                addButtonToMenuTable(item)
            }
        }
        menuTable.add().grow().row()
        for (item in items) {
            if (item.position == ITEM_POS_BOTTOM) {
                addButtonToMenuTable(item)
            }
        }

        // Check any checked item
        for (item in items) {
            item.checked = item.checked
        }

        content.findScrollFocus()
    }

    private fun addButtonToMenuTable(item: MenuItem) {
        val btn = MenuButton(skin, style.itemFontStyle, item.title, item.icon).apply {
            onClick { btnClickListener(this) }
            anchorSide = if (menuPosition == MenuPosition.LEFT) MenuButton.Side.RIGHT else MenuButton.Side.LEFT
            iconSide = MenuButton.Side.LEFT
            iconSize = this@SubMenu.style.itemIconSize
            enabled = item.enabled
            align(Align.left)
        }
        item.button = btn

        if (item.shown) {
            val iconPadding = if (item.icon == null) btn.iconSize + 30f else 0f
            menuTable.add(btn).growX().pad(2f, 0f, 2f, 0f).prefHeight(140f)
            if (menuPosition == MenuPosition.LEFT) {
                btn.pad(20f, 20f + iconPadding, 20f, 40f)
            } else {
                btn.pad(20f, 40f + iconPadding, 20f, 20f)
            }
            menuTable.row()
        }
    }

    private inner class TransitionAction :
            TimeAction(TRANSITION_DURATION, Interpolation.smooth, reversed = !shown) {

        var contentStartY = content.y

        init {
            isVisible = true
            alpha = if (shown) 0f else 1f
            renderToFrameBuffer = true
        }

        override fun update(progress: Float) {
            reversed = !shown

            // Animate the content Y position and alpha
            content.y = contentStartY + (1 - progress) * CONTENT_TRANSITION_TRANSLATE
            alpha = progress

            // Slide each item after the other. Top items are shown after and hidden before.
            // There's a delay (durationGap) between each item slide.
            val durationGap = max(0f, TRANSITION_DURATION - ITEM_TRANSITION_DURATION) / items.size
            for ((i, btn) in menuTable.children.withIndex()) {
                val itemProgress = 1 - interpolation.applyBounded(
                        (elapsed * SPEED_MULTIPLIER - (items.size - i - 1)
                                * durationGap) / ITEM_TRANSITION_DURATION)
                btn.x = menuTable.padLeft + itemProgress * (btn.width + menuTable.padLeft) *
                        if (menuPosition == MenuPosition.LEFT) -1 else 1
            }
        }

        override fun end() {
            isVisible = shown
            renderToFrameBuffer = false
            transitionAction = null

            // Place all animated widgets to their correct position
            content.y = contentStartY
            for (item in items) {
                item.button?.x = menuTable.padLeft
            }
        }
    }

    class SubMenuStyle : MenuTableStyle() {
        lateinit var titleStyle: FontStyle
        lateinit var backArrowIcon: Drawable
        var backArrowSize = 0f
        lateinit var contentBackground: Drawable
    }

    enum class MenuPosition {
        LEFT, RIGHT
    }

    companion object {
        const val ITEM_POS_TOP = 0
        const val ITEM_POS_BOTTOM = 1

        /** The duration of the overall transition. */
        private const val TRANSITION_DURATION = 0.5f

        /** The duration of each menu item slide. */
        private const val ITEM_TRANSITION_DURATION = 0.35f

        /** The Y translation performed by the content table. */
        private const val CONTENT_TRANSITION_TRANSLATE = -200f
    }

}
