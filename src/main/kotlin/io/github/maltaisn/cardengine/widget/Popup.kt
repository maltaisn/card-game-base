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

package io.github.maltaisn.cardengine.widget

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.Action
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.scenes.scene2d.utils.TransformDrawable
import io.github.maltaisn.cardengine.applyBounded
import ktx.actors.alpha
import ktx.actors.plusAssign
import ktx.math.vec2
import kotlin.math.max


/**
 * A popup for information or interaction shown next to any actor on the stage.
 */
class Popup(skin: Skin) : FrameBufferTable() {

    val style = skin.get(PopupStyle::class.java)

    /** Whether the popup is shown or not. Like [isVisible] but with the correct value during a transition. */
    var shown = false
        private set

    /** Distance of the popup from the actor. */
    var distance = 10f

    /** The actor on which the popup is attached to, or `null` if not shown. */
    var actor: Actor? = null
        private set

    /** On which side on the [actor] the popup is. */
    var side = Side.CENTER
        private set


    private var offsetX = 0f
    private var offsetY = 0f

    init {
        isVisible = false
        renderToFrameBuffer = false
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
            this.x = actorPos.x + dx + offsetX
            this.y = actorPos.y + dy + offsetY
        }
    }

    override fun drawChildren(batch: Batch, parentAlpha: Float) {
        // Draw the body
        val colorBefore = batch.color.cpy()
        val scale = style.scale
        val body = style.body as TransformDrawable
        val tipPadLeft = padLeft - body.leftWidth * scale
        val tipPadRight = padRight - body.rightWidth * scale
        val tipPadTop = padTop - body.topHeight * scale
        val tipPadBottom = padBottom - body.bottomHeight * scale
        batch.setColor(1f, 1f, 1f, 1f)
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

        batch.color = colorBefore

        super.drawChildren(batch, parentAlpha)
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
        val scale = style.scale
        pad(top * scale, left * scale, bottom * scale, right * scale)
        invalidateHierarchy()

        if (actions.isEmpty) {
            this += TransitionAction()
        }
    }

    /**
     * Hide the popup.
     */
    fun hide() {
        if (!shown) return

        shown = false

        if (actions.isEmpty) {
            this += TransitionAction()
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
        var scale = 0f
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

    private inner class TransitionAction : Action() {
        private var elapsed = if (shown) 0f else TRANSITION_DURATION

        init {
            isVisible = true
            offsetX = 0f
            offsetY = 0f
            alpha = if (shown) 1f else 0f
            renderToFrameBuffer = true
        }

        override fun act(delta: Float): Boolean {
            elapsed += if (shown) delta else -delta
            val progress = TRANSITION_INTERPOLATION.applyBounded(
                    elapsed / TRANSITION_DURATION)
            val offset = (1 - progress) * TRANSITION_DISTANCE
            when (side) {
                Side.CENTER -> Unit
                Side.LEFT -> offsetX = offset
                Side.RIGHT -> offsetX = -offset
                Side.ABOVE -> offsetY = -offset
                Side.BELOW -> offsetY = offset
            }
            alpha = progress

            if (shown && progress >= 1 || !shown && progress <= 0) {
                if (!shown) {
                    isVisible = false
                    actor = null
                    side = Side.CENTER
                }
                renderToFrameBuffer = false
                return true
            }
            return false
        }
    }

    companion object {
        /** The duration a popup's show and hide transition. */
        private const val TRANSITION_DURATION = 0.3f

        /** The distance in pixels the popup is translated up when shown and down when hidden. */
        private const val TRANSITION_DISTANCE = 30f

        private val TRANSITION_INTERPOLATION: Interpolation = Interpolation.smooth
    }

}