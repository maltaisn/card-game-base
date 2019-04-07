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

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import io.github.maltaisn.cardgame.defaultSize
import io.github.maltaisn.cardgame.widget.ScrollView
import io.github.maltaisn.cardgame.widget.SdfLabel
import io.github.maltaisn.cardgame.widget.TimeAction


/**
 * Drawer widget for showing extra content in a menu.
 * The drawer slides in and out from the rightside of the screen.
 */
class MenuDrawer(skin: Skin) : WidgetGroup() {

    /** The drawer title, can be `null` for none. */
    var title: CharSequence?
        set(value) {
            val titleShown = (value != null && value.isNotEmpty())
            titleLabel.isVisible = titleShown
            titleLabel.setText(value)

            // Change the size of the cell according to its visibility
            // since no text in a label still take a certain height.
            val titleCell = drawerTable.getCell(titleLabel)
            if (titleShown) {
                titleCell.defaultSize().padBottom(20f)
            } else {
                titleCell.size(0f, 0f).padBottom(0f)
            }
        }
        get() = titleLabel.text

    /** The drawer back button text, can be `null` for none. */
    var backBtnText: CharSequence?
        set(value) {
            backBtnLabel.setText(value)
        }
        get() = backBtnLabel.text

    /** The drawer width. */
    var drawerWidth: Value = Value.percentWidth(0.5f, this)
        set(value) {
            field = value
            invalidate()
        }

    /**
     * Changing this value animates a visibility change by sliding the drawer in and out of the screen.
     * If changed during an outgoing transition, the previous one will be inverted.
     */
    var shown = false
        set(value) {
            if (field == value) return

            // Focus or unfocus the drawer for scrolling
            if (!field) {
                scrollFocusBefore = stage.scrollFocus
                stage.scrollFocus = contentPane
            } else {
                stage.scrollFocus = scrollFocusBefore
                scrollFocusBefore = null
            }

            field = value

            if (actions.isEmpty) {
                transitionAction = TransitionAction()
            }
        }

    /** The container in the drawer scroll pane. Change its actor to change the content. */
    var content = Container<Actor>()

    /** The scroll pane containing the [content] container. */
    val contentPane = ScrollView(content)


    private val style = skin[MenuDrawerStyle::class.java]

    private val drawerTable = Table()
    private val titleLabel = SdfLabel(null, skin, style.titleFontStyle)
    private val backBtnLabel = SdfLabel(null, skin, style.backBtnFontStyle)

    private var scrollFocusBefore: Actor? = null

    private var transitionAction: TransitionAction? = null
        set(value) {
            if (field != null) removeAction(field)
            field = value
            if (value != null) addAction(value)
        }

    private var backgroundAlpha = 1f

    init {
        isVisible = false

        // A capture listener is used to block event from going through the shadowed background.
        addCaptureListener(object : InputListener() {
            private var backgroundPressed = false

            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                if (shown && x < drawerTable.x) {
                    backgroundPressed = true
                    event.stop()
                    return true
                }
                return false
            }

            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                if (backgroundPressed && shown && x < drawerTable.x) {
                    shown = false
                }
            }
        })

        drawerTable.background = style.drawerBackground

        content.fill()
        contentPane.setScrollingDisabled(true, false)
        contentPane.setOverscroll(false, false)
        contentPane.setCancelTouchFocus(false)

        // Back button
        val backBtn = Table()
        val backBtnIcon = Image(style.backBtnIcon, Scaling.fit)
        backBtnIcon.color = style.backBtnIconColor
        backBtn.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                // If back button is clicked, dismiss the drawer.
                shown = false
            }
        })
        backBtn.touchable = Touchable.enabled
        backBtn.add(backBtnIcon).size(style.backBtnFontStyle.fontSize + 6f)
        backBtn.add(backBtnLabel).padLeft(10f)
        backBtn.pad(20f, 30f, 15f, 30f)

        val headerSep = Image(style.headerSeparator, Scaling.stretchX)

        // Do the layout
        drawerTable.add(backBtn).expandX().align(Align.left).row()
        drawerTable.add(headerSep).growX().pad(5f, 30f, 20f, 0f).row()
        drawerTable.add(titleLabel).growX().pad(0f, 30f, 20f, 30f).row()
        drawerTable.add(contentPane).grow()

        addActor(drawerTable)

        title = null  // For setting title label height to 0
    }


    override fun layout() {
        // Place the drawer on the right and set its size
        val dw = drawerWidth.get() + style.drawerBackground.leftWidth
        drawerTable.setBounds(width - dw, 0f, dw, height)

        transitionAction?.let {
            it.drawerStartX = drawerTable.x
            it.act(0f)
        }
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        // Draw background
        batch.setColor(color.r, color.g, color.b, color.a * backgroundAlpha * parentAlpha)
        style.background.draw(batch, 0f, 0f, width, height)

        super.draw(batch, parentAlpha)
    }

    private inner class TransitionAction :
            TimeAction(0.4f, Interpolation.smooth, reversed = !shown) {

        var drawerStartX = drawerTable.x

        init {
            isVisible = true
            backgroundAlpha = if (shown) 0f else 1f
        }

        override fun update(progress: Float) {
            reversed = !shown
            drawerTable.x = drawerStartX + (1 - progress) * drawerTable.width
            backgroundAlpha = progress
        }

        override fun end() {
            isVisible = shown
            transitionAction = null
            drawerTable.x = drawerStartX
        }
    }

    class MenuDrawerStyle {
        lateinit var background: Drawable
        lateinit var drawerBackground: Drawable
        lateinit var backBtnFontStyle: SdfLabel.FontStyle
        lateinit var backBtnIcon: Drawable
        lateinit var backBtnIconColor: Color
        lateinit var titleFontStyle: SdfLabel.FontStyle
        lateinit var headerSeparator: Drawable
    }

}