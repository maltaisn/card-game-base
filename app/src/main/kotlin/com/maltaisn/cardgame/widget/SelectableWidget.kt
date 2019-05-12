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

package com.maltaisn.cardgame.widget

import com.badlogic.gdx.Input
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table


abstract class SelectableWidget(skin: Skin? = null) : Table(skin) {

    /**
     * Whether the widget can be pressed, hovered and clicked.
     */
    open var enabled = true
        set(value) {
            if (field == value) return
            field = value

            if (value) {
                touchable = Touchable.enabled
            } else {
                touchable = Touchable.disabled

                pressed = false
                pressAlpha = 0f
                pressAction = null

                hovered = false
                hoverAlpha = 0f
                hoverAction = null
            }
        }

    protected var hovered = false
    protected open var hoverAlpha = 0f
    protected var hoverAction: HoverAction? = null
        set(value) {
            if (field != null) removeAction(field)
            field = value
            if (value != null) addAction(value)
        }

    protected var pressed = false
    protected open var pressAlpha = 0f
    protected var pressAction: PressAction? = null
        set(value) {
            if (field != null) removeAction(field)
            field = value
            if (value != null) addAction(value)
        }

    init {
        touchable = Touchable.enabled
    }

    override fun clearActions() {
        super.clearActions()
        hoverAction = null
        pressAction = null
    }

    protected fun addHoverAction() {
        if (hoverAction == null) {
            hoverAction = HoverAction()
        }
    }

    protected fun addPressAction() {
        if (pressAction == null) {
            pressAction = PressAction()
        }
    }

    protected inner class HoverAction : TimeAction(0.3f,
            Interpolation.pow2Out, Interpolation.smooth, reversed = !hovered) {
        override fun update(progress: Float) {
            reversed = !hovered
            hoverAlpha = progress
        }

        override fun end() {
            hoverAction = null
        }
    }

    protected inner class PressAction : TimeAction(0.3f,
            Interpolation.smooth, reversed = !pressed) {
        override fun update(progress: Float) {
            reversed = !pressed
            pressAlpha = progress
        }

        override fun end() {
            pressAction = null
        }
    }

    protected inner class SelectionListener : InputListener() {
        override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
            if (enabled && button == Input.Buttons.LEFT) {
                pressed = true
                addPressAction()
                return true
            }
            return false
        }

        override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
            if (enabled && button == Input.Buttons.LEFT) {
                pressed = false
                addPressAction()
            }
        }

        override fun enter(event: InputEvent, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
            if (enabled && pointer == -1) {
                hovered = true
                addHoverAction()
            }
        }

        override fun exit(event: InputEvent, x: Float, y: Float, pointer: Int, toActor: Actor?) {
            if (enabled && pointer == -1) {
                hovered = false
                addHoverAction()
            }
        }
    }

}