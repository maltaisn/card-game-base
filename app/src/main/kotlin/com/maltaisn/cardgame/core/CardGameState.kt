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

import com.maltaisn.cardgame.prefs.GamePrefs

/**
 * Defines the state of a game at a particular moment.
 * @property settings The game settings
 * @property players List of players in the game.
 * @property posToMove The position of the next player to move.
 * [getMoves] will return moves for this player.
 */
abstract class CardGameState(val settings: GamePrefs,
                             open val players: List<CardPlayer>,
                             var posToMove: Int) : Cloneable {

    /**
     * The result of the game.
     * If the game is not done, this will return `null`.
     */
    abstract val result: GameResult?

    /** Returns whether the game is done, i.e when [getMoves] is an empty list. */
    open val isGameDone: Boolean
        get() = result != null

    /** Returns the player who has to play next. */
    open val playerToMove: CardPlayer
        get() = players[posToMove]


    /**
     * Create a state copied from another [state].
     */
    protected constructor(state: CardGameState) : this(state.settings,
            state.players.map { it.clone() }, state.posToMove)


    /**
     * Get the position of the player next to [playerPos].
     */
    open fun getPositionNextTo(playerPos: Int) = (playerPos + 1) % players.size

    /**
     * Update the state of the game by doing a [move].
     */
    abstract fun doMove(move: CardGameEvent.Move)

    /**
     * Get the list of legal moves that can be made by [posToMove] at the current state of the game.
     * The list should be empty when game is done, otherwise must contain at least one move.
     * The state must not be modified from this function: two subsequent calls must return the same moves.
     */
    abstract fun getMoves(): MutableList<CardGameEvent.Move>

    /**
     * Get a random legal move, or `null` if there is none possible.
     * By default this picks a random move in [getMoves].
     * In some cases this can be overriden to prevent the instantiation of a lot of move objects.
     * However, it must always provide all the same possible moves as [getMoves] and with the same probability.
     */
    open fun getRandomMove(): CardGameEvent.Move? {
        val moves = getMoves()
        if (moves.size == 0) return null
        return moves.random()
    }

    /**
     * Create a deep copy of this game state.
     */
    public abstract override fun clone(): CardGameState

    /**
     * Get a randomized deep copy of this game state.
     * All information unknown to [observer] player is randomized.
     * By default this returns the same as a normal clone.
     * This should only be called by players using MCTS.
     */
    open fun randomizedClone(observer: Int) = clone()


}
