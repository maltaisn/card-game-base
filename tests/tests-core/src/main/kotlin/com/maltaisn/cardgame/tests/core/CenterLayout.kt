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

package com.maltaisn.cardgame.tests.core

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup
import com.badlogic.gdx.scenes.scene2d.utils.Layout
import kotlin.math.max


/**
 * Layout that aligns its children in itself but without changing its size.
 */
class CenterLayout(vararg actors: Actor) : WidgetGroup() {

    init {
        for (actor in actors) {
            addActor(actor)
        }
        touchable = Touchable.childrenOnly
    }

    override fun layout() {
        for (child in children) {
            var childWidth: Float
            var childHeight: Float
            if (child is Layout) {
                childWidth = child.prefWidth
                childHeight = child.prefHeight
            } else {
                childWidth = child.width
                childHeight = child.height
            }
            childWidth = max(width, childWidth)
            childHeight = max(height, childHeight)
            child.setBounds((width - childWidth) / 2, (height - childHeight) / 2,
                    childWidth, childHeight)

            if (child is Layout) {
                child.validate()
            }
        }
    }
}