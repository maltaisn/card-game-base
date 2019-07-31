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

package com.maltaisn.cardgame.game


/**
 * Draw and returns the bottom card of a card list.
 */
fun <T : Card> MutableList<T>.drawBottom() = removeAt(0)

/**
 * Draw and returns [count] cards from the bottom of a card list.
 */
fun <T : Card> MutableList<T>.drawBottom(count: Int): MutableList<T> {
    val cards = subList(0, count)
    val drawn = cards.toMutableList()
    cards.clear()
    return drawn
}

/**
 * Draw and returns the top card of a card list.
 */
fun <T : Card> MutableList<T>.drawTop() = removeAt(size - 1)

/**
 * Draw and returns [count] cards from the top of a card list.
 */
fun <T : Card> MutableList<T>.drawTop(count: Int): MutableList<T> {
    val cards = subList(size - count, size)
    val drawn = cards.toMutableList()
    cards.clear()
    return drawn
}