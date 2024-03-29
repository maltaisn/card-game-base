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

package com.maltaisn.cardgame.game.player

import com.badlogic.gdx.utils.Json
import com.badlogic.gdx.utils.JsonValue
import com.maltaisn.cardgame.game.CardGameState

/**
 * Base class for a player in a game state.
 */
abstract class CardPlayer : Cloneable, Json.Serializable {

    /** Player position, set by the game state. */
    var position = NO_POSITION


    /**
     * Get the result indicating the outcome of the given game [state] from the point
     * of view of this player. When called [CardGameState.isGameDone] is always `true`.
     * Result must be normalized i.e. between 0 and 1, a value of 1 indicating a better outcome.
     * This must be implemented by all players, not just MCTS players.
     */
    abstract fun getStateResult(state: CardGameState<*>): Float


    /**
     * Create a deep copy of this player.
     */
    public abstract override fun clone(): CardPlayer


    protected fun <T : CardPlayer> cloneTo(player: T) = player.also {
        it.position = position
    }


    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is CardPlayer) return false
        return position == other.position
    }

    override fun hashCode() = position

    override fun toString() = "[pos: $position]"


    override fun read(json: Json, jsonData: JsonValue) {
        position = jsonData.getInt("pos")
    }

    override fun write(json: Json) {
        // Don't write name to Json, should be set after deserializing.
        json.writeValue("pos", position)
    }


    companion object {
        const val NO_POSITION = -1
    }

}
