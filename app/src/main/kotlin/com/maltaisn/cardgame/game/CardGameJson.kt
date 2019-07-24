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

import com.badlogic.gdx.utils.Json
import com.badlogic.gdx.utils.JsonWriter


open class CardGameJson : Json() {

    /**
     * The version of the card game library that the [CardGame] being currently
     * deserialized was written by.
     */
    var version = -1
        internal set

    init {
        setOutputType(JsonWriter.OutputType.javascript)
        setUsePrototypes(false)
        setEnumNames(true)
        setTypeName("type")
    }

}
