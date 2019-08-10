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

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.maltaisn.cardgame.widget.action.ActionDelegate
import com.maltaisn.cardgame.widget.action.TimeAction
import ktx.actors.alpha
import ktx.math.vec2
import ktx.style.get


class DealerChip(skin: Skin) : Image(skin.get<DealerChipStyle>().drawable, Scaling.fit) {

    /** Distance of the chip from the actor. */
    var distance = 20f

    /** The actor on which the chip is attached to, or `null` if not shown. */
    var actor: Actor? = null
        private set

    /** On which side on the [actor] the chip is. */
    var side = Align.center

    /**
     * Whether the chip is shown or not.
     * Like [isVisible] but with the correct value during a transition.
     */
    var shown = false
        private set

    private var transitionAction by ActionDelegate<TimeAction>()


    init {
        isVisible = false
        setSize(prefWidth, prefHeight)
    }


    override fun validate() {
        super.validate()

        if (actor != null && transitionAction !is MoveAction) {
            // Place the chip on the correct side and distance of its actor.
            val pos = getChipEndPosition()
            setPosition(pos.x, pos.y)
        }
    }

    override fun clearActions() {
        super.clearActions()
        transitionAction?.end()
    }

    override fun getPrefWidth() = 96f

    override fun getPrefHeight() = 96f

    /**
     * Show chip on a [side] of an [actor].
     * The chip will follow the actor if it moves.
     */
    fun show(actor: Actor, side: Int) {
        if (shown) return

        shown = true

        this.actor = actor
        this.side = side
        invalidateHierarchy()

        if (transitionAction !is FadeAction) {
            transitionAction?.end()
            transitionAction = FadeAction()
        }
    }

    /**
     * Hide the chip.
     */
    fun hide() {
        if (!shown) return

        shown = false

        if (transitionAction !is FadeAction) {
            transitionAction?.end()
            transitionAction = FadeAction()
        }
    }

    /**
     * Move the chip from its current position to the [side] of another [actor].
     */
    fun moveTo(actor: Actor, side: Int) {
        check(shown) { "Chip must be shown before being moved." }

        transitionAction?.end()

        this.actor = actor
        this.side = side

        transitionAction = MoveAction(x, y)
    }


    private fun getChipEndPosition(): Vector2 {
        val actor = actor!!
        val pos = actor.localToActorCoordinates(parent, vec2())
        pos.x += when {
            Align.isLeft(side) -> -width - distance
            Align.isRight(side) -> actor.width + distance
            else -> (actor.width - width) / 2
        }
        pos.y += when {
            Align.isBottom(side) -> -height - distance
            Align.isTop(side) -> actor.height + distance
            else -> (actor.height - height) / 2
        }
        return pos
    }


    private inner class FadeAction :
            TimeAction(FADE_DURATION, Interpolation.smooth, reversed = !shown) {

        init {
            isVisible = true
            alpha = if (shown) 0f else 1f
        }

        override fun update(progress: Float) {
            reversed = !shown
            alpha = progress
        }

        override fun end() {
            if (!shown) {
                actor = null
                side = Align.center
            }
            isVisible = shown
            transitionAction = null
        }
    }

    private inner class MoveAction(val startX: Float, val startY: Float) :
            TimeAction(MOVE_DURATION, Interpolation.smooth) {

        val endPos = getChipEndPosition()

        override fun update(progress: Float) {
            x = startX + (endPos.x - startX) * progress
            y = startY + (endPos.y - startY) * progress
        }

        override fun end() {
            transitionAction = null
        }

    }


    class DealerChipStyle {
        lateinit var drawable: Drawable
    }

    companion object {
        const val FADE_DURATION = 0.3f
        const val MOVE_DURATION = 0.4f
    }

}
