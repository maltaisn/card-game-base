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
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.scenes.scene2d.utils.TransformDrawable
import io.github.maltaisn.cardgame.withinBounds
import ktx.math.vec2
import kotlin.math.round


/**
 * A slider widget that can be dragged to change its progress.
 */
class Slider(val style: SliderStyle) : SelectableWidget() {

    /**
     * The slider progress. Will be rounded to a multiple of [step] within min and max,
     * so it's important to set it after min, max and step values.
     */
    var progress = 0f
        set(value) {
            if (field == value) return

            field = (step * round(value / step)).coerceIn(minProgress, maxProgress)
            realProgress = field

            slideAction = null

            changeListener?.invoke(field)

            Gdx.graphics.requestRendering()
        }

    /**
     * The slider minimum progress. Must be less or equal to
     * [maxProgress] and will be rounded to a multiple of [step].
     */
    var minProgress = 0f
        set(value) {
            if (field == value) return
            field = value
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
            boundsValidated = false
        }

    /** The amount by which the value is incremented, must be positive. */
    var step = 1f
        set(value) {
            if (field == value) return
            field = value
            boundsValidated = false
        }

    /**
     * The listener called when the slider progress is changed.
     * The listener is also called when the progress is changed programatically.
     */
    var changeListener: ((Float) -> Unit)? = null


    private var boundsValidated = false

    private var sliderPressed = false

    private var slideAction: Action? = null
        set(value) {
            if (field != null) removeAction(field)
            field = value
            if (value != null) addAction(value)
        }


    private var realProgress = 0f
    private var thumbX = 0f
    private var trackWidth = 0f
    private var trackFilledWidth = 0f


    constructor(skin: Skin, styleName: String = "default") :
            this(skin[styleName, SliderStyle::class.java])


    init {
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
                        addPressAction()
                        pressOffset = x - trackFilledWidth
                        event.stop()
                    }
                    return true
                }
                return false
            }

            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                if (enabled && button == Input.Buttons.LEFT) {
                    if (pressed) {
                        pressed = false
                        addPressAction()

                    } else if (sliderPressed && withinBounds(x, y) &&
                            Vector2.len(event.stageX - pressStagePos.x,
                                    event.stageY - pressStagePos.y) < MAX_SLIDE_DRAG_DISTANCE) {
                        // To slide, touch must start and end within bounds and touch must not have been dragged too much.
                        pressOffset = style.thumb.minWidth * style.scale / 2
                        slideTo(computeProgressForX(x))
                    }
                    sliderPressed = false
                }
            }

            override fun touchDragged(event: InputEvent, x: Float, y: Float, pointer: Int) {
                if (enabled) {
                    updateHoveredState(x, y)

                    if (pressed) {
                        progress = computeProgressForX(x)
                    }
                }
            }

            override fun mouseMoved(event: InputEvent, x: Float, y: Float): Boolean {
                updateHoveredState(x, y)
                return false
            }

            override fun exit(event: InputEvent, x: Float, y: Float, pointer: Int, toActor: Actor?) {
                updateHoveredState(x, y)
            }

            /** Returns the progress for mouse position at [x]. */
            private fun computeProgressForX(x: Float): Float {
                val newProgress = (x - pressOffset) / trackWidth * (maxProgress - minProgress) + minProgress
                return (step * round(newProgress / step)).coerceIn(minProgress, maxProgress)
            }

            /** Returns true if point at ([x]; [y]) is on the slider thumb. (in actor coordinates) */
            private fun isPointInThumb(x: Float, y: Float): Boolean {
                val radius = style.thumb.minWidth * style.scale / 2
                return Vector2.len(thumbX + radius - x, radius - y) <= radius
            }

            private fun updateHoveredState(x: Float, y: Float) {
                if (enabled) {
                    val newHovered = isPointInThumb(x, y)
                    if (hovered != newHovered) {
                        hovered = newHovered
                        addHoverAction()
                    }
                }
            }
        })
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        super.draw(batch, parentAlpha)

        val scale = style.scale

        val trackEmpty = style.trackEmpty as TransformDrawable
        val trackFilled = (if (enabled) style.trackFilled else style.trackFilledDisabled) as TransformDrawable
        val thumb = style.thumb as TransformDrawable
        val thumbHover = style.thumbHoverOverlay as TransformDrawable

        val percentProgress = (realProgress - minProgress) / (maxProgress - minProgress)
        val trackX = (thumb.minWidth - trackEmpty.minHeight) * scale / 2
        val trackY = (thumb.minHeight - trackEmpty.minHeight) * scale / 2
        trackWidth = width + (trackEmpty.minHeight - thumb.minWidth) * scale
        trackFilledWidth = trackWidth * percentProgress
        thumbX = (width - thumb.minWidth * scale) * percentProgress

        // Draw track, filled on the left side of the thumb and empty on the right side
        batch.setColor(color.r, color.g, color.b, parentAlpha)
        trackEmpty.draw(batch, x + trackX + trackFilledWidth, y + trackY, 0f, 0f,
                (trackWidth - trackFilledWidth) / scale, trackEmpty.minHeight, scale, scale, 0f)
        trackFilled.draw(batch, x + trackX, y + trackY, 0f, 0f, trackFilledWidth / scale,
                trackFilled.minHeight, scale, scale, 0f)

        // Draw thumb
        batch.setColor(color.r, color.g, color.b, parentAlpha)
        thumb.draw(batch, x + thumbX, y, 0f, 0f, thumb.minWidth,
                thumb.minHeight, scale, scale, 0f)

        // Draw thumb hover/press/disabled overlay
        batch.setColor(color.r, color.g, color.b, parentAlpha *
                (hoverAlpha + pressAlpha) * 0.1f + if (enabled) 0f else 0.2f)
        thumbHover.draw(batch, x + thumbX, y, 0f, 0f, thumbHover.minWidth,
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
    }

    override fun getMinWidth() = style.thumb.minWidth * style.scale + 20f

    override fun getPrefWidth() = 0f

    override fun getPrefHeight() = style.thumb.minHeight * style.scale

    /**
     * Animate the change of the slider progress to a new [progress] value.
     */
    fun slideTo(newProgress: Float) {
        val startProgress = realProgress
        this.progress = newProgress
        realProgress = startProgress

        slideAction = object : TimeAction(0.3f, Interpolation.smooth) {
            override fun update(progress: Float) {
                realProgress = startProgress + (newProgress - startProgress) * progress
            }

            override fun end() {
                slideAction = null
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
        /** Maximum distance that touch can be dragged to allow slide when clicking on track. */
        private const val MAX_SLIDE_DRAG_DISTANCE = 10f
    }

}