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

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.Drawable


class GameLayer(skin: Skin) : Table(skin) {

    val style = skin.get(CoreStyle::class.java)

    /**
     * The size of the tables hidden on each side in pixels.
     * These can be used to put other player hands or other hidden card containers.
     */
    var sideTableSize = 200f
        set(value) {
            field = value
            topTable.height = value
            bottomTable.height = value
            leftTable.width = value
            rightTable.width = value
            invalidateHierarchy()
        }

    val centerTable = Table()
    val topTable = Table()
    val bottomTable = Table()
    val leftTable = Table()
    val rightTable = Table()

    init {
        // Make the layout, one table on each side and one in the center.
        add(topTable).height(sideTableSize).colspan(3).growX().row()
        add(leftTable).width(sideTableSize).growY()
        add(centerTable).grow()
        add(rightTable).width(sideTableSize).growY().row()
        add(bottomTable).height(sideTableSize).colspan(3).growX()

        topTable.isVisible = false
        bottomTable.isVisible = false
        leftTable.isVisible = false
        rightTable.isVisible = false
    }

    override fun drawChildren(batch: Batch, parentAlpha: Float) {
        // Draw the background
        batch.setColor(1f, 1f, 1f, parentAlpha)
        style.background.draw(batch, 0f, 0f,
                centerTable.width, centerTable.height)

        // Draw the background border
        val margin = style.borderMargin
        style.border.draw(batch, margin, margin,
                centerTable.width - 2 * margin,
                centerTable.height - 2 * margin)

        super.drawChildren(batch, parentAlpha)
    }

    class CoreStyle {
        /** Drawable for the game background */
        lateinit var background: Drawable
        /** Drawable for the background border. (9-patch) */
        lateinit var border: Drawable
        /** Margin size for the background border. */
        var borderMargin = 0f

        /** Drawable for the background of a card, including its shadow. Must have the largest padding. (9-patch) */
        lateinit var cardBackground: Drawable
        /** Drawable for the hover. (9-patch) */
        lateinit var cardHover: Drawable
        /** Drawable for the selection. Must have no padding. (9-patch) */
        lateinit var cardSelection: Drawable
        /** Drawable for the slot. Must have no padding. (9-patch) */
        lateinit var cardSlot: Drawable
    }

}