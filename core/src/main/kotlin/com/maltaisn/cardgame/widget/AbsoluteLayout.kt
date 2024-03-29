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

import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup
import com.badlogic.gdx.scenes.scene2d.utils.Layout


/**
 * A layout that has no restrictions on the size and position of children.
 * Each children layout takes its preferred size, or the layout size if it has no preferred size.
 */
class AbsoluteLayout : WidgetGroup() {

    init {
        touchable = Touchable.childrenOnly
    }

    override fun layout() {
        for (child in children) {
            if (child is Layout) {
                child.width = if (child.prefWidth == 0f) width else child.prefWidth
                child.height = if (child.prefHeight == 0f) height else child.prefHeight
                child.validate()
            }
        }
    }

}
