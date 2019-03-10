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

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.utils.TransformDrawable
import ktx.math.vec2


/**
 * A simple wrapper around [Image] that draws its drawable twice to create a shadow effect.
 */
class ShadowImage : Image() {

    /** Whether to draw a shadow by drawing the drawable twice. */
    var drawShadow = true

    /** The offset in pixels at which the shadow is drawn. */
    var shadowOffset = vec2(2f, -2f)

    /** The tint of this icon. */
    var shadowColor: Color = Color.BLACK

    private val tempColor = Color()


    override fun draw(batch: Batch, parentAlpha: Float) {
        validate()

        if (drawShadow) {
            drawIcon(batch, shadowOffset, shadowColor, parentAlpha)
        }
        drawIcon(batch, Vector2.Zero, color, parentAlpha)
    }

    private fun drawIcon(batch: Batch, offset: Vector2, color: Color, parentAlpha: Float) {
        val icon = drawable ?: return

        tempColor.set(batch.color)
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha)

        if (icon is TransformDrawable && (scaleX != 1f || scaleY != 1f || rotation != 0f)) {
            icon.draw(batch, x + imageX + offset.x, y + imageY + offset.y,
                    originX - imageX, originY - imageY,
                    imageWidth, imageHeight, scaleX, scaleY, rotation)
        } else {
            icon.draw(batch, x + imageX + offset.x, y + imageY + offset.y,
                    imageWidth * scaleX, imageHeight * scaleY)
        }

        batch.setColor(tempColor.r, tempColor.g, tempColor.b, tempColor.a)
    }

}