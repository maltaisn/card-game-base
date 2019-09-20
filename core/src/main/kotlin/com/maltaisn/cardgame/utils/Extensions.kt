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

package com.maltaisn.cardgame.utils

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.maltaisn.cardgame.widget.action.TimeAction
import ktx.actors.setScrollFocus


/**
 * Returns whether a point ([x], [y]) in the actor's coordinates is within its bounds.
 */
internal fun Actor.withinBounds(x: Float, y: Float) = x >= 0 && y >= 0 && x <= width && y <= height

/**
 * Add an [action] to this actor to be done on next frame.
 */
inline fun Actor.post(crossinline action: () -> Unit) = postDelayed(0f, action)

/**
 * Add an [action] to this actor to be done after a [delay] in seconds and return it.
 * If the delay is less or equal than 0, the action will happen on next frame.
 */
inline fun Actor.postDelayed(delay: Float, crossinline action: () -> Unit): TimeAction {
    val delayedAction = object : TimeAction(delay) {
        override fun end() {
            action()
        }
    }
    addAction(delayedAction)
    return delayedAction
}

/**
 * Set the default size parameter on a cell.
 */
fun <T : Actor> Cell<T>.defaultSize(): Cell<T> {
    size(Value.prefWidth, Value.prefHeight)
            .minSize(Value.minWidth, Value.minHeight)
            .maxSize(Value.maxWidth, Value.maxHeight)
    return this
}

fun Table.padH(pad: Float): Table = padLeft(pad).padRight(pad)
fun Table.padV(pad: Float): Table = padTop(pad).padBottom(pad)

fun <T : Actor> Cell<T>.padH(pad: Float): Cell<T> = padLeft(pad).padRight(pad)
fun <T : Actor> Cell<T>.padV(pad: Float): Cell<T> = padTop(pad).padBottom(pad)

fun <T : Actor> Container<T>.padH(pad: Float): Container<T> = padLeft(pad).padRight(pad)
fun <T : Actor> Container<T>.padV(pad: Float): Container<T> = padTop(pad).padBottom(pad)

/**
 * Recursively search for a [ScrollPane] in a group and set the scroll focus if one is found.
 */
fun Group.findScrollFocus() {
    for (child in children) {
        if (child is ScrollPane) {
            child.setScrollFocus()
            return
        } else if (child is Group) {
            child.findScrollFocus()
        }
    }
}
