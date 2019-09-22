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
import com.maltaisn.cardgame.widget.CheckableWidget
import com.maltaisn.cardgame.widget.ShadowImage
import com.maltaisn.msdfgdx.FontStyle
import com.maltaisn.msdfgdx.widget.MsdfLabel
import ktx.actors.alpha
import ktx.style.get


/**
 * Base class for a menu button with a title and an icon.
 *
 * @property anchorSide The side on which this button is "anchored".
 * There will be no rounded corners on this side.
 */
open class MenuButton(skin: Skin, fontStyle: FontStyle,
                      title: CharSequence? = null, icon: Drawable? = null,
                      var anchorSide: AnchorSide = AnchorSide.NONE) : CheckableWidget() {

    private val style: MenuButtonStyle = skin.get()
    private val fontStyle = FontStyle(fontStyle)

    /** The button title, or `null` for none. */
    var title: CharSequence?
        get() = titleLabel.text
        set(value) {
            titleLabel.txt = value
        }

    /** The button icon, or `null` for none. */
    var icon: Drawable?
        get() = iconImage.drawable
        set(value) {
            iconImage.drawable = value
        }


    override var enabled
        get() = super.enabled
        set(value) {
            super.enabled = value

            val color = if (value) originalColor else style.disabledColor
            fontStyle.color.set(color)
            iconImage.color.set(color)
        }


    val titleLabel = MsdfLabel(title, skin, this.fontStyle)
    val iconImage = ShadowImage(icon, Scaling.fit, fontStyle.shadowColor)


    override var pressAlpha
        get() = super.pressAlpha
        set(value) {
            super.pressAlpha = value
            val color = interpolateColors(originalColor, style.selectedColor, value)
            fontStyle.color.set(color)
            iconImage.color.set(color)
        }


    private val tempColor = Color()
    private val originalColor = fontStyle.color.cpy()

    private val backgroundDrawable: Drawable
        get() = when (anchorSide) {
            AnchorSide.NONE -> style.backgroundCenter
            AnchorSide.TOP -> style.backgroundTop
            AnchorSide.BOTTOM -> style.backgroundBottom
            AnchorSide.LEFT -> style.backgroundLeft
            AnchorSide.RIGHT -> style.backgroundRight
        }

    init {
        addListener(SelectionListener())
        touchable = Touchable.enabled
        pad(20f)
    }


    override fun drawChildren(batch: Batch, parentAlpha: Float) {
        // Draw background
        // Alpha depends on hovered, checked and enabled states
        val alpha = (0.2f + 0.2f * checkAlpha + 0.1f * hoverAlpha) *
                alpha * parentAlpha * if (enabled) 1f else 0.6f
        batch.setColor(color.r, color.g, color.b, alpha)
        backgroundDrawable.draw(batch, x, y, width, height)

        // Draw button content
        super.drawChildren(batch, parentAlpha * if (enabled) 1f else 0.5f)
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

    enum class AnchorSide {
        NONE, TOP, BOTTOM, LEFT, RIGHT
    }

}
