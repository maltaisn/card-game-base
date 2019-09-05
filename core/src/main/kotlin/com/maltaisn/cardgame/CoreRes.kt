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

package com.maltaisn.cardgame


/**
 * Stores the paths of core game resources.
 * Project should use a Gradle task to copy the core assets to a `core/` folder under their assets.
 */
object CoreRes {

    // Core skin and atlas
    const val SKIN = "core/core.skin"
    const val SKIN_ATLAS = "core/core.atlas"

    // Font
    const val FONT = "core/font.fnt"

    // Strings
    const val CORE_STRINGS_FILE = "core/strings"
    const val CORE_STRINGS_NAME = "core"

    // Sounds
    const val SOUND_GAME_WIN = "sound/game-win.mp3"
    const val SOUND_GAME_LOSE = "sound/game-lose.mp3"
    const val SOUND_GAME_DONE = "sound/game-done.mp3"
    const val SOUND_EVENT_BAD = "sound/event-bad.mp3"
    const val SOUND_EVENT_ERROR = "sound/event-error.mp3"
    const val SOUND_CARD_TAKE = "sound/card-take.mp3"
    const val SOUND_CARD_SHOVE = "sound/card-shove.mp3"
    const val SOUND_CARD_DRAW = "sound/card-draw.mp3"
    const val SOUND_CARD_DROP = "sound/card-drop.mp3"

}
