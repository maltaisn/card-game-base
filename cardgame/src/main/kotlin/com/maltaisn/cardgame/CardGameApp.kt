/*
 * Copyright 2019 Nicolas Maltais
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.maltaisn.cardgame

import com.badlogic.gdx.Application
import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx


abstract class CardGameApp : Game() {

    override fun create() {
        // Disable continuous rendering
        Gdx.graphics.isContinuousRendering = false
        Gdx.graphics.requestRendering()

        @Suppress("LibGDXLogLevel")
        Gdx.app.logLevel = Application.LOG_DEBUG
    }

}