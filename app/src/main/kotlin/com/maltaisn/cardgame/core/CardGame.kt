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

package com.maltaisn.cardgame.core

import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.Disposable
import com.maltaisn.cardgame.prefs.GamePrefs
import com.maltaisn.cardgame.prefs.PrefEntry


/**
 * Base class for managing the game itself. Takes care of creating game states,
 * saving and loading the game, listening to settings changes, etc.
 */
abstract class CardGame(val settings: GamePrefs) : PrefEntry.PrefListener, Disposable {

    /** The state of the game in the current round, or `null` if game is not ongoing. */
    var gameState: CardGameState? = null

    /**
     * The list of events that happened in the game.
     * Cleared when the game is started.
     */
    abstract val events: List<CardGameEvent>

    /** The listener called when a game event happens, or `null` for none. */
    var eventListener: ((CardGameEvent) -> Unit)? = null


    init {
        settings.addListener(this)
    }

    /**
     * Load a game object from a [file].
     */
    constructor(settings: GamePrefs, file: FileHandle) : this(settings)


    /**
     * Save the game options and current state to a local [file].
     */
    abstract fun save(file: FileHandle)


    override fun dispose() {
        // Detach all listeners
        eventListener = null
        settings.removeListener(this)
    }

    override fun toString() = "[${events.size} events]"

}
