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

package com.maltaisn.cardgame.tests.desktop

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.maltaisn.cardgame.CardGameApp
import com.maltaisn.cardgame.tests.core.CardGameTests
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Font
import java.awt.Insets
import java.util.prefs.Preferences
import javax.swing.*
import javax.swing.border.EmptyBorder


object DesktopLauncher {

    @JvmStatic
    fun main(args: Array<String>) {
        val prefs = Preferences.userNodeForPackage(DesktopLauncher::class.java)
        val lastTest = prefs.get("lastTest", null)

        val frame = JFrame().apply {
            defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            size = Dimension(300, 500)
            title = "Card Game Tests"
            setLocationRelativeTo(null)
        }

        val listModel = DefaultListModel<String>()
        for (testName in CardGameTests.TESTS_MAP.keys) {
            listModel.addElement(testName)
        }

        val list = JList(listModel).apply {
            selectionMode = ListSelectionModel.SINGLE_SELECTION
            selectedIndex = listModel.indexOf(lastTest)
            layoutOrientation = JList.VERTICAL
            font = Font(font.name, Font.PLAIN, 16)
            border = EmptyBorder(5, 5, 5, 5)
        }

        JScrollPane().apply {
            setViewportView(list)
            frame.add(this, BorderLayout.CENTER)
        }
        list.ensureIndexIsVisible(list.selectedIndex)

        JButton("Run test").apply {
            font = Font(font.name, Font.BOLD, 16)
            margin = Insets(15, 15, 15, 15)
            addActionListener {
                val testName = list.selectedValue
                if (testName != null) {
                    prefs.put("lastTest", testName)

                    // Run selected test
                    runTest(testName)
                }
            }
            frame.add(this, BorderLayout.PAGE_END)
        }

        frame.isVisible = true
    }

    private fun runTest(testName: String) {
        Lwjgl3Application(object : CardGameApp() {
            override fun create() {
                setScreen(CardGameTests.newTest(testName))
            }
        }, Lwjgl3ApplicationConfiguration().apply {
            setTitle("Cards")
            setWindowedMode(1440, 810)
            setWindowSizeLimits(960, 540, -1, -1)
        })
    }

}
