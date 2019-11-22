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

import com.maltaisn.cardgame.game.CardGameState
import com.maltaisn.cardgame.game.ai.Mcts


/**
 * Interface to be implemented by players using [Mcts] to play.
 */
interface CardMctsPlayer {

    /**
     * Whether this player is a clone used for MCTS simulation.
     * If it's a clone, it shouldn't learn from moves since it only makes random moves.
     */
    var isMctsClone: Boolean

    /**
     * Called to randomize the cloned game [state] to reflect the point of view
     * of this player, randomizing any unknown information. This is only used by the MCTS.
     * This is the "determinization" step, transforming the game of imperfect information
     * into a game of perfect information.
     */
    fun randomizeGameState(state: CardGameState<*>) {
        // Set MCTS clone attribute to all players
        for (player in state.players) {
            if (player is CardMctsPlayer) {
                player.isMctsClone = true
            }
        }
    }

}
