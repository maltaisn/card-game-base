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

package io.github.maltaisn.cardgame.core


/**
 * Base class for a card.
 * Card classes should always be immutable and be pooled.
 * @property value The card value, unique for each card.
 */
abstract class Card(val value: Int) {

    interface Sorter<T : Card> : Comparator<T> {
        /**
         * Whether the sorter is transitive or not.
         * If not transitive, the order might depends on what cards
         * are being sorted, so [initialize] must be called
         */
        val transitive: Boolean

        /**
         * Initialize the sorter to sort [cards], if sorter is not [transitive].
         * Then, the deck can be sorted with `deck.sortWith(Sorter)`.
         */
        fun initialize(cards: List<T>)
    }

}