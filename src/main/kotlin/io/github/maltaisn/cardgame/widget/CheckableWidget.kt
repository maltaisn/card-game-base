package io.github.maltaisn.cardgame.widget

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.ui.Skin


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
                check(value, true)
            }
        }

    /**
     * The listener called when the checked state is changed.
     * The listener is also called when the state is changed programatically.
     */
    var checkListener: ((Boolean) -> Unit)? = null

    protected open var checkAlpha = 0f
    protected var checkAction: CheckAction? = null
        set(value) {
            if (field != null) removeAction(field)
            field = value
            if (value != null) addAction(value)
        }


    override fun clearActions() {
        super.clearActions()
        checkAction = null
    }

    /** Change the checked state with or without animation. */
    fun check(checked: Boolean, animate: Boolean) {
        this.checked = checked
        if (!animate) {
            checkAction = null
            checkAlpha = if (checked) 1f else 0f
        } else {
            addCheckAction()
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