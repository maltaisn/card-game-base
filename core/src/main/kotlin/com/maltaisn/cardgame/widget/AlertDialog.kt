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

package com.maltaisn.cardgame.widget

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.maltaisn.cardgame.utils.defaultSize
import com.maltaisn.cardgame.widget.text.FontStyle
import com.maltaisn.cardgame.widget.text.SdfLabel
import ktx.style.get


/**
 * A dialog class with a title, a content pane and buttons.
 */
open class AlertDialog(skin: Skin) : Dialog(skin) {

    private val style: AlertDialogStyle = skin.get()

    /**
     * The dialog title, or `null` for no title bar.
     */
    var title: CharSequence? = null
        set(value) {
            field = value
            titleLabel.setText(value)
            val titleCell = content.getCell(titleLabel)
            val sepCell = content.getCell(titleSeparator)
            if (value != null) {
                // Show title bar
                titleCell.defaultSize().pad(30f, 60f, 0f, 60f)
                sepCell.defaultSize().pad(30f, 50f, 0f, 50f)
            } else {
                // Hide title bar
                titleCell.size(0f, 0f).pad(0f)
                sepCell.size(0f, 0f).pad(0f)
            }
            titleSeparator.isVisible = (value != null)
        }

    /**
     * The dialog message, or `null` for none.
     */
    var message: CharSequence? = null
        set(value) {
            field = value
            messageLabel.setText(value)
            val cell = content.getCell(messageLabel)
            if (value != null) {
                // Show message
                cell.defaultSize().pad(30f, 60f, 30f, 60f)
            } else {
                // Hide message
                cell.size(0f, 0f).pad(0f)
            }
        }

    /**
     * The content table of the alert dialog.
     */
    val alertContent = Table()


    val titleLabel = SdfLabel(skin, style.titleFontStyle)
    val messageLabel = SdfLabel(skin, style.messageFontStyle).apply {
        setWrap(true)
    }

    private val titleSeparator = Separator(skin)
    private val buttonBar = Table()


    init {
        content.add(titleLabel).growX().row()
        content.add(titleSeparator).growX().row()
        content.add(messageLabel).growX().row()
        content.add(alertContent).grow().row()
        content.add(buttonBar).growX().row()

        title = null
        message = null
    }

    /**
     * Add a button with a [text] to the button bar and return it.
     */
    fun addButton(text: String): Button {
        if (buttonBar.children.size == 0) {
            buttonBar.pad(30f, 50f, 30f, 50f)
        }
        val btn = Button(skin, text)
        buttonBar.add(btn).growX().space(30f)
        return btn
    }

    /**
     * Remove all buttons from the layout and hide the button bar.
     */
    fun clearButtons() {
        buttonBar.clearChildren()
        buttonBar.pad(0f)
    }

    class AlertDialogStyle {
        lateinit var titleFontStyle: FontStyle
        lateinit var messageFontStyle: FontStyle
    }

}
