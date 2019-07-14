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

package com.maltaisn.cardgame.widget.action

import com.badlogic.gdx.scenes.scene2d.Action
import com.badlogic.gdx.scenes.scene2d.Actor
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


/**
 * A delegate for nullable action property of type [T] that gets
 * added to the actor when set and removed when set to null.
 */
class ActionDelegate<T : Action>(private var action: T? = null) : ReadWriteProperty<Actor, T?> {

    override fun getValue(thisRef: Actor, property: KProperty<*>) = action

    override fun setValue(thisRef: Actor, property: KProperty<*>, value: T?) {
        if (action != null) {
            thisRef.removeAction(action)
        }

        action = value

        if (value != null) {
            thisRef.addAction(value)
        }
    }

}
