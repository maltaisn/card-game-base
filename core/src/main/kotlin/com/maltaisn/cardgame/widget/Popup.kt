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
import com.maltaisn.cardgame.widget.action.ActionDelegate
import com.maltaisn.cardgame.widget.action.TimeAction
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
    var distance = 10f

    /** The actor on which the popup is attached to, or `null` if not shown. */
    var actor: Actor? = null
        private set

    /** On which side on the [actor] the popup is, or `null` if not shown */
    var side: Side? = null
        private set

    /**
     * Whether the popup is shown or not.
     * Like [isVisible] but with the correct value during a transition.
     */
    var shown = false
        private set

    private var translateX = 0f
    private var translateY = 0f

    private var transitionAction by ActionDelegate<TransitionAction>()


    init {
        isVisible = false
    }


    override fun validate() {
        super.validate()

        actor?.let {
            // Place the popup on the correct side and distance of its actor.
            var dx = 0f
            var dy = 0f
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
            x = actorPos.x + dx + translateX
            y = actorPos.y + dy + translateY
        }
    }

    override fun drawChildren(batch: Batch, parentAlpha: Float) {
        batch.setColor(color.r, color.g, color.b, alpha * parentAlpha)

        // Draw the body
        val body = style.body
        val tipPadLeft = padLeft - body.leftWidth
        val tipPadRight = padRight - body.rightWidth
        val tipPadTop = padTop - body.topHeight
        val tipPadBottom = padBottom - body.bottomHeight
        body.draw(batch, x + tipPadLeft, y + tipPadBottom,
                (width - tipPadLeft - tipPadRight), (height - tipPadTop - tipPadBottom))

        // Draw the tip
        if (side != Side.CENTER) {
            lateinit var tip: Drawable
            var offsetX = 0f
            var offsetY = 0f
            when (side) {
                Side.LEFT -> {
                    tip = style.rightTip
                    offsetX = width - tip.minWidth
                    offsetY = (style.rightTipOffsetY + style.bodyOffsetY) + height / 2
                }
                Side.RIGHT -> {
                    tip = style.leftTip
                    offsetX = max(0f, style.leftTipOffsetX)
                    offsetY = (style.leftTipOffsetY + style.bodyOffsetY) + height / 2
                }
                Side.ABOVE -> {
                    tip = style.bottomTip
                    offsetX = (style.bottomTipOffsetX + style.bodyOffsetX) + width / 2
                    offsetY = max(0f, style.bottomTipOffsetY)
                }
                Side.BELOW -> {
                    tip = style.topTip
                    offsetX = (style.topTipOffsetX + style.bodyOffsetX) + width / 2
                    offsetY = height - tip.minHeight
                }
                else -> {
                }
            }
            tip.draw(batch, x + offsetX, y + offsetY, tip.minWidth, tip.minHeight)
        }

        super.drawChildren(batch, parentAlpha)
    }

    override fun clearActions() {
        super.clearActions()
        transitionAction?.end()
    }

    override fun getPrefWidth() = max(super.getPrefWidth(), style.body.minWidth + when (side) {
        Side.ABOVE -> style.bottomTip.minWidth
        Side.BELOW -> style.topTip.minWidth
        else -> 0f
    })

    override fun getPrefHeight() = max(super.getPrefHeight(), style.body.minHeight + when (side) {
        Side.LEFT -> style.rightTip.minHeight
        Side.RIGHT -> style.leftTip.minHeight
        else -> 0f
    })

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
        pad(top, left, bottom, right)
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
                Side.LEFT -> translateX = -offset
                Side.RIGHT -> translateX = offset
                Side.ABOVE -> translateY = offset
                Side.BELOW -> translateY = -offset
                else -> Unit
            }
            alpha = progress
        }

        override fun end() {
            if (!shown) {
                actor = null
                side = null
            }
            isVisible = shown
            touchable = touchableBefore
            renderToFrameBuffer = false
            transitionAction = null
        }
    }

    /**
     * The style for a [Popup] widget. A popup is made of a stretchable body with
     * an optional tip pointing at the actor it is attached to, in any four directions.
     */
    class PopupStyle(
            val body: Drawable,

            // The offset of the center position of the body drawable
            val bodyOffsetX: Float,
            val bodyOffsetY: Float,

            val topTip: Drawable,
            val bottomTip: Drawable,
            val leftTip: Drawable,
            val rightTip: Drawable,

            // The offset values are relative to the coordinates of the center of
            // the side of the popup image, taking body offset values into account.
            val bottomTipOffsetX: Float,
            val bottomTipOffsetY: Float,
            val topTipOffsetX: Float,
            val topTipOffsetY: Float,
            val leftTipOffsetX: Float,
            val leftTipOffsetY: Float,
            val rightTipOffsetX: Float,
            val rightTipOffsetY: Float)

    companion object {
        /** The distance in pixels the popup is translated up when shown and down when hidden. */
        private const val TRANSITION_DISTANCE = 30f
    }

}
