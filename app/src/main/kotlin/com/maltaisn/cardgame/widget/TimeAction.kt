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
import com.badlogic.gdx.scenes.scene2d.Action


/**
 * A action that transitions over time using a progress value.
 * @property duration The action duration in seconds.
 * @property interpolationIn The interpolation when not reversed, linear by default.
 * @property interpolationOut The interpolation when reversed, same as in by default.
 * @property reversed Whether the action is reversed, progress will decrease instead of increasing.
 */
abstract class TimeAction(var duration: Float,
                          var interpolationIn: Interpolation = Interpolation.linear,
                          var interpolationOut: Interpolation = interpolationIn,
                          var reversed: Boolean = false) : Action() {

    var elapsed = if (reversed) duration / SPEED_MULTIPLIER else 0f

    /** The current interpolation used, depends on [reversed]. */
    val interpolation: Interpolation
        get() = if (reversed) interpolationOut else interpolationIn

    private var begun = false

    final override fun act(delta: Float): Boolean {
        if (!begun) {
            begin()
            begun = true
        }

        val oldPool = pool
        pool = null

        elapsed += if (reversed) -delta else delta

        val progress = interpolation.applyBounded(elapsed * SPEED_MULTIPLIER / duration)
        update(progress)

        val done = !reversed && progress >= 1f || reversed && progress <= 0f
        if (done) {
            end()
        }
        pool = oldPool
        return done
    }

    /** Called the first time [act] is called. */
    open fun begin() = Unit

    /** Called everytime [act] is called with the interpolated progress value. */
    open fun update(progress: Float) = Unit

    /** Called the last time [act] is called. */
    open fun end() = Unit

    companion object {
        /**
         * Global speed modifier applied on all time actions.
         * Can be used for debug purposes to see animations better.
         * Higher values result in faster actions.
         */
        var SPEED_MULTIPLIER = 1f
    }

}

/** Apply an interpolation to an [alpha] value, returning a value between [start] and [end]. */
fun Interpolation.applyBounded(alpha: Float, start: Float = 0f, end: Float = 1f) =
        start + (end - start) * apply(alpha.coerceIn(0f, 1f)).coerceIn(0f, 1f)