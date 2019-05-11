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

import org.junit.Assert.assertEquals
import org.junit.Test


internal class MarkdownTest {

    @Test
    fun headers() {
        assertMarkdownHas("""
            |# Header 1
            |## Header 1.1
            |# Header 2 ##
        """.trimMargin(),

                MdElement.Header("Header 1", 1, null, null).apply {
                    elements += MdElement.Header("Header 1.1", 2, null, this)
                }, MdElement.Header("Header 2 #", 1, null, null))
    }

    @Test
    fun header_icon() {
        assertMarkdownHas("""
            |# ![](icon-1) Header 1 #
            |## ![](icon-2) Header 1.1
        """.trimMargin(),

                MdElement.Header("Header 1", 1, "icon-1", null).apply {
                    elements += MdElement.Header("Header 1.1", 2, "icon-2", this)
                })
    }

    @Test
    fun hline() {
        assertMarkdownHas("""
            | ***
            | - - -
            | __   _
            | -*_
        """.trimMargin(),
                MdElement.HLine,
                MdElement.HLine,
                MdElement.HLine,
                MdElement.Text("-*_"))
    }

    @Test
    fun line_breaks() {
        assertMarkdownHas("""
            |line 1\
            |line 2${"  "}
            |line 3
            |
            |# Header
            |line 4
            |
            |
            |## Sub-header
            |line 5
            |line 5
            |
        """.trimMargin(),
                MdElement.Text("line 1\nline 2\nline 3"),
                MdElement.Header("Header", 1, null, null).apply {
                    elements += MdElement.Text("line 4")
                    elements += MdElement.Header("Sub-header", 2, null, null).apply {
                        elements += MdElement.Text("line 5 line 5")
                    }
                })
    }

    @Test
    fun whitespace() {
        assertMarkdownHas("""
            |    Hello
            |  world         !
            |
            |
        """.trimMargin(),
                MdElement.Text("""
                    |Hello world !
                """.trimMargin()))
    }

    @Test
    fun list_simple() {
        assertMarkdownHas("""
            |- Item 1
            |1. Item 2
            |* Item 3
            |is a bit longer
        """.trimMargin(),
                MdElement.List(MdElement.List.Type.BULLET, 0, null).apply {
                    elements += MdElement.Text("Item 1")
                    elements += MdElement.Text("Item 2")
                    elements += MdElement.Text("Item 3 is a bit longer")
                })
    }

    @Test
    fun list_levels() {
        assertMarkdownHas("""
            |1. Item 1
            |    - Item 1.1
            |        1. Item 1.1.1
            |        2. Item 1.1.2
            |    * Item 1.2
            |2. Item 2
            |    1. Item 2.1
        """.trimMargin(),
                MdElement.List(MdElement.List.Type.NUMBER, 0, null).apply {
                    elements += MdElement.Text("Item 1")
                    elements += MdElement.List(MdElement.List.Type.BULLET, 1, this).apply {
                        elements += MdElement.Text("Item 1.1")
                        elements += MdElement.List(MdElement.List.Type.NUMBER, 2, this).apply {
                            elements += MdElement.Text("Item 1.1.1")
                            elements += MdElement.Text("Item 1.1.2")
                        }
                        elements += MdElement.Text("Item 1.2")
                    }
                    elements += MdElement.Text("Item 2")
                    elements += MdElement.List(MdElement.List.Type.NUMBER, 1, this).apply {
                        elements += MdElement.Text("Item 2.1")
                    }
                })
    }

    @Test
    fun list_special_elements() {
        assertMarkdownHas("""
            |- # Header in item
            |    - Normal text
            |      ## Header!!!
            |1. ***
        """.trimMargin(),
                MdElement.List(MdElement.List.Type.BULLET, 0, null).apply {
                    elements += MdElement.Text("# Header in item")
                    elements += MdElement.List(MdElement.List.Type.BULLET, 1, this).apply {
                        elements += MdElement.Text("Normal text")
                    }
                },
                MdElement.Header("Header!!!", 2, null, null).apply {
                    elements += MdElement.List(MdElement.List.Type.NUMBER, 0, null).apply {
                        elements += MdElement.Text("***")
                    }
                })
    }

    @Test
    fun list_indent() {
        assertMarkdownHas("""
            |0. Item 1
            |
            |Paragraph
            |
            |1. Item 2
            |
            |  Still
            |  in item
        """.trimMargin(),
                MdElement.List(MdElement.List.Type.NUMBER, 0, null).apply {
                    elements += MdElement.Text("Item 1")
                },
                MdElement.Text("Paragraph"),
                MdElement.List(MdElement.List.Type.NUMBER, 0, null).apply {
                    elements += MdElement.Text("Item 2\n\nStill in item")
                })
    }

    @Test
    fun list_number_markers() {
        val level0 = MdElement.List(MdElement.List.Type.NUMBER, 0, null)
        assertEquals("1", level0.getItemMarker(1))
        assertEquals("10", level0.getItemMarker(10))

        val level1 = MdElement.List(MdElement.List.Type.NUMBER, 1, null)
        assertEquals("i", level1.getItemMarker(1))
        assertEquals("iv", level1.getItemMarker(4))
        assertEquals("lxxix", level1.getItemMarker(79))
        assertEquals("mmmmdccclxxxviii", level1.getItemMarker(4888))

        val level2 = MdElement.List(MdElement.List.Type.NUMBER, 2, null)
        assertEquals("a", level2.getItemMarker(1))
        assertEquals("z", level2.getItemMarker(26))
        assertEquals("aa", level2.getItemMarker(27))
        assertEquals("all", level2.getItemMarker(1000))
        assertEquals("rfu", level2.getItemMarker(12345))
    }

    private fun assertMarkdownHas(content: String, vararg elements: MdElement) {
        assertEquals(elements.toMutableList(), Markdown(content).elements)
    }

}