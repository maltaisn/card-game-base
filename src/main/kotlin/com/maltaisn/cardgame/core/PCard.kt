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

import com.maltaisn.cardgame.core.PCard.Sorter.Companion.ASCENDING
import com.maltaisn.cardgame.core.PCard.Sorter.Companion.BY_RANK
import com.maltaisn.cardgame.core.PCard.Sorter.Companion.BY_SUIT
import com.maltaisn.cardgame.core.PCard.Sorter.Companion.DESCENDING
import java.util.*


/**
 * A standard playing card.
 * Playing cards have 4 suits: hearts, spades, diamonds and clubs,
 * and range from ace to king (13 cards), with a red and a black joker.
 */
class PCard private constructor(val rank: Int, val suit: Int, value: Int) : Card(value) {

    val color = if (suit == HEART || suit == DIAMOND || suit == RED) RED else BLACK

    /**
     * Returns true if [card]'s rank is higher than this card's rank.
     * [aceHigh] determines if ace is considered less than 2 or greater than king.
     * Joker is the highest rank.
     */
    fun greaterThan(card: PCard, aceHigh: Boolean): Boolean {
        if (aceHigh && rank == ACE && card.rank != ACE && card.rank != JOKER) {
            return true
        } else if (aceHigh && card.rank == ACE && rank != ACE && rank != JOKER) {
            return false
        }
        return rank > card.rank
    }

    /**
     * Returns true if [card]'s rank is less than this card's rank.
     * [aceHigh] determines if ace is considered less than 2 or greater than king.
     * Joker is the highest rank.
     */
    fun lessThan(card: PCard, aceHigh: Boolean): Boolean {
        if (aceHigh && rank == ACE && card.rank != ACE && card.rank != JOKER) {
            return false
        } else if (aceHigh && card.rank == ACE && rank != ACE && rank != JOKER) {
            return true
        }
        return rank < card.rank
    }

    override fun toString(): String = RANK_STR[rank - 1] + SUIT_STR[suit]


