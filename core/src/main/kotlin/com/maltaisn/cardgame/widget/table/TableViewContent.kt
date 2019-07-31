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

package com.maltaisn.cardgame.widget.table

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import ktx.style.get


/**
 * A table for use in [TableView] with a background and a foreground to "clip" rounded corners.
 */
class TableViewContent(skin: Skin) : Table(skin) {

    private val style: TableContentGroupStyle = skin.get()

    init {
        this.background = style.background
    }

    override fun drawChildren(batch: Batch, parentAlpha: Float) {
        super.drawChildren(batch, parentAlpha)

        // Draw the foreground
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha)
        style.foreground.draw(batch, x, y, width, height)
    }

    class TableContentGroupStyle {
        lateinit var background: Drawable
        lateinit var foreground: Drawable
    }

}
