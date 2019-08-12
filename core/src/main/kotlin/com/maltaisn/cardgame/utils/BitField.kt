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


/**
 * A bit field backed by an integer.
 * Flags are defined are bit positions, from 0 to 31.
 */
inline class BitField(val value: Int = 0) {

    /** The number of bits set in this field. */
    val count: Int
        get() = Integer.bitCount(value)

    /** The position of the first bit set, or `-1` if none is set. */
    val firstFlag: Int
        get() {
            for (i in 0..31) {
                if (i in this) return i
            }
            return -1
        }

    /** Returns whether the bit at the position of [flag] is set or not. */
    operator fun contains(flag: Int) = (value and (1 shl flag) != 0)

    /** Set the bit at the position of [flag]. */
    operator fun plus(flag: Int) = BitField(value or (1 shl flag))

    /** Unset the bit at the position of [flag]. */
    operator fun minus(flag: Int) = BitField(value and (1 shl flag).inv())

}
