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
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.maltaisn.cardgame.post
import com.maltaisn.cardgame.widget.CheckableWidget
import com.maltaisn.cardgame.widget.ShadowImage
import com.maltaisn.cardgame.widget.text.FontStyle
import com.maltaisn.cardgame.widget.text.SdfLabel
import ktx.actors.alpha
import ktx.style.get


/**
 * A button for a menu, with an optional title and icon.
 */
open class MenuButton(skin: Skin,
                      private val fontStyle: FontStyle,
                      title: CharSequence? = null, icon: Drawable? = null,
                      private val style: MenuButtonStyle = skin.get()) : CheckableWidget() {

    /** The button title, or `null` for none. */
    var title: CharSequence?
        get() = titleLabel.text
        set(value) {
            if (!value.isNullOrBlank() != !titleLabel.text.isBlank()) {
                invalidateLayout()
            }
            titleLabel.setText(value)
        }

    /** The button icon, or `null` for none. */
    var icon: Drawable?
        get() = iconImage.drawable
        set(value) {
            if ((value == null) != (iconImage.drawable == null)) {
                invalidateLayout()
            }
            iconImage.drawable = value
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
            invalidateLayout()
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
            invalidateLayout()
        }

    /**
     * The alignment of the title label.
     */
    var titleAlign = Align.center
        set(value) {
            if (field == value) return
            field = value
            invalidateLayout()
        }

    override var enabled
        get() = super.enabled
        set(value) {
            super.enabled = value

            val color = if (value) fontStyle.fontColor else style.disabledColor
            titleLabel.color.set(color)
            iconImage.color.set(color)
        }


    val titleLabel: SdfLabel
    val iconImage: ShadowImage

    private var invalidLayout = false

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

        titleLabel = SdfLabel(skin, fontStyle)
        iconImage = ShadowImage().apply {
            setScaling(Scaling.fit)
            drawShadow = fontStyle.drawShadow
            shadowColor = fontStyle.shadowColor
        }

        pad(20f)
        updateLayout()

        this.title = title
        this.icon = icon
        touchable = Touchable.enabled
    }

    /**
     * Create a menu button with only an [icon].
     */
    constructor(skin: Skin, iconColor: Color, icon: Drawable,
                style: MenuButtonStyle = skin.get()) :
            this(skin, FontStyle(fontColor = iconColor), null, icon, style)


    /**
     * Clear the children and redo the correct label and icon layout.
     * If title or icon are not set, their widget won't  be added to the layout.
     */
    private fun updateLayout() {
        clearChildren()

        when {
            !title.isNullOrBlank() && icon == null -> {
                add(titleLabel).expand().align(titleAlign)
            }
            icon != null && title.isNullOrBlank() -> {
                add(iconImage).expand()
            }
            else -> {
                when (if (iconSide == Side.NONE) anchorSide else iconSide) {
                    Side.NONE, Side.TOP -> {
                        add(iconImage).expandX().padBottom(30f).row()
                        add(titleLabel).expand(true, !Align.isCenterVertical(titleAlign)).align(titleAlign)
                    }
                    Side.BOTTOM -> {
                        add(titleLabel).expand(true, !Align.isCenterVertical(titleAlign)).align(titleAlign)
                        row()
                        add(iconImage).padTop(30f).expandX()
                    }
                    Side.LEFT -> {
                        add(iconImage).padRight(30f).expandY()
                        add(titleLabel).expand(!Align.isCenterHorizontal(titleAlign), true).align(titleAlign)
                    }
                    Side.RIGHT -> {
                        add(titleLabel).expand(!Align.isCenterHorizontal(titleAlign), true).align(titleAlign)
                        add(iconImage).padLeft(30f).expandY()
                    }
                }
            }
        }
        updateIconSize()

        invalidLayout = false
    }

    /** Request layout update. */
    private fun invalidateLayout() {
        if (!invalidLayout) {
            invalidLayout = true
            post {
                updateLayout()
            }
        }
    }

    private fun updateIconSize() {
        icon?.let {
            getCell(iconImage).size(iconSize, it.minHeight / it.minWidth * iconSize)
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
