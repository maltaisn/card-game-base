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

package com.maltaisn.cardgame.widget.prefs

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.maltaisn.cardgame.widget.text.FontStyle
import com.maltaisn.cardgame.widget.text.SdfLabel
import ktx.actors.onClickEvent


/**
 * A label that draws an icon on the rightside of the last character of the last line.
 * The icon is size proportionally to the font size.
 * This widget must always be drawn
 */
class PrefTitleLabel(skin: Skin, sdfStyle: FontStyle, text: CharSequence? = null,
                     private val helpIcon: Drawable? = null) : SdfLabel(skin, sdfStyle, text) {

    /** The listener called when the icon is clicked. */
    var iconClickListener: (() -> Unit)? = null

    private val iconSize = sdfStyle.fontSize + 8f
    private val iconRect = Rectangle(0f, 1f - ICON_PADDING,
            iconSize + 2 * ICON_PADDING, iconSize + 2 * ICON_PADDING)


    init {
        onClickEvent { _, _, x, y ->
            if (enabled && helpIcon != null && iconRect.contains(x, y)) {
                iconClickListener?.invoke()
            }
        }
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        super.draw(batch, parentAlpha)

        // Draw the help icon
        if (helpIcon != null) {
            val lastLine = glyphLayout.runs.last()
            iconRect.x = lastLine.x + lastLine.width + 20f - ICON_PADDING
            batch.setColor(color.r, color.g, color.b,
                    color.a * parentAlpha * if (enabled) 1f else 0.5f)
            helpIcon.draw(batch, x + iconRect.x + ICON_PADDING,
                    y + iconRect.y + ICON_PADDING, iconSize, iconSize)
        }
    }

    companion object {
        private const val ICON_PADDING = 10f
    }

}
