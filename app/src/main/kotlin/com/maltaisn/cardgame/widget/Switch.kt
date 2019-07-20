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

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.maltaisn.cardgame.withinBounds
import ktx.actors.setKeyboardFocus
import ktx.style.get
import kotlin.math.absoluteValue


/**
 * A switch widget that can be checked and unchecked with animation.
 */
class Switch(skin: Skin) : CheckableWidget() {

    val style: SwitchStyle = skin.get()

    init {
        // A capture listener is used to intercept the touch down event,
        // so that if the switch is in a scroll pane, it won't be able to scroll.
        addCaptureListener(object : InputListener() {
            private var pressPosX = 0f
            private var checkAlphaOnPress = 0f
            private var dragged = false

            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                if (enabled && button == Input.Buttons.LEFT) {
                    pressed = true
                    addPressAction()

                    setKeyboardFocus()

                    pressPosX = x
                    checkAlphaOnPress = checkAlpha
                    dragged = false

                    event.stop()
                    return true
                }
                return false
            }

            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                if (enabled && button == Input.Buttons.LEFT) {
                    pressed = false
                    addPressAction()

                    if (dragged) {
                        // Thumb was dragged by user, change check state from position
                        // Check animation elapsed time is linearly approximated, it's not perfect.
                        checked = checkAlpha >= 0.5f
                        addCheckAction()
                        checkAction?.elapsed = checkAlpha * checkAction!!.duration
                    } else if (withinBounds(x, y)) {
                        checked = !checked
                        addCheckAction()
                    }
                }
            }

            override fun touchDragged(event: InputEvent, x: Float, y: Float, pointer: Int) {
                if (enabled) {
                    val distance = x - pressPosX
                    if (distance.absoluteValue > MIN_DRAG_DISTANCE) {
                        dragged = true
                    }
                    val trackWidth = style.background.minWidth - style.thumb.minWidth
                    checkAlpha = (checkAlphaOnPress + distance / trackWidth * DRAG_SPEED).coerceIn(0f, 1f)
                    Gdx.graphics.requestRendering()
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
        })
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        super.draw(batch, parentAlpha)

        val background = style.background
        val offsetX = (width - background.minWidth) / 2
        val offsetY = (height - background.minHeight) / 2

        // Draw background
        val bgColor = style.backgroundColor
        batch.setColor(bgColor.r * color.a, bgColor.g * color.g,
                bgColor.b * color.b, parentAlpha)
        background.draw(batch, x + offsetX, y + offsetY,
                background.minWidth, background.minHeight)

        // Draw check overlay
        val checkedColor = if (enabled) style.checkedColor else style.checkedDisabledColor
        batch.setColor(checkedColor.r * color.a, checkedColor.g * color.g,
                checkedColor.b * color.b, parentAlpha * checkAlpha)
        background.draw(batch, x + offsetX, y + offsetY,
                background.minWidth, background.minHeight)

        // Draw thumb
        batch.setColor(color.r, color.g, color.b, parentAlpha)
        val thumb = style.thumb
        val thumbX = x + offsetX + (background.minWidth - thumb.minWidth) * checkAlpha
        thumb.draw(batch, thumbX, y + offsetY, thumb.minWidth, thumb.minHeight)

        // Draw thumb hover/press overlay
        batch.setColor(color.r, color.g, color.b,
                parentAlpha * (hoverAlpha + pressAlpha) * 0.1f + if (enabled) 0f else 0.2f)
        val thumbOverlay = style.thumbOverlay
        thumbOverlay.draw(batch, thumbX, y + offsetY,
                thumbOverlay.minWidth, thumbOverlay.minHeight)
    }

    override fun getPrefWidth() = style.background.minWidth

    override fun getPrefHeight() = style.background.minHeight


    class SwitchStyle {
        lateinit var background: Drawable
        lateinit var thumb: Drawable
        lateinit var thumbOverlay: Drawable
        lateinit var backgroundColor: Color
        lateinit var checkedColor: Color
        lateinit var checkedDisabledColor: Color
    }

    companion object {
        /** The minimum drag distance in pixels to enable drag mode. */
        private const val MIN_DRAG_DISTANCE = 20f

        /** How fast is the thumb moving compared to the pointer when dragged. */
        private const val DRAG_SPEED = 0.5f
    }

}
