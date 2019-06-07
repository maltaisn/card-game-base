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

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.scenes.scene2d.utils.TransformDrawable
import ktx.actors.alpha
import ktx.math.vec2
import ktx.style.get
import kotlin.math.max


/**
 * A popup for information or interaction shown next to any actor on the stage.
 */
class Popup(skin: Skin) : FboTable() {

    val style: PopupStyle = skin.get()

    /** Distance of the popup from the actor. */
    var distance = 5f

    /** The actor on which the popup is attached to, or `null` if not shown. */
    var actor: Actor? = null
        private set

    /** On which side on the [actor] the popup is. */
    var side = Side.CENTER
        private set

    /** Whether the popup is shown or not. Like [isVisible] but with the correct value during a transition. */
    var shown = false
        private set

    private var translateX = 0f
    private var translateY = 0f

    private var transitionAction: TransitionAction? = null
        set(value) {
            if (field != null) removeAction(field)
            field = value
            if (value != null) addAction(value)
        }

    init {
        isVisible = false
    }

    override fun validate() {
        super.validate()

        actor?.let {
            // Place the popup on the correct side and distance of its actor.
            val dx: Float
            val dy: Float
            when (side) {
                Side.CENTER -> {
                    dx = (it.width - width) / 2 - style.bodyOffsetX
                    dy = (it.height - height) / 2 - style.bodyOffsetY
                }
                Side.LEFT -> {
                    dx = -(distance + width)
                    dy = (it.height - height) / 2 - style.bodyOffsetY
                }
                Side.RIGHT -> {
                    dx = distance + it.width
                    dy = (it.height - height) / 2 - style.bodyOffsetY
                }
                Side.ABOVE -> {
                    dx = (it.width - width) / 2 - style.bodyOffsetX
                    dy = distance + it.height
                }
                Side.BELOW -> {
                    dx = (it.width - width) / 2 - style.bodyOffsetX
                    dy = -(distance + height)
                }
            }
            val actorPos = it.localToActorCoordinates(parent, vec2())
            this.x = actorPos.x + dx + translateX
            this.y = actorPos.y + dy + translateY
        }
    }

    override fun delegateDraw(batch: Batch, parentAlpha: Float) {
        batch.setColor(color.r, color.g, color.b, alpha * parentAlpha)

        // Draw the body
        val scale = style.backgroundScale
        val body = style.body as TransformDrawable
        val tipPadLeft = padLeft - body.leftWidth * scale
        val tipPadRight = padRight - body.rightWidth * scale
        val tipPadTop = padTop - body.topHeight * scale
        val tipPadBottom = padBottom - body.bottomHeight * scale
        body.draw(batch, x + tipPadLeft, y + tipPadBottom, 0f, 0f,
                (width - tipPadLeft - tipPadRight) / scale,
                (height - tipPadTop - tipPadBottom) / scale, scale, scale, 0f)

        // Draw the tip
        if (side != Side.CENTER) {
            lateinit var tip: Drawable
            var offsetX = 0f
            var offsetY = 0f
            when (side) {
                Side.LEFT -> {
                    tip = style.rightTip
                    offsetX = width - tip.minWidth * scale
                    offsetY = (style.rightTipOffsetY + style.bodyOffsetY) * scale + height / 2
                }
                Side.RIGHT -> {
                    tip = style.leftTip
                    offsetY = (style.leftTipOffsetY + style.bodyOffsetY) * scale + height / 2
                }
                Side.ABOVE -> {
                    tip = style.bottomTip
                    offsetX = (style.bottomTipOffsetX + style.bodyOffsetX) * scale + width / 2
                }
                Side.BELOW -> {
                    tip = style.topTip
                    offsetX = (style.topTipOffsetX + style.bodyOffsetX) * scale + width / 2
                    offsetY = height - tip.minHeight * scale
                }
                else -> {
                }
            }
            (tip as TransformDrawable).draw(batch, x + offsetX, y + offsetY,
                    0f, 0f, tip.minWidth, tip.minHeight, scale, scale, 0f)
        }

        super.delegateDraw(batch, parentAlpha)
    }

    override fun clearActions() {
        super.clearActions()
        transitionAction = null
    }

    /**
     * Show popup on a [side] of an [actor].
     * The popup will follow the actor if it moves.
     */
    fun show(actor: Actor, side: Side) {
        if (shown) return

        shown = true

        this.actor = actor
        this.side = side

        // Adjust the padding for the tip
        val body = style.body
        var left = body.leftWidth
        var right = body.rightWidth
        var top = body.topHeight
        var bottom = body.bottomHeight
        when (side) {
            Side.CENTER -> Unit
            Side.LEFT -> right += max(0f, style.rightTip.minWidth + style.rightTipOffsetX)
            Side.RIGHT -> left += max(0f, -style.leftTipOffsetX)
            Side.ABOVE -> bottom += max(0f, -style.bottomTipOffsetY)
            Side.BELOW -> top += max(0f, style.topTip.minHeight + style.topTipOffsetY)
        }
        val scale = style.backgroundScale
        pad(top * scale, left * scale, bottom * scale, right * scale)
        invalidateHierarchy()

        if (transitionAction == null) {
            transitionAction = TransitionAction()
        }
    }

    /**
     * Hide the popup.
     */
    fun hide() {
        if (!shown) return

        shown = false

        if (transitionAction == null) {
            transitionAction = TransitionAction()
        }
    }

    enum class Side {
        CENTER, ABOVE, BELOW, LEFT, RIGHT
    }

    /**
     * The style for a [Popup] widget. A popup is made of a stretchable body with
     * an optional tip pointing at the actor it is attached to, in any four directions.
     */
    class PopupStyle {
        lateinit var body: Drawable
        var backgroundScale = 0f
        var bodyOffsetX = 0f
        var bodyOffsetY = 0f

        lateinit var topTip: Drawable
        lateinit var bottomTip: Drawable
        lateinit var leftTip: Drawable
        lateinit var rightTip: Drawable
        var bottomTipOffsetX = 0f
        var bottomTipOffsetY = 0f
        var topTipOffsetX = 0f
        var topTipOffsetY = 0f
        var leftTipOffsetX = 0f
        var leftTipOffsetY = 0f
        var rightTipOffsetX = 0f
        var rightTipOffsetY = 0f
    }

    private inner class TransitionAction :
            TimeAction(0.3f, Interpolation.smooth, reversed = !shown) {

        private val touchableBefore = touchable

        init {
            isVisible = true
            translateX = 0f
            translateY = 0f
            alpha = if (shown) 0f else 1f
            touchable = Touchable.disabled
            renderToFrameBuffer = true
        }

        override fun update(progress: Float) {
            reversed = !shown

            val offset = progress * TRANSITION_DISTANCE
            when (side) {
                Side.CENTER -> Unit
                Side.LEFT -> translateX = -offset
                Side.RIGHT -> translateX = offset
                Side.ABOVE -> translateY = offset
                Side.BELOW -> translateY = -offset
            }
            alpha = progress
        }

        override fun end() {
            if (!shown) {
                actor = null
                side = Side.CENTER
            }
            isVisible = shown
            touchable = touchableBefore
            renderToFrameBuffer = false
            transitionAction = null
        }
    }

    companion object {
        /** The distance in pixels the popup is translated up when shown and down when hidden. */
        private const val TRANSITION_DISTANCE = 30f
    }

}