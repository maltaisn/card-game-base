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

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup
import io.github.maltaisn.cardengine.widget.GameLayer


/**
 * Custom container that acts places its children on top of each other, matching its size.
 * Game layer uses custom bounds to account for its side tables.
 */
class CardGameContainer(vararg actors: Actor) : WidgetGroup() {

    init {
        setFillParent(true)
        for (actor in actors) {
            addActor(actor)
        }
    }

    override fun layout() {
        super.layout()

        for (child in children) {
            if (child is GameLayer) {
                val side = child.sideTableSize
                child.setBounds(-side, -side, width + 2 * side, height + 2 * side)
            } else {
                child.setBounds(0f, 0f, width, height)
            }
        }
    }

}