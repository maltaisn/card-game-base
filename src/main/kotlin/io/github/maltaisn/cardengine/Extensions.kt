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

package io.github.maltaisn.cardengine

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.Actor


/**
 * Draw a scaled and offset [sprite] on a [batch] with an [alpha] value for transparency.
 */
internal fun Actor.drawSprite(batch: Batch, sprite: Sprite, scale: Float, offset: Float, alpha: Float) {
    val oldScaleX = sprite.scaleX
    val oldScaleY = sprite.scaleY
    val offsetScaled = offset * scale
    val tx = x + offsetScaled
    val ty = y + offsetScaled
    sprite.setScale(scale)
    sprite.translate(tx, ty)
    sprite.draw(batch, alpha)
    sprite.setScale(oldScaleX, oldScaleY)
    sprite.translate(-tx, -ty)
}

/**
 * Returns whether a point ([x], [y]) in the actor's coordinates is within its bounds.
 */
internal fun Actor.withinBounds(x: Float, y: Float) = x >= 0 && y >= 0 && x <= width && y <= height

/**
 * Apply an interpolation to an alpha value.
 * Both the parameter and the result are checked to be between 0 and 1.
 */
fun Interpolation.applyBounded(a: Float) = this.apply(a.coerceIn(0f, 1f)).coerceIn(0f, 1f)