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
 * A result of a game state when game is done.
 * @property playerResults an arbitrarly chosen result representing the outcome of the game for
 * each player. A larger value must always indicate a better outcome and a smaller value a worse
 * for [Mcts] to work correctly.
 */
class GameResult(val playerResults: List<Float>)