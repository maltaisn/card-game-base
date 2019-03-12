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

package io.github.maltaisn.cardgame.widget

import com.badlogic.gdx.Gdx
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
import io.github.maltaisn.cardgame.withinBounds


/**
 * A button in a menu, with an optional text and icon.
 */
class MenuButton(skin: Skin, val style: MenuButtonStyle, text: String? = null, icon: Drawable? = null) : Table(skin) {

    /** The button text, or `null` for none. */
    var text: CharSequence?
        set(value) {
            labelView.isVisible = (value != null)
            labelView.setText(value)
            updateIconViewPadding()
            invalidateHierarchy()
        }
        get() = labelView.text

    /** The button icon, or `null` for none. */
    var icon: Drawable?
        set(value) {
            iconView.isVisible = (value != null)
            iconView.drawable = value
            updateIconViewPadding()
            updateIconSize()
            invalidateHierarchy()
        }
        get() = iconView.drawable

    /** The icon size (width actually), in pixels. */
    var iconSize = 32f
        set(value) {
            field = value
            updateIconSize()
            invalidateHierarchy()
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

    /** Whether the button can be selected, hovered and clicked */
    var enabled = true
        set(value) {
            field = value
            if (!value) {
                selected = false
                hovered = false
                selectionAlpha = 0f
                hoverAlpha = 0f
            }
            val color = if (value) style.fontStyle.fontColor else style.disabledColor
            labelView.color.set(color)
            iconView.color.set(color)
        }

    /**
     * Click listener, called when the button is clicked. Clicks must end within the bounds.
     * The listener is not called when the button is disabled.
     */
    var clickListener: ClickListener? = null


    private val labelView: SdfLabel
    private val iconView: ShadowImage

    // Hover and selection status.
    private var selected = false
    private var hovered = false
    private var selectionElapsed = 0f
    private var hoverElapsed = 0f
    private var hoverAlpha = 0f
    private var selectionAlpha = 0f
        set(value) {
            field = value
            val color = interpolateColors(style.fontStyle.fontColor, style.selectedColor, value)
            labelView.color.set(color)
            iconView.color.set(color)
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

    constructor(skin: Skin, text: String? = null, icon: Drawable? = null) :
            this(skin, skin.get(MenuButtonStyle::class.java), text, icon)

    init {
        touchable = Touchable.enabled

        addListener(object : InputListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                if (enabled) {
                    selected = true
                    selectionElapsed = 0f
                }
                return true
            }

            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                if (enabled) {
                    selected = false
                    selectionElapsed = SELECTION_FADE_DURATION * selectionAlpha

                    if (clickListener != null && withinBounds(x, y)) {
                        clickListener?.onMenuButtonClicked(this@MenuButton)
                    }
                }
            }

            override fun enter(event: InputEvent, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
                // Pointer must be -1 because hovering can only happen on desktop.
                // Also when on touch down/up, an enter/exit event is fired, but that shouldn't stop hovering.
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

        val fontStyle = style.fontStyle
        labelView = SdfLabel(null, skin, fontStyle).apply {
            isVisible = false
            touchable = Touchable.disabled
        }
        iconView = ShadowImage().apply {
            isVisible = false
            touchable = Touchable.disabled
            setScaling(Scaling.fit)
            drawShadow = fontStyle.drawShadow
            shadowColor = fontStyle.shadowColor
        }

        pad(10f, 10f, 10f, 10f)
        updateButtonLayout()

        this.text = text
        this.icon = icon
    }

    /** Clear the children and redo the correct label and icon layout. */
    private fun updateButtonLayout() {
        clearChildren()
        when (if (iconSide == Side.NONE) anchorSide else iconSide) {
            Side.NONE, Side.TOP -> {
                add(iconView).expandX().row()
                add(labelView).expandX()
            }
            Side.BOTTOM -> {
                add(labelView).expandX().row()
                add(iconView).expandX()
            }
            Side.LEFT -> {
                add(iconView).expandY()
                add(labelView).expandY()
            }
            Side.RIGHT -> {
                add(labelView).expandY()
                add(iconView).expandY()
            }
        }
        updateIconViewPadding()
        updateIconSize()
    }

    /** If both icon and label are visible, update the margin between them. */
    private fun updateIconViewPadding() {
        val iconCell = getCell(iconView)
        if (icon != null && text != null) {
            val padding = style.iconLabelMargin
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
            getCell(iconView).size(iconSize, icon.minHeight / icon.minWidth * iconSize)
        }
    }

    override fun act(delta: Float) {
        super.act(delta)

        var renderingNeeded = false

        // Update selection alpha
        if (selected && selectionElapsed < SELECTION_FADE_DURATION) {
            selectionElapsed += delta
            selectionAlpha = SELECTION_IN_INTERPOLATION.applyBounded(selectionElapsed / SELECTION_FADE_DURATION)
            renderingNeeded = true
        } else if (!selected && selectionElapsed > 0f) {
            selectionElapsed -= delta
            selectionAlpha = SELECTION_OUT_INTERPOLATION.applyBounded(selectionElapsed / SELECTION_FADE_DURATION)
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
        tempColor.set(batch.color)

        val scale = style.backgroundScale

        // Draw background
        val background = backgroundDrawable as TransformDrawable
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha * if (enabled) 1f else 0.7f)
        background.draw(batch, x, y, 0f, 0f,
                width / scale, height / scale, scale, scale, 0f)

        // Draw hover
        if (hoverAlpha != 0f) {
            batch.setColor(color.r, color.g, color.b,
                    color.a * hoverAlpha * style.hoverMaxAlpha * parentAlpha)
            background.draw(batch, x, y, 0f, 0f,
                    width / scale, height / scale, scale, scale, 0f)
        }

        // Draw button content
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha)
        super.drawChildren(batch, parentAlpha)

        batch.setColor(tempColor.r, tempColor.g, tempColor.b, tempColor.a)
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

        /** Font style for the label */
        lateinit var fontStyle: SdfLabel.SdfLabelStyle
        /** Color of the text and icon on selected state. */
        lateinit var selectedColor: Color
        /** Color of the text and icon on disabled state. */
        lateinit var disabledColor: Color
        /** When hovered, the background drawable is redrawn on top. The maximum alpha of that redraw. */
        var hoverMaxAlpha = 0f

        /** The margin between the icon and the label view. */
        var iconLabelMargin = 0f
    }

    enum class Side {
        NONE, TOP, BOTTOM, LEFT, RIGHT
    }

    interface ClickListener {
        fun onMenuButtonClicked(button: MenuButton)
    }

    companion object {
        /** The duration of the hover fade. */
        private const val HOVER_FADE_DURATION = 0.3f

        /** The duration of the selection fade. */
        private const val SELECTION_FADE_DURATION = 0.3f

        private val SELECTION_IN_INTERPOLATION: Interpolation = Interpolation.smooth
        private val SELECTION_OUT_INTERPOLATION: Interpolation = Interpolation.smooth
        private val HOVER_IN_INTERPOLATION: Interpolation = Interpolation.pow2Out
        private val HOVER_OUT_INTERPOLATION: Interpolation = Interpolation.smooth
    }

}