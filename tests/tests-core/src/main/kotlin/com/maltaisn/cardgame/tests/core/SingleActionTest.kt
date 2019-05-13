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

import com.badlogic.gdx.Input
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.maltaisn.cardgame.CardGameLayout


/**
 * Base class for a test with a single action that can be
 * triggered on space or volume down keypress.
 */
abstract class SingleActionTest : CardGameTest() {

    protected var action: (() -> Unit)? = null

    override fun layout(layout: CardGameLayout) {
        super.layout(layout)

        addListener(object : InputListener() {
            override fun keyUp(event: InputEvent, keycode: Int): Boolean {
                if (keycode == Input.Keys.SPACE || keycode == Input.Keys.VOLUME_DOWN) {
                    action?.invoke()
                    return true
                }
                return false
            }
        })
    }

}