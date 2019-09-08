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
    const val SOUND_GAME_WIN = "game-win"
    const val SOUND_GAME_LOSE = "game-lose"
    const val SOUND_GAME_DONE = "game-done"
    const val SOUND_EVENT_BAD = "event-bad"
    const val SOUND_EVENT_ERROR = "event-error"
    const val SOUND_CARD_TAKE = "card-take"
    const val SOUND_CARD_SHOVE = "card-shove"
    const val SOUND_CARD_DRAW = "card-draw"
    const val SOUND_CARD_DROP = "card-drop"

    val SOUNDS = mapOf(
            SOUND_GAME_WIN to "core/sound/game-win.mp3",
            SOUND_GAME_LOSE to "core/sound/game-lose.mp3",
            SOUND_GAME_DONE to "core/sound/game-done.mp3",
            SOUND_EVENT_BAD to "core/sound/event-bad.mp3",
            SOUND_EVENT_ERROR to "core/sound/event-error.mp3",
            SOUND_CARD_TAKE to "core/sound/card-take.mp3",
            SOUND_CARD_SHOVE to "core/sound/card-shove.mp3",
            SOUND_CARD_DRAW to "core/sound/card-draw.mp3",
            SOUND_CARD_DROP to "core/sound/card-drop.mp3")

}
