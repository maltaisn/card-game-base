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

package com.maltaisn.cardgame.widget

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import ktx.style.get


class GameLayer(skin: Skin) : Table(skin) {

    private val style: CoreStyle = skin.get()

    /**
     * The size of the tables hidden on each side in pixels.
     * These can be used to put other player hands or other hidden card containers.
     */
    var sideTableSize = 400f
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
        batch.setColor(1f, 1f, 1f, parentAlpha * 0.3f)
        style.border.draw(batch, 25f, 25f,
                centerTable.width - 50f, centerTable.height - 50f)

        super.drawChildren(batch, parentAlpha)
    }

    class CoreStyle {
        lateinit var background: Drawable
        lateinit var border: Drawable
    }

}
