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
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Action
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Widget
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.scenes.scene2d.utils.TransformDrawable
import io.github.maltaisn.cardgame.applyBounded
import io.github.maltaisn.cardgame.withinBounds
import ktx.actors.plusAssign
import ktx.math.vec2
import kotlin.math.round


/**
 * A slider widget that can be dragged to change its progress.
 */
class Slider(val style: SliderStyle) : Widget() {

    /**
     * The slider progress. Will be rounded to a multiple of [step] within min and max,
     * so it's important to set it after min, max and step values.
     */
    var progress = 0f
        set(value) {
            if (field == value) return
            field = (step * round(value / step)).coerceIn(minProgress, maxProgress)
            changeListener?.invoke(field)
            realProgress = field
            posInvalid = true
            clearActions()
        }

    /**
     * The slider minimum progress. Must be less or equal to
     * [maxProgress] and will be rounded to a multiple of [step].
     */
    var minProgress = 0f
        set(value) {
            if (field == value) return
            field = value
            posInvalid = true
            boundsValidated = false
        }

    /**
     * The slider maximum progress. Must be greater or equal to
     * [minProgress] and will be rounded to a multiple of [step].
     */
    var maxProgress = 100f
        set(value) {
            if (field == value) return
            field = value
            posInvalid = true
            boundsValidated = false
        }

    /** The amount by which the value is incremented, must be positive. */
    var step = 1f
        set(value) {
            if (field == value) return
            field = value
            posInvalid = true
            boundsValidated = false
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
     * The listener called when the slider progress is changed.
     * The listener is also called when the progress is changed programatically.
     */
    var changeListener: ((Float) -> Unit)? = null

    private var boundsValidated = false
    private var posInvalid = true

    private var hovered = false
    private var hoverElapsed = 0f
    private var hoverAlpha = 0f

    private var pressed = false
    private var pressElapsed = 0f
    private var pressAlpha = 0f

    private var sliderPressed = false

    // Information for slider sprites drawing and interaction
    private var realProgress = 0f
    private var percentProgress = 0f
    private var trackX = 0f
    private var trackY = 0f
    private var thumbX = 0f
    private var trackWidth = 0f
    private var trackFilledWidth = 0f


    constructor(skin: Skin, styleName: String = "default") :
            this(skin[styleName, SliderStyle::class.java])


    init {
        setSize(prefWidth, prefHeight)

        // A capture listener is used to intercept the touch down event,
        // so that if the slider is in a scroll pane, it won't be able to scroll.
        addCaptureListener(object : InputListener() {
            private var pressOffset = 0f
            private var pressStagePos = vec2()

            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                if (enabled && button == Input.Buttons.LEFT) {
                    sliderPressed = true
                    pressStagePos.set(event.stageX, event.stageY)
                    if (isPointInThumb(x, y)) {
                        pressed = true
                        pressElapsed = 0f
                        pressOffset = x - trackFilledWidth
                        event.stop()
                        return true
                    }
                }
                return false
            }

            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                if (enabled && button == Input.Buttons.LEFT) {
                    if (pressed) {
                        pressed = false
                        pressElapsed = PRESS_FADE_DURATION * pressAlpha
                    } else if (sliderPressed && withinBounds(x, y) &&
                            Vector2.len(event.stageX - pressStagePos.x,
                                    event.stageY - pressStagePos.y) < MAX_SLIDE_DRAG_DISTANCE) {
                        // To slide, touch must start and end within bounds and touch must not have been dragged too much.
                        pressOffset = style.thumb.minWidth * style.scale / 2
                        slideTo(getProgressForX(x))
                    }
                    sliderPressed = false
                }
            }

            override fun touchDragged(event: InputEvent, x: Float, y: Float, pointer: Int) {
                if (enabled) {
                    updateHoveredState(x, y)

                    if (pressed) {
                        progress = getProgressForX(x)
                        Gdx.graphics.requestRendering()
                    }
                }
            }

            private fun getProgressForX(x: Float): Float {
                val newProgress = (x - pressOffset) / trackWidth * (maxProgress - minProgress) + minProgress
                return (step * round(newProgress / step)).coerceIn(minProgress, maxProgress)
            }
        })

