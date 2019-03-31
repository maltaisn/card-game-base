package io.github.maltaisn.cardgame.widget

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.Action
import io.github.maltaisn.cardgame.applyBounded


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

    private var elapsed = if (reversed) duration else 0f
    private var begun = false

    final override fun act(delta: Float): Boolean {
        if (!begun) {
            begin()
            begun = true
        }

        val oldPool = pool
        pool = null

        elapsed += if (reversed) -delta else delta

        val interpolation = if (reversed) interpolationOut else interpolationIn
        val progress = interpolation.applyBounded(elapsed / duration)
        update(progress)

        val done = !reversed && progress >= 1f || reversed && progress <= 0f
        if (done) {
            end()
        }
        pool = oldPool
        return done
    }

    /** Called the first time [act] is called. */
    open fun begin() {}

    /** Called everytime [act] is called with the interpolated progress value. */
    open fun update(progress: Float) {}

    /** Called the last time [act] is called. */
    open fun end() {}

}