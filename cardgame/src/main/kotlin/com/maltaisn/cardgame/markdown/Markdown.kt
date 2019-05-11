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

import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align


/**
 * An object containing elements forming a markdown-like document.
 * Can be parsed from text or from a file to an element tree.
 *
 * Only a subset of the markdown specification in supported:
 *
 * - Header
 *     - 2 sizes with `#` or `##`, must be followed with space or end of line.
 *     - Header icon can be set like that `# ![](drawable) Header name`.
 *
 * - Whitespace and linebreaks
 *     - Empty line for a paragraph break.
 *     - Repeated spaces and line breaks are removed.
 *     - Two spaces or `\` at the end of the line for line break.
 *
 * - Horizontal line: 3 or more asterisks, underscores or hyphens, ignoring whitespaces.
 *
 * - Lists
 *     - Bullet lists with one of `+-*` followed by a space.
 *     - Number lists with a digit followed by a period and a space.
 *     - Up to 3 levels of number and bullet lists, indented with 4 spaces.
 *     - Use 2 more spaces to indent paragraphs in items
 */
class Markdown(content: CharSequence) {

    /** The list of markdown elements elements */
    internal val elements = mutableListOf<MdElement>()


    init {
        // Parse lines
        val lines = content.lines()

        val rootHeader = MdElement.Header(null, 0, null, null)  // Imaginary root header
        var currentHeader = rootHeader
        var currentList: MdElement.List? = null
        var currentText: StringBuilder? = null

        for (line in lines) {
            // Whitespace trimming and line breaks
            var text = line.replace("\t", "    ")
            var indentSize = INDENT_REGEX.find(text)!!.value.length
            text = text.trimStart()
                    .replace(LINE_BREAK_REGEX, "\n")
                    .replace(WHITESPACE_REGEX, " ")

            // Paragraph break
            if (text.isEmpty() && currentText != null) {
                currentText.append("\n\n")
                continue
            }

            // Horizontal line
            if (text.matches(HLINE_REGEX)) {
                (currentList?.elements ?: currentHeader.elements) += MdElement.HLine
                continue
            }

            // List
            val listMatch = LIST_REGEX.find(text)
            if (listMatch != null) {
                val level = indentSize / INDENT_SIZE
                indentSize = (level + 1) * INDENT_SIZE

                currentText = null

                if (currentList != null && level < currentList.level) {
                    while (level < currentList!!.level) {
                        currentList = currentList.parent!!
                    }
                } else if (currentList == null || level > currentList.level) {
                    val type = if (listMatch.value.first().isDigit()) {
                        MdElement.List.Type.NUMBER
                    } else {
                        MdElement.List.Type.BULLET
                    }
                    val newList = MdElement.List(type, level, currentList)
                    (currentList?.elements ?: currentHeader.elements) += newList
                    currentList = newList
                }

                text = listMatch.groupValues[1]
                if (text.isEmpty()) {
                    continue
                }
            }

            // Header
            val headerMatch = HEADER_REGEX.matchEntire(text)
            if (headerMatch != null && currentList?.elements?.isNotEmpty() != false) {
                val size = headerMatch.groupValues[1].length
                val icon = headerMatch.groups[2]?.value
                val title = headerMatch.groupValues[3]

                currentText = null
                currentList = null

                while (size <= currentHeader.size) {
                    currentHeader = currentHeader.parent!!
                }

                val newHeader = MdElement.Header(title, size, icon, currentHeader)
                (currentList?.elements ?: currentHeader.elements) += newHeader
                currentHeader = newHeader
                continue
            }

            // End lists if indent is not big enough
            if (currentText != null && currentText.endsWith('\n')) {
                while (currentList != null && indentSize < currentList.level * INDENT_SIZE + LIST_INDENT_SIZE) {
                    currentList = currentList.parent
                    currentText = null
                }
            }

            // Text
            if (currentText != null) {
                if (!currentText.endsWith('\n')) {
                    currentText.append(' ')
                }
                currentText.append(text)
            } else {
                currentText = StringBuilder(text)
                (currentList?.elements ?: currentHeader.elements) += MdElement.Text(currentText)
            }
        }

        // Transfer elements from root header to markdown object
        elements += rootHeader.elements

        cleanupElements(elements)

        for (element in elements) {
            if (element is MdElement.Header) {
                // Unset root header as parent for top level headers
                element.parent = null
            }
        }
    }

    private fun cleanupElements(elements: MutableList<MdElement>) {
        for (element in elements) {
            if (element is MdElement.Text) {
                // Trim texts and limit line breaks to 2 max in a row
                element.text = element.text.trimEnd().replace(PARAGRAPH_BREAK_REGEX, "\n\n")
            } else if (element is MdElement.Container<*>) {
                cleanupElements(element.elements)
            }
        }
    }

    /** Create a markdown object from a `.md` file. */
    constructor(file: FileHandle, encoding: String) :
            this(file.reader(encoding).use { it.readText() })

    /** Create a view for this markdown object. */
    fun createView(skin: Skin): Table {
        val view = Table()
        view.pad(20f, 20f, 20f, 20f).align(Align.top)
        for (element in elements) {
            view.add(element.createView(skin)).growX().row()
        }
        return view
    }

    companion object {
        private const val INDENT_SIZE = 4
        private const val LIST_INDENT_SIZE = 2

        private val LINE_BREAK_REGEX = """\h*(?:\h{2}|\\)$""".toRegex()
        private val PARAGRAPH_BREAK_REGEX = """\n{2,}""".toRegex()
        private val WHITESPACE_REGEX = """\h{2,}""".toRegex()
        private val INDENT_REGEX = """^\h*""".toRegex()
        private val HEADER_REGEX = """^(#{1,2})\h*(?:\h|$)(?:!\[.*?]\((.+?)\))?\h*(.*?)\h*\1?$""".toRegex()  // https://regex101.com/r/4NREZt/4
        private val LIST_REGEX = """^(?:\d+\.|[-+*])\h+(.*)$""".toRegex()  // https://regex101.com/r/VyGXFB/3
        private val HLINE_REGEX = """^([-_*])\h*(?:\1\h*){2,}$""".toRegex()  // https://regex101.com/r/np52ep/1
    }

}