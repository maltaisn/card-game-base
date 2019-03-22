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

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Widget
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.scenes.scene2d.utils.TransformDrawable
import io.github.maltaisn.cardgame.applyBounded
import io.github.maltaisn.cardgame.widget.menu.SubMenu
import io.github.maltaisn.cardgame.withinBounds
import kotlin.math.absoluteValue


/**
 * A switch widget that can be checked and unchecked with animation.
 */
class Switch(val style: SwitchStyle) : Widget() {

    /**
     * Whether the switch is checked or not.
     * Changing this property will always result in an animation.
     */
    var checked = false
        set(value) {
            if (field == value) return
            field = value
            checkListener?.invoke(value)
        }

    /** Whether the switch can be pressed, hovered and checked. */
    var enabled = true
        set(value) {
            field = value
            if (!value) {
                pressed = false
                hovered = false
                hoverAlpha = 0f
                pressAlpha = 0f
            }
        }


    /**
     * The listener called when the checked state is changed.
     * The listener is also called when the state is changed programatically.
     */
    var checkListener: ((Boolean) -> Unit)? = null


    private var checkElapsed = 0f
    private var checkAlpha = 0f

    private var hovered = false
    private var hoverElapsed = 0f
    private var hoverAlpha = 0f

    private var pressed = false
    private var pressElapsed = 0f
    private var pressAlpha = 0f
    private var pressPosX = 0f
    private var checkAlphaOnPress = 0f

    private var dragged = false

    constructor(skin: Skin, styleName: String = "default") :
            this(skin[styleName, SwitchStyle::class.java])


