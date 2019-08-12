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

package com.maltaisn.cardgame.utils

import org.junit.Assert.*
import org.junit.Test


internal class BitFieldTest {

    @Test
    fun addFlags() {
        var bf = BitField()

        assertEquals(0, bf.value)

        bf += 0
        assertEquals(1, bf.value)
        bf += 1
        assertEquals(3, bf.value)
        bf += 31
        assertEquals(3 + (1 shl 31), bf.value)
    }

    @Test
    fun removeFlags() {
        var bf = BitField()
        bf += 0
        bf += 1
        bf += 2

        assertEquals(7, bf.value)

        bf -= 0
        assertEquals(6, bf.value)
        bf -= 1
        assertEquals(4, bf.value)
        bf -= 2
        assertEquals(0, bf.value)
    }

    @Test
    fun count() {
        assertEquals(31, BitField(Int.MAX_VALUE).count)
        assertEquals(0, BitField().count)
        assertEquals(1, BitField(1).count)
        assertEquals(6, BitField(5674).count)
    }

    @Test
    fun contains() {
        assertTrue(0 in BitField(1))
        assertTrue(1 in BitField(2))
        assertTrue(6 in BitField(Int.MAX_VALUE))
        assertFalse(0 in BitField())
    }

    @Test
    fun firstFlag() {
        assertEquals(-1, BitField().firstFlag)
        assertEquals(0, BitField(1).firstFlag)
        assertEquals(0, BitField(Int.MAX_VALUE).firstFlag)
        assertEquals(13, BitField(1 shl 13).firstFlag)
    }

}
