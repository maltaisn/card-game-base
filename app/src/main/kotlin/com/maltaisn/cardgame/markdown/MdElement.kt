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

package com.maltaisn.cardgame.markdown

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.maltaisn.cardgame.widget.markdown.MdHLineView
import com.maltaisn.cardgame.widget.markdown.MdHeaderView
import com.maltaisn.cardgame.widget.markdown.MdListView
import com.maltaisn.cardgame.widget.markdown.MdTextView


sealed class MdElement {

    /** Create a view for this markdown element. */
    abstract fun createView(skin: Skin): Actor

    abstract class Container<T>(var parent: T?,
                                val elements: MutableList<MdElement> = mutableListOf()) : MdElement()

    /** Header element. */
    class Header(var text: CharSequence?,
                 var size: Int,
                 var icon: String?,
                 parent: Header?) : Container<Header>(parent) {

        override fun createView(skin: Skin) = MdHeaderView(skin, this)

        override fun equals(other: Any?) = (other === this ||
                other is Header && other.text.toString() == text.toString() &&
                other.size == size && other.icon == icon && other.elements == elements)

        override fun hashCode() = text.hashCode()

        override fun toString() = "Header[text: \"$text\", size: $size, icon: $icon, elements: $elements]"
    }

    /** List element. */
    class List(val type: Type,
               val level: Int,
               parent: List?) : Container<List>(parent) {

        override fun createView(skin: Skin) = MdListView(skin, this)

        override fun equals(other: Any?) = (other === this || other is List && other.type == type &&
                other.level == level && other.elements == elements)

        override fun hashCode() = type.hashCode()

        override fun toString() = "List[type: $type, level: $level, elements: $elements]"

        /**
         * Returns the display string for a list [item] for a number list.
         * @param item Item position, starts at 1.
         */
        internal fun getItemMarker(item: Int) = when (level) {
            0 -> item.toString()
            1 -> {
                // Roman number
                if (item >= 5000) {
                    item.toString()
                } else {
                    var n = item
                    var result = ""
                    for ((multiple, numeral) in ROMAN_NUMERALS.entries) {
                        while (n >= multiple) {
                            n -= multiple
                            result += numeral
                        }
                    }
                    result
                }
            }
            else -> {
                // Excel column-like: a-z, aa, etc
                var n = item
                var result = ""
                do {
                    n -= 1
                    result = ('a' + n % 26) + result
                    n /= 26
                } while (n > 0)
                result
            }
        }

        enum class Type {
            BULLET, NUMBER
        }

        companion object {
            private val ROMAN_NUMERALS = mapOf(
                    1000 to "m",
                    900 to "cm",
                    500 to "d",
                    400 to "cd",
                    100 to "c",
                    90 to "xc",
                    50 to "l",
                    40 to "xl",
                    10 to "x",
                    9 to "ix",
                    5 to "v",
                    4 to "iv",
                    1 to "i"
            )
        }
    }


    /** Text element. */
    class Text(var text: CharSequence) : MdElement() {

        override fun createView(skin: Skin) = MdTextView(skin, this)

        override fun equals(other: Any?) = (other === this || other is Text &&
                other.text.toString() == text.toString())

        override fun hashCode() = text.hashCode()

        override fun toString() = "Text[text: \"$text\"]"
    }

    /** Horizontal line element. */
    object HLine : MdElement() {

        override fun createView(skin: Skin) = MdHLineView(skin)

        override fun toString() = "HLine"
    }

}