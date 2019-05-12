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

import com.maltaisn.cardgame.core.PCard
import junit.framework.Assert.*
import org.junit.Test

internal class PCardTest {

    @Test
    fun parse() {
        val card1 = PCard.get(PCard.ACE, PCard.DIAMOND)
        val card2 = PCard.parse("A♦")
        assertEquals(card1, card2)

        val card3 = PCard.get(10, PCard.HEART)
        val card4 = PCard.parse("10♥")
        assertEquals(card3, card4)

        val card5 = PCard.BLACK_JOKER
        val card6 = PCard.parse("*B")
        assertEquals(card5, card6)
    }

    @Test
    fun color() {
        val card1 = PCard.get(PCard.ACE, PCard.DIAMOND)
        assertEquals(PCard.ACE, card1.rank)
        assertEquals(PCard.DIAMOND, card1.suit)
        assertEquals(PCard.RED, card1.color)

        val card2 = PCard.get(PCard.KING, PCard.SPADE)
        assertEquals(PCard.KING, card2.rank)
        assertEquals(PCard.SPADE, card2.suit)
        assertEquals(PCard.BLACK, card2.color)

        val card3 = PCard.RED_JOKER
        assertEquals(PCard.JOKER, card3.rank)
        assertEquals(PCard.RED, card3.suit)
        assertEquals(PCard.RED, card3.color)
    }

    @Test
    fun comparison() {
        assertFalse(PCard.parse("5♥").greaterThan(PCard.parse("6♣"), false))
        assertTrue(PCard.parse("A♥").greaterThan(PCard.parse("K♦"), true))
        assertFalse(PCard.parse("A♥").greaterThan(PCard.parse("K♦"), false))
        assertFalse(PCard.parse("A♥").greaterThan(PCard.parse("*R"), true))
        assertFalse(PCard.parse("5♥").greaterThan(PCard.parse("5♦"), true))
        assertFalse(PCard.parse("2♥").greaterThan(PCard.parse("A♦"), true))

        assertTrue(PCard.parse("5♥").lessThan(PCard.parse("6♣"), false))
        assertFalse(PCard.parse("A♥").lessThan(PCard.parse("K♦"), true))
        assertTrue(PCard.parse("A♥").lessThan(PCard.parse("K♦"), false))
        assertTrue(PCard.parse("A♥").lessThan(PCard.parse("*R"), true))
        assertFalse(PCard.parse("5♥").lessThan(PCard.parse("5♦"), true))
        assertTrue(PCard.parse("2♥").lessThan(PCard.parse("A♦"), true))
    }

    @Test
    fun equals() {
        val card1 = PCard.get(PCard.QUEEN, PCard.CLUB)
        val card2 = PCard.get(PCard.QUEEN, PCard.CLUB)
        assertEquals(card1, card2)
        assertEquals(card1.hashCode(), card2.hashCode())

        val card3 = PCard.BLACK_JOKER
        val card4 = PCard.get(PCard.JOKER, PCard.BLACK)
        assertEquals(card3, card4)
        assertEquals(card3.hashCode(), card4.hashCode())
    }

    @Test
    fun parseDeck() {
        val deck1 = PCard.parseDeck("A♦", "10♥", "*B")
        val deck2 = Deck(mutableListOf(PCard.get(PCard.ACE, PCard.DIAMOND),
                PCard.get(10, PCard.HEART), PCard.BLACK_JOKER))
        assertEquals(deck1, deck2)
    }

    @Test
    fun sort() {
        // Transitive
        val sorter1 = PCard.Sorter(PCard.Sorter.BY_SUIT, PCard.Sorter.ASCENDING, true,
                intArrayOf(PCard.HEART, PCard.SPADE, PCard.DIAMOND, PCard.CLUB, PCard.BLACK, PCard.RED), false)
        val deck1 = PCard.parseDeck("2♥", "3♥", "A♥", "3♠", "A♠", "2♣", "K♣", "*B", "*R")
        val deck1shuffled = deck1.clone()
        deck1shuffled.shuffle()
        deck1.sortWith(sorter1)
        assertEquals(deck1, deck1shuffled)

        // Non-transitive 1
        val sorter2 = PCard.Sorter(PCard.Sorter.BY_SUIT, PCard.Sorter.ASCENDING, true,
                intArrayOf(PCard.HEART, PCard.SPADE, PCard.DIAMOND, PCard.CLUB, PCard.BLACK, PCard.RED), true)
        val deck2 = PCard.parseDeck("2♥", "3♥", "A♥", "K♣", "3♦", "A♦", "2♦")
        val deck2shuffled = deck2.clone()
        deck2shuffled.shuffle()
        sorter2.initialize(deck2shuffled)
        deck2shuffled.sortWith(sorter2)
        assertEquals(deck2, deck2shuffled)

        // Non-transitive 2
        val deck3 = PCard.parseDeck("2♠", "3♠", "A♠", "K♥", "3♣", "A♣", "2♣")
        val deck3shuffled = deck3.clone()
        deck3shuffled.shuffle()
        sorter2.initialize(deck3shuffled)
        deck2shuffled.sortWith(sorter2)
        assertEquals(deck3, deck3shuffled)
    }

}