    companion object {
        const val ACE = 1
        const val JACK = 11
        const val QUEEN = 12
        const val KING = 13
        const val JOKER = 14

        const val HEART = 0
        const val SPADE = 1
        const val DIAMOND = 2
        const val CLUB = 3

        const val BLACK = 4
        const val RED = 5

        val SUITS_COLOR = listOf(RED, BLACK, RED, BLACK, BLACK, RED)

        val BLACK_JOKER = PCard(JOKER, BLACK, 52)
        val RED_JOKER = PCard(JOKER, RED, 53)

        val RANK_STR = listOf("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "*")
        val SUIT_STR = listOf('♥', '♠', '♦', '♣', 'B', 'R')

        val RANK_RANGE = 1..13
        val SUIT_RANGE = 0..3

        private val cards = arrayOfNulls<PCard>(52)

        init {
            // Create instances of all possible cards.
            var i = 0
            for (suit in SUIT_RANGE) {
                for (rank in RANK_RANGE) {
                    cards[i] = PCard(rank, suit, i)
                    i++
                }
            }
        }

        /**
         * Get a card by its [rank] and [suit].
         */
        fun get(rank: Int, suit: Int): PCard {
            var card: PCard? = null
            if (rank == JOKER) {
                if (suit == BLACK) {
                    card = BLACK_JOKER
                } else if (suit == RED) {
                    card = RED_JOKER
                }
            } else if (rank in RANK_RANGE && suit in SUIT_RANGE) {
                card = cards[suit * 13 + (rank - 1)]
            }
            requireNotNull(card) { "No playing card exist with this rank and suit." }
            return card
        }

        /**
         * Get a card from its string representation.
         */
        fun parse(cardStr: String): PCard {
            val suit = SUIT_STR.indexOf(cardStr.last())
            val rank = RANK_STR.indexOf(cardStr.dropLast(1)) + 1
            require(suit != -1 && rank != -1) { "No playing card exist for '$cardStr'." }
            return get(rank, suit)
        }

        ////////////////////// DECKS //////////////////////
        /**
         * Create [count] full decks of cards.
         * @param withJokers Whetehr to include jokers or not.
         */
        fun fullDecks(count: Int, withJokers: Boolean): Deck<PCard> {
            val deck = Deck<PCard>((if (withJokers) 54 else 52) * count)
            for (i in 0 until count) {
                for (suit in 0..3) {
                    for (rank in 1..13) {
                        deck += get(rank, suit)
                    }
                }
                if (withJokers) {
                    deck += BLACK_JOKER
                    deck += RED_JOKER
                }
            }
            return deck
        }

        /**
         * Create a single full deck.
         * @param withJokers Whetehr to include jokers or not.
         */
        fun fullDeck(withJokers: Boolean) = fullDecks(1, withJokers)

        /**
         * Create a new 36 cards deck
         * All suits from 6 to ace, without jokers
         */
        fun deck36(): Deck<PCard> {
            val deck = Deck<PCard>(36)
            for (suit in 0..3) {
                for (rank in 6..13) {
                    deck += get(rank, suit)
                }
                deck += get(ACE, suit)
            }
            return deck
        }

        /**
         * Return a deck from a list of card strings
         */
        fun parseDeck(vararg cardsStr: String): Deck<PCard> {
            val deck = Deck<PCard>(cardsStr.size)
            for (cardStr in cardsStr) {
                deck += parse(cardStr)
            }
            return deck
        }

        /**
         * Return a deck from a string of card strings separated by a [separator].
         */
        fun parseDeck(separator: Char, deckStr: String): Deck<PCard> {
            val cardsStr = deckStr.split(separator)
            val deck = Deck<PCard>(cardsStr.size)
            for (cardStr in cardsStr) {
                deck += parse(cardStr)
            }
            return deck
        }

        val DEFAULT_SORTER = Sorter(BY_SUIT, ASCENDING, true,
                intArrayOf(HEART, SPADE, DIAMOND,
                        CLUB, BLACK, RED), true)
    }

    /**
     * A specialized comparator for sorting decks of [PCard].
     * @property order The primary sort field, [BY_RANK] or [BY_SUIT].
     * @property rankOrder The rank order, [ASCENDING] or [DESCENDING].
     * @property aceHigh Whether the ace is considered bigger than king or not.
     * @property suitOrder The suit order, should be an int array of length 6, containing:
     * [PCard.HEART], [PCard.SPADE], [PCard.DIAMOND], [PCard.CLUB],
     * and both [PCard.RED] and [PCard.BLACK] for jokers.
     * First suit in array will be first in deck.
     * @property separateColors If true, the sorter will avoid placing two suits of the same color
     * next to each other. For this, sorter must sort by suit and [suitOrder] must alternate colors.
     */
    class Sorter(private val order: Int,
                 private val rankOrder: Int,
                 private val aceHigh: Boolean,
                 private var suitOrder: IntArray,
                 private val separateColors: Boolean) :
            Card.Sorter<PCard>, Comparator<PCard> {

        override val transitive = !(separateColors && order == BY_SUIT)
        private val originalSuitOrder = suitOrder.clone()

        init {
            require(suitOrder.size == 6) { "Suit order array must have length of 6." }
        }

        override fun initialize(cards: List<PCard>) {
            if (!transitive) {
                val suitsList = originalSuitOrder.toMutableList()

                var suitsFound = 0
                for (card in cards) {
                    suitsFound = suitsFound or (1 shl card.suit)
                }
                suitsFound = suitsFound and 0xF // Ignore red and black
                if (Integer.bitCount(suitsFound) == 3) {
                    // There are 3 suits in the cards, colors may need to be separated.
                    // If there were 2 suits of the same color, nothing could be done.

                    // Find which suit is missing and which suit will be used to separate the
                    // two suits of the same color
                    var missingSuit = -1
                    for (i in SUIT_RANGE) {
                        if (suitsFound and (1 shl i) == 0) {
                            missingSuit = i
                            break
                        }
                    }
                    val separatingSuit = missingSuit + if (missingSuit > 1) -2 else 2

                    // Move the separating suit between the two suit of the same color.
                    var inserted = false
                    for (i in suitsList.indices.reversed()) {
                        val suit = suitsList[i]
                        if (suitsFound and (1 shl suit) != 0) {
                            if (suit == separatingSuit) {
                                suitsList.removeAt(i)
                            } else if (!inserted) {
                                suitsList.add(i, separatingSuit)
                                inserted = true
                            }
                        }
                    }
                }

                suitOrder = suitsList.toIntArray()
            }
        }

        override fun compare(card1: PCard, card2: PCard): Int {
            var result: Int
            if (order == BY_RANK) {
                result = compareRank(rankOrder, aceHigh, card1, card2)
                if (result == 0) result = compareSuit(suitOrder, card1, card2)
            } else {
                result = compareSuit(suitOrder, card1, card2)
                if (result == 0) result = compareRank(rankOrder, aceHigh, card1, card2)
            }
            return result
        }

        private fun compareRank(order: Int, aceHigh: Boolean, card1: PCard, card2: PCard): Int {
            if (card1.rank == card2.rank) return 0
            return (if (card1.greaterThan(card2, aceHigh)) 1 else -1) * order
        }

        private fun compareSuit(order: IntArray, card1: PCard, card2: PCard): Int {
            return order.indexOf(card1.suit).compareTo(order.indexOf(card2.suit))
        }

        companion object {
            const val BY_RANK = 0
            const val BY_SUIT = 1
            const val ASCENDING = 1
            const val DESCENDING = -1
        }

    }

}