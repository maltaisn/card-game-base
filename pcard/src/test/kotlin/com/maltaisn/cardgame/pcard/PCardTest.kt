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

package com.maltaisn.cardgame.pcard

import com.maltaisn.cardgame.game.sortWith
import org.junit.Assert.*
import org.junit.Test

internal class PCardTest {

    @Test
    fun parse() {
        val card1 = PCard(PCard.ACE, PCard.DIAMOND)
        val card2 = PCard("A♦")
        assertEquals(card1, card2)

        val card3 = PCard(10, PCard.HEART)
        val card4 = PCard("10♥")
        assertEquals(card3, card4)

        val card5 = PCard.BLACK_JOKER
        val card6 = PCard("*B")
        assertEquals(card5, card6)
    }

    @Test
    fun color() {
        val card1 = PCard(PCard.ACE, PCard.DIAMOND)
        assertEquals(PCard.ACE, card1.rank)
        assertEquals(PCard.DIAMOND, card1.suit)
        assertEquals(PCard.RED, card1.color)

        val card2 = PCard(PCard.KING, PCard.SPADE)
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
        assertFalse(PCard("5♥").greaterThan(PCard("6♣"), false))
        assertTrue(PCard("A♥").greaterThan(PCard("K♦"), true))
        assertFalse(PCard("A♥").greaterThan(PCard("K♦"), false))
        assertFalse(PCard("A♥").greaterThan(PCard("*R"), true))
        assertFalse(PCard("5♥").greaterThan(PCard("5♦"), true))
        assertFalse(PCard("2♥").greaterThan(PCard("A♦"), true))

        assertTrue(PCard("5♥").lessThan(PCard("6♣"), false))
        assertFalse(PCard("A♥").lessThan(PCard("K♦"), true))
        assertTrue(PCard("A♥").lessThan(PCard("K♦"), false))
        assertTrue(PCard("A♥").lessThan(PCard("*R"), true))
        assertFalse(PCard("5♥").lessThan(PCard("5♦"), true))
        assertTrue(PCard("2♥").lessThan(PCard("A♦"), true))
    }

    @Test
    fun equals() {
        val card1 = PCard(PCard.QUEEN, PCard.CLUB)
        val card2 = PCard(PCard.QUEEN, PCard.CLUB)
        assertEquals(card1, card2)
        assertEquals(card1.hashCode(), card2.hashCode())

        val card3 = PCard.BLACK_JOKER
        val card4 = PCard(PCard.JOKER, PCard.BLACK)
        assertEquals(card3, card4)
        assertEquals(card3.hashCode(), card4.hashCode())
    }

    @Test
    fun parseDeck() {
        val deck1 = PCard.parseDeck("A♦", "10♥", "*B")
        val deck2 = mutableListOf(PCard(PCard.ACE, PCard.DIAMOND),
                PCard(10, PCard.HEART), PCard.BLACK_JOKER)
        assertEquals(deck1, deck2)
    }

    @Test
    fun sortBySuit() {
        val sorter1 = PCard.Sorter(PCard.Sorter.BY_SUIT, PCard.Sorter.ASCENDING, true,
                intArrayOf(PCard.HEART, PCard.SPADE, PCard.DIAMOND, PCard.CLUB, PCard.BLACK, PCard.RED), false)
        val deck1 = PCard.parseDeck("2♥", "3♥", "A♥", "3♠", "A♠", "2♣", "K♣", "*B", "*R")
        val deck1shuffled = deck1.shuffled().toMutableList()
        deck1shuffled.sortWith(sorter1)
        assertEquals(deck1, deck1shuffled)
    }

    @Test
    fun sortSeparateColors() {
        val sorter2 = PCard.Sorter(PCard.Sorter.BY_SUIT, PCard.Sorter.ASCENDING, true,
                intArrayOf(PCard.HEART, PCard.SPADE, PCard.DIAMOND, PCard.CLUB, PCard.BLACK, PCard.RED), true)
        val deck2 = PCard.parseDeck("2♥", "3♥", "A♥", "K♣", "2♦", "3♦", "A♦")
        val deck2shuffled = deck2.shuffled().toMutableList()
        deck2shuffled.sortWith(sorter2)
        assertEquals(deck2, deck2shuffled)

        val deck3 = PCard.parseDeck("2♠", "3♠", "A♠", "K♥", "2♣", "3♣", "A♣")
        val deck3shuffled = deck3.shuffled().toMutableList()
        deck3shuffled.sortWith(sorter2)
        assertEquals(deck3, deck3shuffled)
    }

}
