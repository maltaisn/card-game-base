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

package com.maltaisn.cardgame.core

import java.util.*


/**
 * Base class for a list of cards that can be sorted, shuffled and compared.
 * @param T Type of card in the deck
 */
open class Deck<T : Card> : ArrayList<T> {

    /**
     * Create a new deck with an initial capacity of 16.
     */
    constructor() : this(16)

    /**
     * Create a new deck with an [initialCapacity].
     */
    constructor(initialCapacity: Int) : super(initialCapacity)

    /**
     * Create a new deck from a collection of [cards].
     */
    constructor(cards: Collection<T>) : super(cards)

    /**
     * Draw and returns the bottom card.
     */
    fun drawBottom() = removeAt(0)

    /**
     * Draw and returns [count] cards from the bottom.
     */
    fun drawBottom(count: Int): Deck<T> {
        val cards = subList(0, count)
        val drawn = Deck<T>(cards)
        cards.clear()
        return drawn
    }

    /**
     * Draw and returns the top card.
     */
    fun drawTop() = removeAt(size - 1)

    /**
     * Draw and returns [count] cards from the top.
     */
    fun drawTop(count: Int): Deck<T> {
        val cards = subList(size - count, size)
        val drawn = Deck<T>(cards)
        cards.clear()
        return drawn
    }

    /**
     * Remove duplicate cards from this deck, keeping the order.
     */
    fun removeDuplicates() {
        val found = BitSet(128)
        var i = 0
        while (i < size) {
            val card = this[i]
            if (found.get(card.value)) {
                removeAt(i)
            } else {
                found.set(card.value)
                i++
            }
        }
    }

    /**
     * Check if the deck contains the same cards in the same order as the [order] deck.
     */
    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other is Deck<*>) return true
        return super.equals(other)
    }

    /**
     * Check if deck contains the same cards as the [other] deck. Order is ignored.
     */
    fun equalsContent(other: Deck<T>): Boolean {
        if (other === this) return true

        val deck1 = clone()
        val deck2 = other.clone()

        val comparator = Comparator<Card> { card1, card2 -> card1.value.compareTo(card2.value) }
        deck1.sortWith(comparator)
        deck2.sortWith(comparator)
        return deck1 == deck2
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append('[')
        for (card in this) {
            sb.append(card)
            sb.append(", ")
        }
        if (sb.length > 1) {
            sb.delete(sb.length - 2, sb.length)
        }
        sb.append(']')
        return sb.toString()
    }

    /**
     * Create a copy of this deck.
     */
    override fun clone() = Deck(this)

    /**
     * Return a string representation of the deck sorted with a [comparator].
     */
    fun toSortedString(comparator: Comparator<T>): String {
        val deck = clone()
        deck.sortWith(comparator)
        return deck.toString()
    }

}