        addListener(object : InputListener() {
            override fun mouseMoved(event: InputEvent, x: Float, y: Float): Boolean {
                updateHoveredState(x, y)
                return false
            }

            override fun exit(event: InputEvent, x: Float, y: Float, pointer: Int, toActor: Actor?) {
                if (enabled && pointer == -1) {
                    hovered = false
                    hoverElapsed = HOVER_FADE_DURATION * hoverAlpha
                }
            }
        })
    }

    override fun invalidate() {
        super.invalidate()
        posInvalid = true
    }

    override fun act(delta: Float) {
        super.act(delta)

        var renderingNeeded = false

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

        // Draw track, filled on the left side of the thumb and empty on the right side
        val trackEmpty = style.trackEmpty as TransformDrawable
        val trackFilled = (if (enabled) style.trackFilled else style.trackFilledDisabled) as TransformDrawable
        batch.setColor(color.r, color.g, color.b, parentAlpha)
        trackEmpty.draw(batch, trackX + trackFilledWidth, trackY, 0f, 0f,
                (trackWidth - trackFilledWidth) / scale, trackEmpty.minHeight, scale, scale, 0f)
        trackFilled.draw(batch, trackX, trackY, 0f, 0f, trackFilledWidth / scale,
                trackFilled.minHeight, scale, scale, 0f)

        // Draw thumb
        val thumb = style.thumb as TransformDrawable
        batch.setColor(color.r, color.g, color.b, parentAlpha)
        thumb.draw(batch, thumbX, y, 0f, 0f, thumb.minWidth,
                thumb.minHeight, scale, scale, 0f)

        // Draw thumb hover/press/disabled overlay
        val thumbHover = style.thumbHoverOverlay as TransformDrawable
        batch.setColor(color.r, color.g, color.b, parentAlpha *
                (hoverAlpha + pressAlpha) * 0.1f + if (enabled) 0f else 0.2f)
        thumbHover.draw(batch, thumbX, y, 0f, 0f, thumbHover.minWidth,
                thumbHover.minHeight, scale, scale, 0f)
    }

    override fun validate() {
        super.validate()

        // Validate min, max, step values.
        if (!boundsValidated) {
            require(step > 0) { "Slider step must be positive" }

            minProgress = step * round(minProgress / step)
            maxProgress = step * round(maxProgress / step)
            require(minProgress <= maxProgress) { "Slider max progress must be greater or equal to min progress." }

            progress = (step * round(progress / step)).coerceIn(minProgress, maxProgress)

            boundsValidated = true
        }

        // Update drawing positions for sprites.
        if (posInvalid) {
            val scale = style.scale
            val trackEmpty = style.trackEmpty
            val thumb = style.thumb

            percentProgress = (realProgress - minProgress) / (maxProgress - minProgress)
            trackX = x + (thumb.minWidth - trackEmpty.minHeight) * scale / 2
            trackY = y + (thumb.minHeight - trackEmpty.minHeight) * scale / 2
            trackWidth = width + (trackEmpty.minHeight - thumb.minWidth) * scale
            trackFilledWidth = trackWidth * percentProgress
            thumbX = x + (width - thumb.minWidth * scale) * percentProgress
        }
    }

    override fun getMinWidth() = style.thumb.minWidth * style.scale + 20f

    override fun getPrefWidth() = 0f

    override fun getPrefHeight() = style.thumb.minHeight * style.scale

    /** Animate the change of the slider progress to a new [progress] value */
    fun slideTo(progress: Float) {
        val startProgress = realProgress
        this.progress = progress
        realProgress = startProgress

        this += object : Action() {
            private var elapsed = 0f
            override fun act(delta: Float): Boolean {
                elapsed += delta
                realProgress = SLIDE_INTERPOLATION.applyBounded(
                        startProgress, progress, elapsed / SLIDE_DURATION)
                return elapsed >= SLIDE_DURATION
            }
        }
    }

    /** Returns true if point at ([x]; [y]) is on the slider thumb. (in actor coordinates) */
    private fun isPointInThumb(x: Float, y: Float): Boolean {
        val radius = style.thumb.minWidth * style.scale / 2
        return Vector2.len((trackFilledWidth + radius) - x, radius - y) <= radius
    }

    private fun updateHoveredState(x: Float, y: Float) {
        if (enabled) {
            val newHovered = isPointInThumb(x, y)
            if (hovered != newHovered) {
                hovered = newHovered
                hoverElapsed = if (hovered) 0f else HOVER_FADE_DURATION * hoverAlpha
            }
        }
    }


    class SliderStyle {
        lateinit var trackEmpty: Drawable
        lateinit var trackFilled: Drawable
        lateinit var trackFilledDisabled: Drawable
        lateinit var thumb: Drawable
        lateinit var thumbHoverOverlay: Drawable
        var scale = 0f
    }

    companion object {
        /** The duration of the press fade. */
        private const val PRESS_FADE_DURATION = 0.3f

        /** The duration of the hover fade. */
        private const val HOVER_FADE_DURATION = 0.3f

        /** The duration of the animation when the thumb slides to a new position. */
        private const val SLIDE_DURATION = 0.3f

        /** Maximum distance that touch can be dragged to allow slide when clicking on track. */
        private const val MAX_SLIDE_DRAG_DISTANCE = 10f

        private val PRESS_IN_INTERPOLATION: Interpolation = Interpolation.smooth
        private val PRESS_OUT_INTERPOLATION: Interpolation = Interpolation.smooth
        private val HOVER_IN_INTERPOLATION: Interpolation = Interpolation.pow2Out
        private val HOVER_OUT_INTERPOLATION: Interpolation = Interpolation.smooth
        private val SLIDE_INTERPOLATION: Interpolation = Interpolation.smooth
    }

}