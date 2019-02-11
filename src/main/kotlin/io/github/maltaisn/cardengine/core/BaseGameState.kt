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

package io.github.maltaisn.cardengine.core

/**
 * Defines the state of a game at a particular moment.
 * @param P The type of player in this game state.
 */
abstract class BaseGameState<P : BasePlayer> : Cloneable {

    /**
     * List of players in the game.
     */
    val players: List<P>

    /**
     * The position of the current player to move.
     * [getMoves] will return moves for this player.
     */
    var playerToMove: Int

    /**
     * Create a game state with [players].
     * @param playerToMove is the position of the player to move.
     */
    constructor(players: List<P>, playerToMove: Int) {
        this.players = players
        this.playerToMove = playerToMove
    }

    /**
     * Create a state copied from another [state].
     */
    @Suppress("UNCHECKED_CAST")
    protected constructor(state: BaseGameState<P>) {
        players = List(state.players.size) { state.players[it].clone() as P }
        playerToMove = state.playerToMove
    }

    /**
     * Get the position of the player next to [player].
     */
    open fun getPlayerNextTo(player: Int) = (player + 1) % players.size

    /**
     * Update the state of the game by doing a [move].
     */
    abstract fun doMove(move: BaseMove)

    /**
     * Get the list of legal moves that can be made by [playerToMove] at the current state of the game.
     * The list should be empty when game is done, otherwise must contain at least one move.
     * The state must not be modified from this function: two subsequent calls must return the same moves.
     */
    abstract fun getMoves(): MutableList<BaseMove>

    /**
     * Get a random legal move, or null if there is none possible
     * By default this picks a random move in [getMoves].
     * In some cases this can be overriden to prevent the instantiation of a lot of move objects.
     * However, it must always provide all the same possible
     * moves as [getMoves] and with the same probability.
     */
    open fun getRandomMove(): BaseMove? {
        val moves = getMoves()
        if (moves.size == 0) return null
        return moves.random()
    }

    /**
     * Get the result of the game.
     * If the game is not done, this will return `null`.
     */
    abstract fun getResult(): BaseResult?

    /**
     * Returns true if this state cannot do any more moves, i.e when [getMoves] is an empty list.
     */
    abstract fun isGameDone(): Boolean

    /**
     * Create a deep copy of this game state.
     */
    public abstract override fun clone(): BaseGameState<P>

    /**
     * Get a randomized deep copy of this game state.
     * All information unknown to [observer] player is randomized.
     * By default this returns the same as a normal clone.
     * This should only be called by players using MCTS.
     */
    open fun randomizedClone(observer: Int) = clone()


}
