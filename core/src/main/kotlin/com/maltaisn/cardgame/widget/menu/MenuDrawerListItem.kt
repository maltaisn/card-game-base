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

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.utils.Pool
import com.maltaisn.cardgame.widget.CheckableWidget
import com.maltaisn.msdfgdx.FontStyle
import com.maltaisn.msdfgdx.widget.MsdfLabel
import ktx.style.get


class MenuDrawerListItem(skin: Skin) : CheckableWidget(), Pool.Poolable {

    /** The item text. */
    var text: CharSequence?
        get() = label.text
        set(value) {
            label.txt = value
        }

    override var enabled: Boolean
        get() = super.enabled
        set(value) {
            super.enabled = value
            label.isDisabled = !value
        }

    private val itemStyle: DrawerListItemStyle = skin.get()
    private val label = MsdfLabel(null, skin, itemStyle.fontStyle)

    private val inputListener = SelectionListener()

    init {
        label.setWrap(true)
        label.touchable = Touchable.disabled

        touchable = Touchable.enabled
        add(label).grow().pad(30f, 60f, 30f, 60f)

        setSize(prefWidth, prefHeight)

        addListener(inputListener)
    }


    override fun reset() {
        text = null
        enabled = true
        check(checked = false, animate = false)
        hoverAction = null
        hoverAlpha = 0f
        pressAction = null
        pressAlpha = 0f
        clearActions()
        clearListeners()
        addListener(inputListener)
        checkListener = null
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        super.draw(batch, parentAlpha)

        // Draw selection/hover overlay
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha *
                (checkAlpha * 0.2f + pressAlpha * 0.1f + hoverAlpha * 0.1f))
        itemStyle.selectionOverlay.draw(batch, x, y, width, height)
    }


    class DrawerListItemStyle(
            val fontStyle: FontStyle,
            val selectionOverlay: Drawable)
}
