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

/**
 * The base class for any move in any game made by a [player].
 * All subclasses MUST implement [equals] for MCTS to work.
 * A move must always be immutable.
 */
@Suppress("EqualsOrHashCode")
abstract class BaseMove(val player: Int) {

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is BaseMove) return false
        return player == other.player
    }

}