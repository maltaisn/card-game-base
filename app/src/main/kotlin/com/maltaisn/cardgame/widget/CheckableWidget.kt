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
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.maltaisn.cardgame.widget.action.ActionDelegate
import com.maltaisn.cardgame.widget.action.TimeAction


abstract class CheckableWidget(skin: Skin? = null) : SelectableWidget(skin) {

    /**
     * Whether the widget is checked or not.
     * Changing this property will always result in an animation.
     */
    var checked = false
        set(value) {
            if (field != value) {
                field = value
                checkListener?.invoke(value)
                addCheckAction()
            }
        }

    /**
     * The listener called when the checked state is changed.
     * The listener is also called when the state is changed programatically.
     */
    var checkListener: ((Boolean) -> Unit)? = null

    protected open var checkAlpha = 0f
    protected var checkAction by ActionDelegate<CheckAction>()


    override fun clearActions() {
        super.clearActions()
        checkAction = null
    }

    /** Change the checked state with or without animation. */
    fun check(checked: Boolean, animate: Boolean = true) {
        this.checked = checked
        if (!animate) {
            checkAction = null
            checkAlpha = if (checked) 1f else 0f
        }
    }


    protected fun addCheckAction() {
        if (checkAction == null) {
            checkAction = CheckAction()
        }
    }


    protected inner class CheckAction : TimeAction(0.3f,
            Interpolation.smooth, reversed = !checked) {
        override fun update(progress: Float) {
            reversed = !checked
            checkAlpha = progress
        }

        override fun end() {
            checkAction = null
        }
    }

}
