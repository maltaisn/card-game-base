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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel


/**
 * Base class for managing the game itself. Takes care of creating game states,
 * saving and loading the game, listening to settings changes, etc.
 */
abstract class CardGame(val settings: GamePrefs) : PrefEntry.PrefListener, Disposable {

    /** The current phase of the game. */
    var phase = Phase.ENDED

    /** The state of the game in the current round, or `null` if game is not ongoing. */
    var gameState: CardGameState? = null

    /**
     * The list of events that happened in the game.
     * Cleared when the game is started.
     */
    val events: List<GameEvent>
        get() = _events

    private val _events = mutableListOf<GameEvent>()

    /** The listener called when a game event happens, or `null` for none. */
    var eventListener: ((GameEvent) -> Unit)? = null

    /** The current round. */
    var round = 0
        protected set


    protected var coroutineScope = CoroutineScope(Dispatchers.Default)


    init {
        for (pref in settings.prefs.values) {
            pref.listeners += this
        }
    }


    constructor(settings: GamePrefs, file: FileHandle) : this(settings)


    /** Start the game. */
    open fun start() {
        check(phase == Phase.ENDED) { "Game has already started." }
        phase = Phase.GAME_STARTED

        round = 0
        gameState = null
        _events.clear()
        _events += GameEvent.Start
        eventListener?.invoke(GameEvent.Start)
    }

    /** End the game. */
    open fun end() {
        if (phase == Phase.ROUND_STARTED) {
            // End round if necessary
            endRound()
        }
        check(phase == Phase.GAME_STARTED) { "Game has already ended." }
        phase = Phase.ENDED

        _events += GameEvent.End
        eventListener?.invoke(GameEvent.End)
    }

    /** Start a new round. */
    open fun startRound() {
        check(phase == Phase.GAME_STARTED) { "Round has already started or game has not started." }
        phase = Phase.ROUND_STARTED

        round++
        gameState = null
        _events += GameEvent.RoundStart
        eventListener?.invoke(GameEvent.RoundStart)
    }

    /** End the current round. */
    open fun endRound() {
        check(phase == Phase.ROUND_STARTED) { "Round has already ended or game has not started." }
        phase = Phase.GAME_STARTED

        _events += GameEvent.RoundEnd
        eventListener?.invoke(GameEvent.RoundEnd)
    }

    /** Do a [move] on the game state. */
    open fun doMove(move: GameEvent.Move) {
        check(phase != Phase.ENDED) { "Game has not started." }

        _events += move
        gameState?.doMove(move)
        eventListener?.invoke(move)
    }

    /**
     * Save the game options and current state to a local [file].
     */
    abstract fun save(file: FileHandle)


    override fun dispose() {
        // Detach all listeners
        eventListener = null
        for (pref in settings.prefs.values) {
            pref.listeners -= this
        }

        // Cancel all coroutines
        coroutineScope.cancel()
    }

    override fun toString() = "[phase: $phase, round $round, ${events.size} events]"


    enum class Phase {
        /** Game has not started or is done. */
        ENDED,
        /** Game is started but round is done. */
        GAME_STARTED,
        /** Round has started and is being played. */
        ROUND_STARTED
    }

}
