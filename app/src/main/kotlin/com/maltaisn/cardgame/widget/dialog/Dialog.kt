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

package com.maltaisn.cardgame.widget.dialog

import com.badlogic.gdx.Input
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.*
import com.badlogic.gdx.scenes.scene2d.ui.Cell
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.maltaisn.cardgame.defaultSize
import com.maltaisn.cardgame.findScrollFocus
import com.maltaisn.cardgame.widget.FboTable
import com.maltaisn.cardgame.widget.action.ActionDelegate
import com.maltaisn.cardgame.widget.action.TimeAction
import ktx.actors.alpha
import ktx.actors.onKeyDown
import ktx.actors.setKeyboardFocus
import ktx.style.get


/**
 * An empty game dialog.
 */
open class Dialog(skin: Skin) : FboTable(skin) {

    private val style: DialogStyle = skin.get()

    /**
     * Whether the dialog is shown or not.
     * Like [isVisible] but with the correct value during a transition.
     */
    var shown = false
        private set

    private var transitionAction by ActionDelegate<TimeAction>()

    /**
     * The dialog content table.
     */
    val content = Table()

    private val contentCell: Cell<Table>

    /**
     * The fixed width of the dialog content, in pixels.
     * The default width is 640 pixels. Use `0` for no fixed size.
     */
    var dialogWidth: Float
        get() = contentCell.maxWidth
        set(value) {
            if (value == 0f) {
                contentCell.defaultSize()
            } else {
                contentCell.width(value)
                content.invalidateHierarchy()
            }
        }

    /**
     * Whether to hide the dialog when user clicks outside of it.
     */
    var dismissOnClickOutside = false

    private var oldKeyboardFocus: Actor? = null
    private var oldScrollFocus: Actor? = null


    init {
        isVisible = false
        touchable = Touchable.enabled
        background = style.shadow
        content.background = style.background
        contentCell = add(content).width(800f).pad(60f)
        setFillParent(true)

        onKeyDown(true) {
            if (it == Input.Keys.BACK || it == Input.Keys.ESCAPE) {
                // Dismiss dialog on back press.
                dismiss()
            }
        }
        addCaptureListener(object : InputListener() {
            private var backgroundPressed = false

            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                if (dismissOnClickOutside && isClickOutside(x, y)) {
                    backgroundPressed = true
                    return true
                }
                return false
            }

            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                if (backgroundPressed && isClickOutside(x, y)) {
                    backgroundPressed = false
                    dismiss()
                }
            }

            fun isClickOutside(x: Float, y: Float) =
                    (shown && hit(x, y, false) === this@Dialog)
        })
    }

    override fun clearActions() {
        super.clearActions()
        transitionAction?.end()
    }

    /**
     * Show the dialog on a [stage] with a transition.
     * The scroll and keyboard focus are set to the dialog.
     */
    fun show(stage: Stage) {
        if (shown) return
        shown = true

        stage.addActor(this@Dialog)

        oldKeyboardFocus = stage.keyboardFocus
        oldScrollFocus = stage.scrollFocus
        setKeyboardFocus()
        findScrollFocus()

        if (transitionAction == null) {
            transitionAction = TransitionAction()
        }
    }

    /**
     * Hide the dialog and remove it from the stage.
     * The previous scroll and keyboard focuses are restored.
     */
    fun hide() {
        if (!shown) return
        shown = false

        stage?.keyboardFocus = oldKeyboardFocus
        stage?.scrollFocus = oldScrollFocus

        if (transitionAction == null) {
            transitionAction = TransitionAction()
        }
    }

    fun dismiss() {
        onDismiss()
        hide()
    }

    /**
     * Called when the dialog is hidden by a back press or
     * by a click outside if [dismissOnClickOutside] is `true`.
     */
    open fun onDismiss() = Unit


    private inner class TransitionAction :
            TimeAction(0.4f, Interpolation.smooth, reversed = !shown) {

        init {
            isVisible = true
            alpha = if (shown) 1f else 0f
            renderToFrameBuffer = true
        }

        override fun update(progress: Float) {
            reversed = !shown
            alpha = progress
        }

        override fun end() {
            isVisible = shown
            alpha = 1f
            renderToFrameBuffer = false
            transitionAction = null
            if (!shown) {
                remove()
            }
        }
    }

    class DialogStyle {
        lateinit var shadow: Drawable
        lateinit var background: Drawable
    }

}
