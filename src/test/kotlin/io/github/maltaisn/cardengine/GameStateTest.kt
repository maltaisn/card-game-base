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

package io.github.maltaisn.cardengine

import io.github.maltaisn.cardengine.core.BaseGameState
import io.github.maltaisn.cardengine.core.BaseMove
import io.github.maltaisn.cardengine.core.BasePlayer
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame

internal class GameStateTest {

    @Test
    fun duplicate() {
        val state1 = GameState(mutableListOf(Player(), Player()), 0)
        val state2 = state1.clone()

        assertNotSame(state1, state2)
        assertNotSame(state1.players, state2.players)
        assertEquals(state1.playerToMove, state2.playerToMove)
        for (i in 0 until state1.players.size) {
            assertNotSame(state1.players[i], state2.players[i])
        }
    }

    @Test
    fun playerToMove() {
        val state = GameState(mutableListOf(Player(), Player()), 0)
        assertEquals(0, state.playerToMove)
        state.doMove(Move(0))
        assertEquals(1, state.playerToMove)
        assertEquals(0, state.getPlayerNextTo(state.playerToMove))
        state.doMove(Move(0))
        assertEquals(0, state.playerToMove)
    }


    // Test classes that do not much more than the base classes.

    class Move(player: Int) : BaseMove(player)

    class Player : BasePlayer() {
        override fun play(state: BaseGameState<out BasePlayer>): Nothing {
            throw UnsupportedOperationException()
        }

        override fun clone(): Player {
            val player = Player()
            player.name = name
            return player
        }
    }

    class GameState : BaseGameState<BasePlayer> {
        constructor(players: List<Player>, playerToMove: Int) : super(players, playerToMove)
        constructor(state: GameState) : super(state)

        override fun getMoves() = ArrayList<BaseMove>()
        override fun doMove(move: BaseMove) {
            playerToMove = getPlayerNextTo(playerToMove)
        }

        override fun getResult(): Nothing? = null
        override fun isGameDone() = false
        override fun clone() = GameState(this)
    }

}