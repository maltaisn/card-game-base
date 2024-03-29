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

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.scenes.scene2d.utils.TransformDrawable
import com.badlogic.gdx.utils.Scaling
import ktx.actors.alpha
import ktx.math.vec2


/**
 * A simple wrapper around [Image] that draws its drawable twice to create a shadow effect.
 */
class ShadowImage(drawable: Drawable? = null,
                  scaling: Scaling = Scaling.stretch,
                  var shadowColor: Color = Color.BLACK) : Image(drawable, scaling) {

    /**
     * The offset in pixels at which the shadow is drawn.
     * Y positive down coordinate system.
     */
    var shadowOffset = vec2(4f, 4f)


    override fun draw(batch: Batch, parentAlpha: Float) {
        validate()

        if (shadowColor.a > 0f) {
            drawIcon(batch, shadowOffset, shadowColor, parentAlpha)
        }
        drawIcon(batch, Vector2.Zero, color, parentAlpha)
    }

    private fun drawIcon(batch: Batch, offset: Vector2, color: Color, parentAlpha: Float) {
        val drawable = drawable ?: return

        batch.setColor(color.r, color.g, color.b, alpha * parentAlpha)

        if (drawable is TransformDrawable && (scaleX != 1f || scaleY != 1f || rotation != 0f)) {
            drawable.draw(batch, x + imageX + offset.x, y + imageY - offset.y,
                    originX - imageX, originY - imageY,
                    imageWidth, imageHeight, scaleX, scaleY, rotation)
        } else {
            drawable.draw(batch, x + imageX + offset.x, y + imageY - offset.y,
                    imageWidth * scaleX, imageHeight * scaleY)
        }
    }

}
