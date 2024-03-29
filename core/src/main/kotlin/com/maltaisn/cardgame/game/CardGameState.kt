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
import com.badlogic.gdx.utils.JsonValue
import com.maltaisn.cardgame.game.event.CardGameMove
import com.maltaisn.cardgame.game.player.CardPlayer
import com.maltaisn.cardgame.prefs.GamePrefs

/**
 * Defines the state of a game at a particular moment.
 */
abstract class CardGameState<P : CardPlayer> : Cloneable, Json.Serializable {

    /**
     * The game settings.
     */
    lateinit var settings: GamePrefs

    /**
     * List of players in the game.
     */
    lateinit var players: List<P>

    /**
     * Whether the game is done or not.
     * When game is done, [getMoves] should return an empty list.
     */
    var isGameDone = false
        protected set

    /**
     * The position of the next player to move.
     * Any value set will always be changed to a value between 0 and `players.size`.
     */
    open var posToMove = CardPlayer.NO_POSITION

    /**
     * Returns the player who has to play next.
     */
    open val playerToMove: P
        get() = players[posToMove]


    /**
     * Initialize the game state, needed before making moves.
     */
    protected open fun initialize(settings: GamePrefs, players: List<P>, posToMove: Int) {
        this.settings = settings
        this.players = players
        this.posToMove = posToMove
    }


    /**
     * Get the position of the player next to [playerPos].
     */
    open fun getPositionNextTo(playerPos: Int) = (playerPos + 1) % players.size

    /**
     * Update the state of the game by doing a [move].
     */
    abstract fun doMove(move: CardGameMove)

    /**
     * Get the list of legal moves that can be made by [posToMove] at the current state of the game.
     * The list should be empty when game is done, otherwise must contain at least one move.
     * The state must not be modified from this function: two subsequent calls must return the same moves.
     */
    abstract fun getMoves(): MutableList<out CardGameMove>

    /**
     * Get a random legal move, or `null` if there is none possible.
     * By default this picks a random move in [getMoves].
     * In some cases this can be overriden to prevent the instantiation of a lot of move objects.
     * However, it must always provide all the same possible moves as [getMoves] and with the same probability.
     */
    open fun getRandomMove(): CardGameMove? {
        val moves = getMoves()
        if (moves.size == 0) return null
        return moves.random()
    }

    /**
     * Create a deep copy of this game state.
     */
    public abstract override fun clone(): CardGameState<P>


    @Suppress("UNCHECKED_CAST")
    protected fun <T : CardGameState<P>> cloneTo(state: T) = state.also { s ->
        s.settings = settings
        s.players = players.map { it.clone() as P }
        s.posToMove = posToMove
    }

    override fun equals(other: Any?) = other === this || other is CardGameState<*> &&
            other.players == players && other.posToMove == posToMove

    override fun hashCode() = arrayOf(players, posToMove).contentHashCode()

    override fun toString() = "[${players.size} players, posToMove: $posToMove]"


    override fun read(json: Json, jsonData: JsonValue) {
        posToMove = jsonData.getInt("posToMove")
    }

    /**
     * Write the game state to [json]. The players and settings are not written, the [CardGame] must save
     * them and set them back when loading its game state. This is to avoid duplication in JSON game save.
     */
    override fun write(json: Json) {
        json.writeValue("posToMove", posToMove)
    }

}
