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

import kotlin.math.ln
import kotlin.math.sqrt

/**
 * Implementation of the Information Set Monte Carlo Tree Search (ISMCTS)
 * - Explanation of MCTS: [http://mcts.ai].
 * - Original ISMCTS implementation in Python: [https://gist.github.com/kjlubick/8ea239ede6a026a61f4d].
 * - ISMCTS algorithm: [https://ieeexplore.ieee.org/document/6203567].
 */
class Mcts {

    /**
     * Exploration param balancing exploration and exploitation.
     * Lower value = more exploitation, higher value = more exploration.
     */
    var explorationParam = DEFAULT_EXPLORATION

    /**
     * Find and returns the move with the best outcome from moves
     * available in [rootState] in [iter] simulations.
     */
    fun run(rootState: BaseGameState<out BasePlayer>, iter: Int): BaseMove {
        val rootNode = Node(null, null, rootState.playerToMove, explorationParam)

        repeat(iter) {
            var node = rootNode
            val state = rootState.randomizedClone(rootState.playerToMove)

            // Select
            var moves = state.getMoves()
            if (moves.size == 1) {
                // Root state has only 1 possible move, no choice to make.
                return moves.first()
            }

            var untriedMoves = node.getUntriedMoves(moves)
            while (node.childNodes.isNotEmpty() && untriedMoves.isEmpty()) {
                node = node.selectUCBChild(moves) ?: break
                state.doMove(node.move!!)
                moves = state.getMoves()
                untriedMoves = node.getUntriedMoves(moves)
            }

            // Expand
            if (untriedMoves.isNotEmpty()) {
                val move = untriedMoves.random()
                val player = state.playerToMove
                state.doMove(move)
                moves = state.getMoves()
                node = node.addChild(move, player)
            }

            // Simulate
            if (moves.isNotEmpty()) {
                var randomMove: BaseMove? = moves.random()
                do {
                    state.doMove(randomMove!!)
                    randomMove = state.getRandomMove()
                } while (randomMove != null)
            }

            // Backpropagate
            var parent: Node? = node
            val result = state.getResult()!!
            while (parent != null) {
                parent.update(result)
                parent = parent.parent
            }
        }

        return rootNode.childNodes.maxBy { it.visits }!!.move!!
    }

    private class Node(val move: BaseMove?, val parent: Node?, val playerThatMoved: Int,
                       val explorationParam: Double) {

        val childNodes = ArrayList<Node>()

        // Number of times this node lead to a win
        var wins = 0.0

        // Number of times this node has been visited
        var visits = 0

        var avails = 0

        /**
         * Filter moves for which this node has no children, from a list of moves
         * Note that it is not possible to optimize this operation by creating a list of untried
         * moves when node is created and removing them at the same time as children nodes are
         * added, because the list of possible moves is rarely always the same, eg: there may be
         * one move to draw a card that results in many moves depending on the card
         */
        fun getUntriedMoves(moves: List<BaseMove>): List<BaseMove> {
            val tried = List(childNodes.size) { childNodes[it].move }
            return moves.filter { move -> tried.find { it == move } == null }
        }

        /**
         * Select the child node that has a move in [moves] that maximizes the [computeUCB] formula
         * All child nodes are not necessarily selectable because there might be more children
         * in total than those available from a particular state (see example above)
         */
        fun selectUCBChild(moves: List<BaseMove>): Node? {
            val selectable = childNodes.filter { child -> moves.find { it == child.move } != null }
            for (node in selectable) {
                node.avails++
            }
            return selectable.maxBy { it.computeUCB() }
        }

        /**
         * Compute the UCB formula for this node.
         * Cannot be done for the root node.
         * See [https://en.wikipedia.org/wiki/Monte_Carlo_tree_search#Exploration_and_exploitation].
         */
        fun computeUCB() = wins / visits + explorationParam * sqrt(ln(avails.toDouble()) / visits)

        fun addChild(move: BaseMove, playerThatMoved: Int): Node {
            val node = Node(move, this, playerThatMoved, explorationParam)
            childNodes += node
            return node
        }

        /**
         * Update the node visits and wins from a state [result].
         */
        fun update(result: BaseResult) {
            visits++
            wins += result.playerResults[playerThatMoved]
        }

        override fun toString(): String = if (move == null) {
            "[root node, ${childNodes.size} children, $visits visits]"
        } else {
            "[move: $move, ${childNodes.size} children, $wins wins in $visits visits, avails: $avails]"
        }
    }


    companion object {
        const val DEFAULT_EXPLORATION = 0.70710678  // sqrt(2.0) / 2
    }

}
