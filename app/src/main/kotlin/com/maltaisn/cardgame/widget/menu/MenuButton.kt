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

package com.maltaisn.cardgame.widget.menu

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.utils.Scaling
import com.maltaisn.cardgame.defaultSize
import com.maltaisn.cardgame.widget.CheckableWidget
import com.maltaisn.cardgame.widget.FontStyle
import com.maltaisn.cardgame.widget.SdfLabel
import com.maltaisn.cardgame.widget.ShadowImage
import ktx.actors.alpha
import ktx.style.get


/**
 * A button for a menu, with an optional title and icon.
 */
class MenuButton(skin: Skin,
                 val fontStyle: FontStyle,
                 title: CharSequence? = null, icon: Drawable? = null,
                 val style: MenuButtonStyle = skin.get()) : CheckableWidget() {

    /** The button title, or `null` for none. */
    var title: CharSequence?
        get() = titleLabel.text
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

    /** The button icon, or `null` for none. */
    var icon: Drawable?
        get() = iconImage.drawable
        set(value) {
            iconImage.isVisible = (value != null)
            iconImage.drawable = value
            updateIconViewPadding()
            updateIconSize()
        }

    /** The icon size (width actually), in pixels. */
    var iconSize = 64f
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

    override var enabled
        get() = super.enabled
        set(value) {
            super.enabled = value

            val color = if (value) fontStyle.fontColor else style.disabledColor
            titleLabel.color.set(color)
            iconImage.color.set(color)
        }


    private val titleLabel: SdfLabel
    private val iconImage: ShadowImage


    override var pressAlpha
        get() = super.pressAlpha
        set(value) {
            super.pressAlpha = value
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

    init {
        addListener(SelectionListener())

        titleLabel = SdfLabel(skin, fontStyle).apply {
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

        pad(20f, 20f, 20f, 20f)
        updateButtonLayout()

        this.title = title
        this.icon = icon
    }

    /**
     * Create a menu button with only an [icon].
     */
    constructor(skin: Skin, iconColor: Color, icon: Drawable,
                style: MenuButtonStyle = skin.get()) :
            this(skin, FontStyle(fontColor = iconColor), null, icon, style)


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
            when (if (iconSide == Side.NONE) anchorSide else iconSide) {
                Side.NONE, Side.TOP -> iconCell.padBottom(30f)
                Side.BOTTOM -> iconCell.padTop(30f)
                Side.LEFT -> iconCell.padRight(30f)
                Side.RIGHT -> iconCell.padLeft(30f)
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

    override fun drawChildren(batch: Batch, parentAlpha: Float) {
        // Draw background
        // Alpha depends on hovered, checked and enabled states
        val alpha = (0.2f + 0.2f * checkAlpha + 0.1f * hoverAlpha) *
                alpha * parentAlpha * if (enabled) 1f else 0.6f
        batch.setColor(color.r, color.g, color.b, alpha)
        backgroundDrawable.draw(batch, x, y, width, height)

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
        lateinit var backgroundCenter: Drawable
        lateinit var backgroundTop: Drawable
        lateinit var backgroundBottom: Drawable
        lateinit var backgroundLeft: Drawable
        lateinit var backgroundRight: Drawable

        lateinit var selectedColor: Color
        lateinit var disabledColor: Color
    }

    enum class Side {
        NONE, TOP, BOTTOM, LEFT, RIGHT
    }

}
