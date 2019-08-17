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

import com.badlogic.gdx.utils.I18NBundle
import com.badlogic.gdx.utils.Json
import com.badlogic.gdx.utils.JsonValue


/**
 * A JSON object for deserializing JSON with string references, denoted
 * with values starting with [prefix]. String references are resolved using a [bundle].
 */
class StringRefJson(private val bundle: I18NBundle,
                    private val prefix: String = "@string/") : Json() {

    override fun <T : Any?> readValue(type: Class<T>?, elementType: Class<*>?, jsonData: JsonValue): T =
            if (jsonData.isString && jsonData.asString().startsWith(prefix)) {
                // The string is a reference, resolve it.
                @Suppress("UNCHECKED_CAST")
                bundle[jsonData.asString().substring(prefix.length)] as T
            } else {
                super.readValue(type, elementType, jsonData)
            }
}