    init {
        setSize(prefWidth, prefHeight)

        addListener(object : InputListener() {
            private var disabledContentPane: SubMenu.ContentPane? = null

            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                if (enabled && button == Input.Buttons.LEFT) {
                    pressed = true
                    pressElapsed = 0f
                    pressPosX = x
                    checkAlphaOnPress = checkAlpha
                    dragged = false

                    // If switch is in a disableable scrollpane, disable it.
                    var parent = parent
                    while (parent != null) {
                        if (parent is SubMenu.ContentPane) {
                            parent.scrollingEnabled = false
                            disabledContentPane = parent
                            break
                        }
                        parent = parent.parent
                    }
                }
                return true
            }

            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                if (enabled && button == Input.Buttons.LEFT) {
                    pressed = false
                    pressElapsed = PRESS_FADE_DURATION * pressAlpha

                    if (dragged) {
                        // Thumb was dragged by user, change check state from position
                        // Check animation elapsed time is linearly approximated, it's not perfect.
                        checked = checkAlpha >= 0.5f
                        checkElapsed = checkAlpha * CHECK_DURATION
                    } else if (withinBounds(x, y)) {
                        checked = !checked
                    }

                    // Re-enable scrollpane if necessary
                    disabledContentPane?.scrollingEnabled = true
                    disabledContentPane = null
                }
            }

            override fun touchDragged(event: InputEvent, x: Float, y: Float, pointer: Int) {
                if (pressed && pointer == Input.Buttons.LEFT) {
                    val distance = x - pressPosX
                    if (distance.absoluteValue > MIN_DRAG_DISTANCE) {
                        dragged = true
                    }
                    val trackWidth = (style.background.minWidth - style.thumb.minWidth) * style.scale
                    checkAlpha = (checkAlphaOnPress + distance / trackWidth * DRAG_SPEED).coerceIn(0f, 1f)
                    Gdx.graphics.requestRendering()
                }
            }

            override fun enter(event: InputEvent, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
                // Pointer must be -1 because hovering can only happen on desktop.
                // Also when on touch down/up, an enter/exit event is fired, but that shouldn't stop hovering.
                if (enabled && pointer == -1) {
                    hovered = true
                    hoverElapsed = 0f
                }
            }

            override fun exit(event: InputEvent, x: Float, y: Float, pointer: Int, toActor: Actor?) {
                if (enabled && pointer == -1) {
                    hovered = false
                    hoverElapsed = HOVER_FADE_DURATION * hoverAlpha
                }
            }
        })
    }

    override fun act(delta: Float) {
        super.act(delta)

        var renderingNeeded = false

        // Update check alpha
        // If pressed, do nothing because the check alpha is changed with the touch position.
        if (!pressed && (checked && checkElapsed < CHECK_DURATION || !checked && checkElapsed > 0f)) {
            checkElapsed += if (checked) delta else -delta
            checkAlpha = CHECK_INTERPOLATION.applyBounded(checkElapsed / CHECK_DURATION)
            renderingNeeded = true
        }

        // Update press alpha
        if (pressed && pressElapsed < PRESS_FADE_DURATION) {
            pressElapsed += delta
            pressAlpha = PRESS_IN_INTERPOLATION.applyBounded(pressElapsed / PRESS_FADE_DURATION)
            renderingNeeded = true
        } else if (!pressed && pressElapsed > 0f) {
            pressElapsed -= delta
            pressAlpha = PRESS_OUT_INTERPOLATION.applyBounded(pressElapsed / PRESS_FADE_DURATION)
            renderingNeeded = true
        }

        // Update hover alpha
        if (hovered && hoverElapsed < HOVER_FADE_DURATION) {
            hoverElapsed += delta
            hoverAlpha = HOVER_IN_INTERPOLATION.applyBounded(hoverElapsed / HOVER_FADE_DURATION)
            renderingNeeded = true
        } else if (!hovered && hoverElapsed > 0f) {
            hoverElapsed -= delta
            hoverAlpha = HOVER_OUT_INTERPOLATION.applyBounded(hoverElapsed / HOVER_FADE_DURATION)
            renderingNeeded = true
        }

        if (renderingNeeded) {
            Gdx.graphics.requestRendering()
        }
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        super.draw(batch, parentAlpha)

        val scale = style.scale

        // Draw background
        batch.setColor(color.r, color.g, color.b, parentAlpha)
        val background = style.background as TransformDrawable
        background.draw(batch, x, y, 0f, 0f, background.minWidth,
                background.minHeight, scale, scale, 0f)

        // Draw check overlay
        batch.setColor(color.r, color.g, color.b, parentAlpha * checkAlpha)
        val checkOverlay = style.checkOverlay as TransformDrawable
        checkOverlay.draw(batch, x, y, 0f, 0f, checkOverlay.minWidth,
                checkOverlay.minHeight, scale, scale, 0f)

        // Draw thumb
        batch.setColor(color.r, color.g, color.b, parentAlpha * if (enabled) 1f else 0.5f)
        val thumb = style.thumb as TransformDrawable
        val thumbX = x + (background.minWidth - thumb.minWidth) * scale * checkAlpha
        thumb.draw(batch, thumbX, y, 0f, 0f, thumb.minWidth,
                thumb.minHeight, scale, scale, 0f)

        // Draw thumb hover/press
        batch.setColor(color.r, color.g, color.b, parentAlpha * (hoverAlpha + pressAlpha) * 0.1f)
        val thumbHover = style.thumbHoverOverlay as TransformDrawable
        thumbHover.draw(batch, thumbX, y, 0f, 0f, thumbHover.minWidth,
                thumbHover.minHeight, scale, scale, 0f)
    }

    override fun getPrefWidth() = style.background.minWidth * style.scale

    override fun getPrefHeight() = style.background.minHeight * style.scale

    /**
     * Change the checked state of the switch
     * @param animate Whether to animate the check or not.
     */
    fun check(checked: Boolean, animate: Boolean) {
        this.checked = checked
        if (!animate) {
            if (checked) {
                checkAlpha = 1f
                checkElapsed = CHECK_DURATION
            } else {
                checkAlpha = 0f
                checkElapsed = 0f
            }
        }
    }

    class SwitchStyle {
        lateinit var background: Drawable
        lateinit var checkOverlay: Drawable
        lateinit var thumb: Drawable
        lateinit var thumbHoverOverlay: Drawable
        var scale = 0f
    }

    companion object {
        /** The duration of the press fade. */
        private const val PRESS_FADE_DURATION = 0.3f

        /** The duration of the hover fade. */
        private const val HOVER_FADE_DURATION = 0.3f

        /** The duration of the check animation. */
        private const val CHECK_DURATION = 0.3f

        /** The minimum drag distance in pixels to enable drag mode. */
        private const val MIN_DRAG_DISTANCE = 5f

        /** How fast is the thumb moving compared to the pointer when dragged. */
        private const val DRAG_SPEED = 0.5f

        private val PRESS_IN_INTERPOLATION: Interpolation = Interpolation.smooth
        private val PRESS_OUT_INTERPOLATION: Interpolation = Interpolation.smooth
        private val HOVER_IN_INTERPOLATION: Interpolation = Interpolation.pow2Out
        private val HOVER_OUT_INTERPOLATION: Interpolation = Interpolation.smooth
        private val CHECK_INTERPOLATION: Interpolation = Interpolation.smooth
    }

}