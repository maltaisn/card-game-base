/*
 * Copyright 2019 Nicolas Maltais
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.maltaisn.cardgame.tests.core

/**
 * Base class for a player in a game state.
 */
abstract class CardPlayer() : Cloneable {

    /** Player position, set by the game state. */
    var position = -1

    /** Player name, can be `null` if not named. */
    var name: String? = null

    /**
     * Copy constructor.
     */
    protected constructor(player: CardPlayer) : this() {
        name = player.name
        position = player.position
    }

    /**
     * Called when [state] performs a [move] for any player.
     */
    open fun onMove(state: CardGameState, move: GameEvent.Move) {

    }

    /**
     * Create a deep copy of this player.
     */
    public abstract override fun clone(): CardPlayer


    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is CardPlayer) return false
        return position == other.position
    }

    override fun hashCode() = name.hashCode()

    override fun toString() = "[name: ${name ?: "<player-${position + 1}>"}, position: $position]"

}
