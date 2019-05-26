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

import junit.framework.Assert.assertEquals
import org.junit.Test


internal class DeckTest {

    @Test
    fun drawTop() {
        val deck1 = PCard.parseDeck("2♥", "3♥", "4♥")
        deck1.drawTop()
        assertEquals(PCard.parseDeck("2♥", "3♥"), deck1)
    }

    @Test
    fun drawTopMany() {
        val deck2 = PCard.parseDeck("2♥", "3♥", "4♥", "5♥", "6♥")
        deck2.drawTop(3)
        assertEquals(PCard.parseDeck("2♥", "3♥"), deck2)
    }

    @Test
    fun drawBottom() {
        val deck1 = PCard.parseDeck("2♥", "3♥", "4♥")
        deck1.drawBottom()
        assertEquals(PCard.parseDeck("3♥", "4♥"), deck1)
    }

    @Test
    fun drawBottomMany() {
        val deck2 = PCard.parseDeck("2♥", "3♥", "4♥", "5♥", "6♥")
        deck2.drawBottom(3)
        assertEquals(PCard.parseDeck("5♥", "6♥"), deck2)
    }
}