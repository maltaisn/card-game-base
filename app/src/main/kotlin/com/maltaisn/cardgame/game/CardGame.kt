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

package com.maltaisn.cardgame.game

import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.Json
import com.badlogic.gdx.utils.JsonValue


/**
 * Base class for managing the game itself.
 */
abstract class CardGame : Disposable, Json.Serializable {

    override fun read(json: Json, jsonData: JsonValue) {
        json as CardGameJson
        json.version = jsonData.getInt("_version")
    }

    override fun write(json: Json) {
        json.writeValue("_version", VERSION)
    }

    companion object {
        val VERSION = 1
    }

}
