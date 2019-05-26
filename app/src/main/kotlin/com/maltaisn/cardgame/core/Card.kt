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
 * Base class for a card.
 * Card classes should always be immutable and be pooled.
 * @property value The card value, unique for each card.
 */
abstract class Card(val value: Int) {

    interface Sorter<T : Card> {
        /**
         * Sort a list of [cards] with this sorter.
         */
        fun sort(cards: MutableList<T>) = sortBy(cards) { it }

        /**
         * Sort a [list] by the order of the cards given by the [selector].
         */
        fun <R> sortBy(list: MutableList<R>, selector: (R) -> T)
    }

}

fun <T : Card> MutableList<T>.sortWith(sorter: Card.Sorter<T>) =
        sorter.sort(this)

fun <T : Card, R> MutableList<R>.sortWith(sorter: Card.Sorter<T>, selector: (R) -> T) =
        sorter.sortBy(this, selector)