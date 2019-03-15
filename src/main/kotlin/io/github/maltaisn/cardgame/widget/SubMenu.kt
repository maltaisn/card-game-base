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
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.Value
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.utils.Align
import io.github.maltaisn.cardgame.applyBounded
import ktx.actors.alpha
import ktx.actors.plusAssign
import kotlin.math.max


/**
 * A sub menu of the game, has a title, a back arrow, a list of menu items and a content pane.
 * When the menu is created, it is hidden.
 */
class SubMenu(skin: Skin) : MenuTable(skin) {

    val style = skin.get(SubMenuStyle::class.java)

    /** The submenu title, shown at the top. May be null for none. */
    var title: CharSequence?
        set(value) {
            titleLabel.setText(value)
        }
        get() = titleLabel.text

    /** The content of the submenu scroll pane. May be null to show nothing or when the submenu isn't shown. */
    val content = Table()

    override var shown = false
        set(value) {
            if (field == value) return
            field = value

            if (value && items.isNotEmpty()) {
                // Always check the first item of the menu when showing.
                checkItem(items.first())
            }

            if (actions.isEmpty) {
                this += TransitionAction()
            }
        }

    /** The listener called when the back arrow is clicked. */
    var backArrowClickListener: (() -> Unit)? = null

    private var transitionAction: TransitionAction? = null

    private val titleLabel = SdfLabel(null, skin, style.titleStyle)
    private val menuTable = Table()
    private val contentPane = ScrollPane(content, ScrollPane.ScrollPaneStyle(
            style.contentBackground, null, null, null, null))

    init {
        isVisible = false
        checkable = true

        val backBtn = MenuButton(skin, style.titleStyle, null, style.backArrowIcon)
        backBtn.iconSize = 64f
        backBtn.clickListener = { backArrowClickListener?.invoke() }

        // Do the layout
        val headerTable = Table()
        headerTable.pad(25f, 25f, 0f, 20f)
        headerTable.add(backBtn).size(75f, 75f)
        headerTable.add(titleLabel).padLeft(10f).grow()
        add(headerTable).growX().colspan(2).row()

        menuTable.pad(20f, 25f, 25f, 0f)
        menuTable.align(Align.top)
        add(menuTable).width(Value.percentWidth(0.3f, this)).growY()

        content.pad(20f, 20f, 20f, 20f)
        contentPane.setScrollingDisabled(false, false)
        contentPane.setupOverscroll(30f, 100f, 200f)
        contentPane.addListener(object : InputListener() {
            override fun enter(event: InputEvent, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
                stage.scrollFocus = contentPane
            }

            override fun exit(event: InputEvent, x: Float, y: Float, pointer: Int, toActor: Actor?) {
                stage.scrollFocus = null
            }
        })
        add(contentPane).pad(-style.contentBackground.leftWidth,
                -style.contentBackground.topHeight, 0f, 10f).grow()
    }

    override fun layout() {
        super.layout()

        transitionAction?.let {
            it.contentStartY = contentPane.y
        }
    }

    override fun invalidateLayout() {
        // Create the buttons and add them the menu
        menuTable.clearChildren()
        for (item in items) {
            val btn = MenuButton(skin, style.itemFontStyle, item.title, item.icon).apply {
                clickListener = btnClickListener
                anchorSide = MenuButton.Side.RIGHT
                iconSide = MenuButton.Side.LEFT
                iconSize = this@SubMenu.style.itemIconSize
                align(Align.left)
            }
            item.menu = this
            item.button = btn
            menuTable.add(btn).growX().pad(2f, 0f, 2f, 0f).prefHeight(70f).row()
        }

        if (items.isNotEmpty()) {
            checkItem(items.first())
        }
    }

    /** Check an item by [id] and call the listener.*/
    fun checkItem(id: Int) {
        for (item in items) {
            if (item.id == id) {
                checkItem(item)
                break
            }
        }
    }

    /** Check an [item] and call the listener*/
    fun checkItem(item: MenuItem) {
        if (item.menu === this) {
            item.checked = true
            itemClickListener?.invoke(item)
        }
    }

    private inner class TransitionAction : Action() {
        private var elapsed = if (shown) 0f else TRANSITION_DURATION

        var contentStartY = contentPane.y

        init {
            isVisible = true
            alpha = if (shown) 0f else 1f
            renderToFrameBuffer = true
            transitionAction = this
        }

        override fun act(delta: Float): Boolean {
            elapsed += if (shown) delta else -delta
            val progress = TRANSITION_INTERPOLATION.applyBounded(elapsed / TRANSITION_DURATION)

            contentPane.y = contentStartY + (1 - progress) * CONTENT_TRANSITION_TRANSLATE

            val durationGap = max(0f, TRANSITION_DURATION - ITEM_TRANSITION_DURATION) / items.size
            for ((i, item) in items.withIndex()) {
                val btn = item.button!!
                val itemProgress = TRANSITION_INTERPOLATION.applyBounded(
                        elapsed / ITEM_TRANSITION_DURATION - i * durationGap)
                btn.x = menuTable.padLeft + (1 - itemProgress) * -(btn.width + menuTable.padLeft)
            }

            alpha = progress

            if (shown && progress >= 1 || !shown && progress <= 0) {
                isVisible = shown
                renderToFrameBuffer = false
                transitionAction = null

                // Place all animated widgets to their correct position
                contentPane.y = contentStartY
                for (item in items) {
                    item.button?.x = menuTable.padLeft
                }

                return true
            }
            return false
        }
    }

    class SubMenuStyle : MenuTableStyle() {
        lateinit var titleStyle: SdfLabel.SdfLabelStyle
        lateinit var backArrowIcon: Drawable
        lateinit var contentBackground: Drawable
    }

    companion object {
        /** The duration of the overall transition. */
        internal const val TRANSITION_DURATION = 0.7f
        /** The duration of each menu item slide. */
        internal const val ITEM_TRANSITION_DURATION = 0.5f
        /** The Y translation performed by the content table. */
        private const val CONTENT_TRANSITION_TRANSLATE = -100f

        private val TRANSITION_INTERPOLATION = Interpolation.smooth
    }

}