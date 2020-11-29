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

package com.maltaisn.cardgame.game.ai

import com.maltaisn.cardgame.game.CardGameState
import com.maltaisn.cardgame.game.event.CardGameMove
import com.maltaisn.cardgame.game.player.CardMctsPlayer
import kotlin.math.ln
import kotlin.math.sqrt

/**
 * Implementation of the Information Set Monte Carlo Tree Search (ISMCTS)
 * - Explanation: [http://www.aifactory.co.uk/newsletter/2013_01_reduce_burden.htm].
 * - Paper: [https://ieeexplore.ieee.org/document/6203567].
 * - Original Python implementation: [https://gist.github.com/kjlubick/8ea239ede6a026a61f4d].
 */
class Mcts {

    /**
     * Exploration param balancing exploration and exploitation.
     * Lower value = more exploitation, higher value = more exploration.
     * Default and theoretical best exploration param is equal to `sqrt(2.0) / 2`.
     */
    var explorationParam = 0.70710678f


    /**
     * Like [run] but immediately returns the most visited child node.
     */
    fun findMove(rootState: CardGameState<*>, iter: Int) =
            run(rootState, iter).childNodes.maxBy { it.visits }!!.move!!

    /**
     * Find and returns the move with the best outcome from moves
     * available in [rootState] in [iter] simulations.
     */
    fun run(rootState: CardGameState<*>, iter: Int): Node {
        val rootNode = Node(null, null, rootState.posToMove)

        check(rootState.playerToMove is CardMctsPlayer) { "Player to move must be a MCTS player." }

        var moves = rootState.getMoves()
        if (moves.size == 0) {
            error("Cannot run MCTS on state with no available moves.")
        } else if (moves.size == 1) {
            // Root state has only 1 possible move, no choice to make.
            rootNode.addChild(moves.first(), rootState.posToMove)
            return rootNode
        }

        repeat(iter) {
            var node = rootNode
            val state = rootState.clone()
            val playerToMove = state.playerToMove as CardMctsPlayer

            // Determinization
            playerToMove.randomizeGameState(state)

            // Select
            moves = state.getMoves()
            var untriedMoves = node.getUntriedMoves(moves)
            while (node.childNodes.isNotEmpty() && untriedMoves.isEmpty()) {
                node = node.selectUCBChild(moves)!!
                state.doMove(node.move!!)
                moves = state.getMoves()
                untriedMoves = node.getUntriedMoves(moves)
            }

            // Expand
            if (untriedMoves.isNotEmpty()) {
                val move = untriedMoves.random()
                val playerPos = state.posToMove
                state.doMove(move)
                node = node.addChild(move, playerPos)
            }

            // Simulate
            while (true) {
                state.doMove(state.getRandomMove() ?: break)
            }

            // Backpropagate
            val results = state.players.map {
                val result = it.getStateResult(state)
                assert(result in 0f..1f) { "Game state result must be between 0 and 1." }
                result
            }
            node.update(results)
        }

        return rootNode
    }


    /**
     * Compute the average result of [iter] simulations of [rootState] doing a [move].
     * This is the same as the "Simulate" step of [run].
     */
    fun simulate(rootState: CardGameState<*>, move: CardGameMove, iter: Int): Float {
        check(rootState.playerToMove is CardMctsPlayer) { "Player to move must be a MCTS player." }

        var totalResult = 0f
        repeat(iter) {
            val state = rootState.clone()
            val playerToMove = state.playerToMove

            (playerToMove as CardMctsPlayer).randomizeGameState(state)
            state.doMove(move)

            // Simulate
            while (true) {
                state.doMove(state.getRandomMove() ?: break)
            }

            totalResult += playerToMove.getStateResult(state)
        }
        return totalResult / iter
    }

    inner class Node(val move: CardGameMove?,
                     val parent: Node?,
                     val posThatMoved: Int) {

        val childNodes = mutableListOf<Node>()

        /** Number of times this node lead to a win. */
        var wins = 0f

        /** Number of times this node has been visited */
        var visits = 0

        /** Number of times this node was available for selection */
        var avails = 0

        /**
         * Filter moves for which this node has no children, from a list of [moves].
         */
        fun getUntriedMoves(moves: List<CardGameMove>) =
                moves.filter { move -> childNodes.none { it.move == move } }

        /**
         * Select the child node that has a move in [moves] that maximizes the UCB formula.
         */
        fun selectUCBChild(moves: List<CardGameMove>): Node? {
            val selectable = childNodes.filter { child -> moves.any { it == child.move } }
            for (node in selectable) {
                node.avails++
            }
            return selectable.maxBy { it.computeUCB() }
        }

        /**
         * Compute the UCB formula for this node. Cannot be done for the root node.
         * See [https://en.wikipedia.org/wiki/Monte_Carlo_tree_search#Exploration_and_exploitation].
         */
        fun computeUCB() = wins / visits + explorationParam * sqrt(ln(avails.toFloat()) / visits)

        fun addChild(move: CardGameMove, posThatMoved: Int): Node {
            val node = Node(move, this, posThatMoved)
            childNodes += node
            return node
        }

        /**
         * Update the node visits and wins from player [results].
         * Result is backpropagated to parent node.
         *
         * I think that we use the result from the player that moved rather than the result
         * from the player that has to play in root state to emulate stronger opposing play.
         * This wasn't explained anywhere in the ISMCTS paper or in the implementation.
         */
        fun update(results: List<Float>) {
            visits++
            wins += results[posThatMoved]
            parent?.update(results)
        }

        override fun toString(): String = if (move == null) {
            "[root node, ${childNodes.size} children, $visits visits]"
        } else {
            "[move: $move, ${childNodes.size} children, $wins wins in $visits visits, avails: $avails]"
        }
    }

}
