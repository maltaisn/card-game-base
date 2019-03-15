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

package io.github.maltaisn.cardgame

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.Actor


/**
 * Returns whether a point ([x], [y]) in the actor's coordinates is within its bounds.
 */
internal fun Actor.withinBounds(x: Float, y: Float) = x >= 0 && y >= 0 && x <= width && y <= height

/**
 * Apply an interpolation to an alpha value.
 * Both the parameter and the result are checked to be between 0 and 1.
 */
fun Interpolation.applyBounded(a: Float) = this.apply(a.coerceIn(0f, 1f)).coerceIn(0f, 1f)