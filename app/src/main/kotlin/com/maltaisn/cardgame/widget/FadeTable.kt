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
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.maltaisn.cardgame.widget.action.ActionDelegate
import com.maltaisn.cardgame.widget.action.TimeAction
import ktx.actors.alpha


/**
 * A [Table] widget group that can be faded in and out.
 */
class FadeTable(val duration: Float = DEFAULT_FADE_DURATION) : FboTable() {

    /**
     * Whether the container is shown or not.
     * Like [isVisible] but with the correct value during a transition.
     */
    var shown: Boolean
        get() = _shown
        set(value) {
            _shown = value
            _isVisible = value
            transitionAction?.end()
        }

    private var _shown = true

    private var _isVisible
        get() = isVisible
        set(value) {
            super.setVisible(value)
        }

    private var transitionAction by ActionDelegate<TimeAction>()

    init {
        isVisible = false
    }

    override fun clearActions() {
        super.clearActions()
        transitionAction?.end()
    }

    override fun setVisible(visible: Boolean) {
        shown = visible
    }

    /**
     * Fade the table to a new [shown] state.
     */
    fun fade(shown: Boolean) {
        if (this.shown == shown) return
        _shown = shown

        if (transitionAction == null) {
            transitionAction = TransitionAction()
        }
    }

    private inner class TransitionAction :
            TimeAction(duration, Interpolation.smooth, reversed = !shown) {

        init {
            _isVisible = true
            renderToFrameBuffer = true
            alpha = if (shown) 1f else 0f
        }

        override fun update(progress: Float) {
            reversed = !shown
            alpha = progress
        }

        override fun end() {
            _isVisible = shown
            alpha = 1f
            renderToFrameBuffer = false
            transitionAction = null
        }
    }

    companion object {
        const val DEFAULT_FADE_DURATION = 0.4f
    }

}
