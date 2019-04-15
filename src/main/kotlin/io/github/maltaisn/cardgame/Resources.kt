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

package io.github.maltaisn.cardgame


/**
 * Stores the paths of core game resources.
 * Project should use a Gradle task to copy the core assets to an `core/` folder under their assets.
 */
object Resources {

    const val PCARD_SKIN = "core/pcard/pcard.skin"
    const val PCARD_SKIN_ATLAS = "core/pcard/pcard.atlas"

    const val CORE_SKIN = "core/core.skin"
    const val CORE_SKIN_ATLAS = "core/core.atlas"

    const val FONT_NAME = "core/font/font"
    const val FONT_BOLD_NAME = "core/font/font-bold"
    const val FONT_SHADER_VERT = "core/font/font.vert"
    const val FONT_SHADER_FRAG = "core/font/font.frag"

    const val CORE_STRINGS_FILE = "core/strings"
    const val CORE_STRINGS_NAME = "core-strings"

}