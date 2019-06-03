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

import com.badlogic.gdx.utils.Json
import com.badlogic.gdx.utils.JsonValue

/**
 * Base class for a player in a game state.
 */
abstract class CardPlayer : Cloneable, Json.Serializable {

    /** Player position, set by the game state. */
    var position = NO_POSITION

    /** Player name, can be `null` if not named. */
    var name: String? = null

    /**
     * Called when [state] performs a [move] for any player.
     */
    open fun onMove(state: CardGameState<*>, move: CardGameEvent.Move) = Unit

    /**
     * Create a deep copy of this player.
     */
    public abstract override fun clone(): CardPlayer


    protected fun <T : CardPlayer> cloneTo(player: T) = player.also {
        it.name = name
        it.position = position
    }


    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is CardPlayer) return false
        return position == other.position
    }

    override fun hashCode() = name.hashCode()

    override fun toString() = "[name: ${name ?: "<unnamed>"}, position: $position]"


    override fun read(json: Json, jsonData: JsonValue) {
        position = jsonData.getInt("pos")
        name = jsonData.getString("name")
    }

    override fun write(json: Json) {
        json.writeValue("pos", position)
        json.writeValue("name", name)
    }


    companion object {
        const val NO_POSITION = -1
    }

}
