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


/**
 * Stores the paths of engine resources.
 * Project should use a Gradle task to copy the engine assets to an `engine/` folder under their assets.
 */
object Resources {

    const val PCARD_SKIN = "engine/pcard/pcard.skin"
    const val PCARD_SKIN_ATLAS = "engine/pcard/pcard.atlas"

    internal const val CORE_SKIN = "engine/core.skin"
    internal const val CORE_SKIN_ATLAS = "engine/core.atlas"

    const val FONT_REGULAR = "engine/font-regular.ttf"
    const val FONT_BOLD = "engine/font-bold.ttf"

    // Generated bitmap font names
    const val FONT_16_SHADOW = "font-16-shadow"
    const val FONT_22 = "font-22"
    const val FONT_22_BOLD = "font-22-bold"
    const val FONT_24_BOLD_SHADOW = "font-24-bold-shadow"
    const val FONT_24 = "font-24"
    const val FONT_32_BOLD = "font-32-bold"
    const val FONT_40_SHADOW = "font-40-shadow"

}