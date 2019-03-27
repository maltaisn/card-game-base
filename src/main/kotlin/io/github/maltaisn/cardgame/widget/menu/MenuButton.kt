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

package io.github.maltaisn.cardgame.widget.menu

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.scenes.scene2d.utils.TransformDrawable
import com.badlogic.gdx.utils.Scaling
import io.github.maltaisn.cardgame.applyBounded
import io.github.maltaisn.cardgame.defaultSize
import io.github.maltaisn.cardgame.widget.SdfLabel
import io.github.maltaisn.cardgame.widget.ShadowImage
import io.github.maltaisn.cardgame.withinBounds
import ktx.actors.alpha


/**
 * A button for a menu, with an optional title and icon.
 */
class MenuButton(skin: Skin,
                 val style: MenuButtonStyle,
                 val fontStyle: SdfLabel.FontStyle,
                 title: CharSequence? = null, icon: Drawable? = null) : Table() {

    /** The button title, or `null` for none. */
    var title: CharSequence?
        set(value) {
            val titleShown = (value != null && value.isNotEmpty())
            titleLabel.isVisible = titleShown
            titleLabel.setText(value)
            updateIconViewPadding()

            // Change the size of the cell according to its visibility
            // since no text in a label still take a certain height.
            val titleCell = getCell(titleLabel)
            if (titleShown) {
                titleCell.defaultSize()
            } else {
                titleCell.size(0f, 0f)
            }
        }
        get() = titleLabel.text

    /** The button icon, or `null` for none. */
    var icon: Drawable?
        set(value) {
            iconImage.isVisible = (value != null)
            iconImage.drawable = value
            updateIconViewPadding()
            updateIconSize()
        }
        get() = iconImage.drawable

    /** The icon size (with actually), in pixels. */
    var iconSize = 32f
        set(value) {
            field = value
            updateIconSize()
        }

    /** The side on which this button is anchored. There will be no rounded corners on this side. */
    var anchorSide = Side.NONE
        set(value) {
            if (field == value) return
            field = value
            if (iconSide == Side.NONE) {
                updateButtonLayout()
            }
        }

    /**
     * The side on which the icon is, relative to the label.
     * If value is [Side.NONE], the icon will be on the same side as the button is anchored.
     * And if the button isn't anchored, the icon will be shown on top of the label.
     */
    var iconSide = Side.NONE
        set(value) {
            if (field == value) return
            field = value
            updateButtonLayout()
        }

    /** Whether the button can be pressed, hovered and clicked */
    var enabled = true
        set(value) {
            field = value
            if (!value) {
                pressed = false
                hovered = false
                pressAlpha = 0f
                hoverAlpha = 0f
            }
            val color = if (value) fontStyle.fontColor else style.disabledColor
            titleLabel.color.set(color)
            iconImage.color.set(color)
        }

    /**
     * Click listener, called when the button is clicked. Clicks must end within the bounds.
     * The listener is not called when the button is disabled.
     */
    var clickListener: ((btn: MenuButton) -> Unit)? = null


    private val titleLabel: SdfLabel
    private val iconImage: ShadowImage

    var checked = false
    private var checkedElapsed = 0f
    private var checkedAlpha = 0f

    private var hovered = false
    private var hoverElapsed = 0f
    private var hoverAlpha = 0f

    private var pressed = false
    private var pressElapsed = 0f
    private var pressAlpha = 0f
        set(value) {
            field = value
            val color = interpolateColors(fontStyle.fontColor, style.selectedColor, value)
            titleLabel.color.set(color)
            iconImage.color.set(color)
        }

    private val tempColor = Color()

    private val backgroundDrawable: Drawable
        get() = when (anchorSide) {
            Side.NONE -> style.backgroundCenter
            Side.TOP -> style.backgroundTop
            Side.BOTTOM -> style.backgroundBottom
            Side.LEFT -> style.backgroundLeft
            Side.RIGHT -> style.backgroundRight
        }

    constructor(skin: Skin, fontStyle: SdfLabel.FontStyle,
                text: CharSequence? = null, icon: Drawable? = null) :
            this(skin, skin[MenuButtonStyle::class.java], fontStyle, text, icon)

    init {
        touchable = Touchable.enabled

        addListener(object : InputListener() {
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                if (enabled && button == Input.Buttons.LEFT) {
                    pressed = true
                    pressElapsed = 0f
                }
                return true
            }

            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                if (enabled && button == Input.Buttons.LEFT) {
                    pressed = false
                    pressElapsed = PRESS_FADE_DURATION * pressAlpha

                    if (clickListener != null && withinBounds(x, y)) {
                        clickListener!!(this@MenuButton)
                    }
                }
            }

            override fun enter(event: InputEvent, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
                if (enabled && pointer == -1) {
                    hovered = true
                    hoverElapsed = 0f
                }
            }

            override fun exit(event: InputEvent, x: Float, y: Float, pointer: Int, toActor: Actor?) {
                if (enabled && pointer == -1) {
                    hovered = false
                    hoverElapsed = HOVER_FADE_DURATION * hoverAlpha
                }
            }
        })

        titleLabel = SdfLabel(null, skin, fontStyle).apply {
            isVisible = false
            touchable = Touchable.disabled
        }
        iconImage = ShadowImage().apply {
            isVisible = false
            touchable = Touchable.disabled
            setScaling(Scaling.fit)
            drawShadow = fontStyle.drawShadow
            shadowColor = fontStyle.shadowColor
        }

        pad(10f, 10f, 10f, 10f)
        updateButtonLayout()

        this.title = title
        this.icon = icon
    }

    /** Clear the children and redo the correct label and icon layout. */
    private fun updateButtonLayout() {
        clearChildren()
        when (if (iconSide == Side.NONE) anchorSide else iconSide) {
            Side.NONE, Side.TOP -> {
                add(iconImage).expandX().row()
                add(titleLabel).expandX()
            }
            Side.BOTTOM -> {
                add(titleLabel).expandX().row()
                add(iconImage).expandX()
            }
            Side.LEFT -> {
                add(iconImage).expandY()
                add(titleLabel).expandY()
            }
            Side.RIGHT -> {
                add(titleLabel).expandY()
                add(iconImage).expandY()
            }
        }
        updateIconViewPadding()
        updateIconSize()
    }

    /** If both icon and label are visible, update the margin between them. */
    private fun updateIconViewPadding() {
        val iconCell = getCell(iconImage)
        if (icon != null && title?.isEmpty() != true) {
            val padding = style.iconTitleMargin
            when (if (iconSide == Side.NONE) anchorSide else iconSide) {
                Side.NONE, Side.TOP -> iconCell.padBottom(padding)
                Side.BOTTOM -> iconCell.padTop(padding)
                Side.LEFT -> iconCell.padRight(padding)
                Side.RIGHT -> iconCell.padLeft(padding)
            }
        } else {
            iconCell.pad(0f)
        }
    }

    private fun updateIconSize() {
        val icon = icon
        if (icon != null) {
            getCell(iconImage).size(iconSize, icon.minHeight / icon.minWidth * iconSize)
            invalidateHierarchy()
        }
    }

    override fun layout() {
        super.layout()

        /*
        transitionAction?.let {
            it.topStartY = topRow.y
            it.bottomStartY = bottomRow.y
        }
        */
    }

    override fun act(delta: Float) {
        super.act(delta)

        var renderingNeeded = false

        // Update check alpha
        if (checked && checkedElapsed < CHECK_FADE_DURATION) {
            checkedElapsed += delta
            checkedAlpha = CHECK_IN_INTERPOLATION.applyBounded(checkedElapsed / CHECK_FADE_DURATION)
            renderingNeeded = true
        } else if (!checked && checkedElapsed > 0f) {
            checkedElapsed -= delta
            checkedAlpha = CHECK_OUT_INTERPOLATION.applyBounded(checkedElapsed / CHECK_FADE_DURATION)
            renderingNeeded = true
        }

        // Update press alpha
        if (pressed && pressElapsed < PRESS_FADE_DURATION) {
            pressElapsed += delta
            pressAlpha = PRESS_IN_INTERPOLATION.applyBounded(pressElapsed / PRESS_FADE_DURATION)
            renderingNeeded = true
        } else if (!pressed && pressElapsed > 0f) {
            pressElapsed -= delta
            pressAlpha = PRESS_OUT_INTERPOLATION.applyBounded(pressElapsed / PRESS_FADE_DURATION)
            renderingNeeded = true
        }

        // Update hover alpha
        if (hovered && hoverElapsed < HOVER_FADE_DURATION) {
            hoverElapsed += delta
            hoverAlpha = HOVER_IN_INTERPOLATION.applyBounded(hoverElapsed / HOVER_FADE_DURATION)
            renderingNeeded = true
        } else if (!hovered && hoverElapsed > 0f) {
            hoverElapsed -= delta
            hoverAlpha = HOVER_OUT_INTERPOLATION.applyBounded(hoverElapsed / HOVER_FADE_DURATION)
            renderingNeeded = true
        }

        if (renderingNeeded) {
            Gdx.graphics.requestRendering()
        }
    }

    override fun drawChildren(batch: Batch, parentAlpha: Float) {
        val scale = style.backgroundScale

        // Draw background
        // Alpha depends on hovered, checked and enabled states
        val alpha = (0.2f + 0.2f * checkedAlpha + 0.1f * hoverAlpha) *
                alpha * parentAlpha * if (enabled) 1f else 0.5f
        val background = backgroundDrawable as TransformDrawable
        batch.setColor(color.r, color.g, color.b, alpha)
        background.draw(batch, x, y, 0f, 0f,
                width / scale, height / scale, scale, scale, 0f)

        // Draw button content
        batch.setColor(color.r, color.g, color.b, alpha * parentAlpha)
        super.drawChildren(batch, parentAlpha)
    }

    private fun interpolateColors(start: Color, end: Color, percent: Float): Color {
        if (percent <= 0f) return start
        if (percent >= 1f) return end

        val r = start.r + (end.r - start.r) * percent
        val g = start.g + (end.g - start.g) * percent
        val b = start.b + (end.b - start.b) * percent
        val a = start.a + (end.a - start.a) * percent
        return tempColor.set(r, g, b, a)
    }

    class MenuButtonStyle {
        // Background drawables for each side
        lateinit var backgroundCenter: Drawable
        lateinit var backgroundTop: Drawable
        lateinit var backgroundBottom: Drawable
        lateinit var backgroundLeft: Drawable
        lateinit var backgroundRight: Drawable

        /** At what scale the background drawable is drawn. */
        var backgroundScale = 0f

        /** Color of the title and icon on pressed state. */
        lateinit var selectedColor: Color
        /** Color of the title and icon on disabled state. */
        lateinit var disabledColor: Color

        /** The margin between the icon and the title. */
        var iconTitleMargin = 0f
    }

    enum class Side {
        NONE, TOP, BOTTOM, LEFT, RIGHT
    }

    companion object {
        /** The duration of the check fade. */
        private const val CHECK_FADE_DURATION = 0.3f

        /** The duration of the hover fade. */
        private const val HOVER_FADE_DURATION = 0.3f

        /** The duration of the press fade. */
        private const val PRESS_FADE_DURATION = 0.3f

        private val CHECK_IN_INTERPOLATION: Interpolation = Interpolation.smooth
        private val CHECK_OUT_INTERPOLATION: Interpolation = Interpolation.smooth
        private val HOVER_IN_INTERPOLATION: Interpolation = Interpolation.pow2Out
        private val HOVER_OUT_INTERPOLATION: Interpolation = Interpolation.smooth
        private val PRESS_IN_INTERPOLATION: Interpolation = Interpolation.smooth
        private val PRESS_OUT_INTERPOLATION: Interpolation = Interpolation.smooth
    }

}