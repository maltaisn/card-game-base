package io.github.maltaisn.cardgame.widget.menu

import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Container
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.utils.Pool
import io.github.maltaisn.cardgame.widget.SdfLabel
import io.github.maltaisn.cardgame.widget.TimeAction


class MenuDrawerListItem(skin: Skin) : Container<SdfLabel>(), Pool.Poolable {

    /** The item text. */
    var text: CharSequence?
        set(value) = label.setText(value)
        get() = label.text

    /**
     * Whether the item is checked or not.
     * Changing this property will always result in an animation.
     */
    var checked = false
        set(value) {
            if (field != value) {
                field = value
                check(value, true)
            }
        }

    private var checkAlpha = 0f
    private var checkAction: CheckAction? = null

    private var hovered = false
    private var hoverAlpha = 0f
    private var hoverAction: HoverAction? = null

    private var pressed = false
    private var pressAlpha = 0f
    private var pressAction: PressAction? = null

    private val itemStyle = skin[DrawerListItemStyle::class.java]
    private val label = SdfLabel(null, skin, itemStyle.fontStyle)

    private val inputListener = object : InputListener() {
        override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
            if (button == Input.Buttons.LEFT) {
                pressed = true
                addPressAction()
                return true
            }
            return false
        }

        override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
            if (button == Input.Buttons.LEFT) {
                pressed = false
                addPressAction()
            }
        }

        override fun enter(event: InputEvent, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
            if (pointer == -1) {
                hovered = true
                addHoverAction()
            }
        }

        override fun exit(event: InputEvent, x: Float, y: Float, pointer: Int, toActor: Actor?) {
            if (pointer == -1) {
                hovered = false
                addHoverAction()
            }
        }

        private fun addHoverAction() {
            if (hoverAction == null) {
                hoverAction = HoverAction()
                addAction(hoverAction)
            }
        }

        private fun addPressAction() {
            if (pressAction == null) {
                pressAction = PressAction()
                addAction(pressAction)
            }
        }
    }

    init {
        label.setWrap(true)
        label.touchable = Touchable.disabled

        actor = label
        touchable = Touchable.enabled
        fill().pad(15f, 30f, 15f, 30f)

        addListener(inputListener)
    }

    override fun reset() {
        text = null
        check(false, false)
        hoverAction = null
        hoverAlpha = 0f
        pressAction = null
        pressAlpha = 0f
        clearActions()
        clearListeners()
        addListener(inputListener)
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        super.draw(batch, parentAlpha)

        // Draw selection/hover overlay
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha *
                (checkAlpha * 0.2f + pressAlpha * 0.1f + hoverAlpha * 0.1f))
        itemStyle.selectionOverlay.draw(batch, x, y, width, height)
    }

    /** Change the checked state with or without animation. */
    fun check(checked: Boolean, animate: Boolean) {
        this.checked = checked
        if (!animate) {
            removeAction(checkAction)
            checkAction = null
            checkAlpha = if (checked) 1f else 0f
        } else if (checkAction == null) {
            checkAction = CheckAction()
            addAction(checkAction)
        }
    }

    private inner class HoverAction : TimeAction(0.3f,
            Interpolation.smooth, reversed = !hovered) {
        override fun update(progress: Float) {
            reversed = !hovered
            hoverAlpha = progress
        }

        override fun end() {
            hoverAction = null
        }
    }

    private inner class PressAction : TimeAction(0.3f,
            Interpolation.smooth, reversed = !pressed) {
        override fun update(progress: Float) {
            reversed = !pressed
            pressAlpha = progress
        }

        override fun end() {
            pressAction = null
        }
    }

    private inner class CheckAction : TimeAction(0.3f,
            Interpolation.smooth, reversed = !checked) {
        override fun update(progress: Float) {
            reversed = !checked
            checkAlpha = progress
        }

        override fun end() {
            checkAction = null
        }
    }


    class DrawerListItemStyle {
        lateinit var fontStyle: SdfLabel.FontStyle
        lateinit var selectionOverlay: Drawable
    }

}