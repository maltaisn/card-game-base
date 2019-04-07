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

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Cell
import com.badlogic.gdx.scenes.scene2d.ui.Value


/**
 * Returns whether a point ([x], [y]) in the actor's coordinates is within its bounds.
 */
internal fun Actor.withinBounds(x: Float, y: Float) = x >= 0 && y >= 0 && x <= width && y <= height

/**
 * Set the default size parameter on a cell.
 */
fun <T : Actor> Cell<T>.defaultSize(): Cell<T> {
    size(Value.prefWidth, Value.prefHeight)
            .minSize(Value.minWidth, Value.minHeight)
            .maxSize(Value.maxWidth, Value.maxHeight)
    return this
}