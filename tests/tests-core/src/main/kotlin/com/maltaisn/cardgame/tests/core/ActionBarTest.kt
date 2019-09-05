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

package com.maltaisn.cardgame.tests.core

import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.maltaisn.cardgame.widget.CardGameLayout
import com.maltaisn.cardgame.widget.menu.MenuButton
import com.maltaisn.msdfgdx.FontStyle
import ktx.actors.onClick
import java.text.NumberFormat
import kotlin.math.round


/**
 * Base class for a test with a top toolbar with buttons to provide test actions.
 */
abstract class ActionBarTest : CardGameTest() {

    protected val btnTable = Table()

    val btnFontStyle = fontStyle(
            weight = 0.1f,
            size = 32f,
            color = Color.WHITE,
            shadowColor = Color.BLACK)

    override fun layout(layout: CardGameLayout) {
        super.layout(layout)

        layout.centerTable.add(btnTable).growX().colspan(100)
                .pad(50f, 40f, 40f, 40f).row()
    }


    /**
     * Add a button with a [title] and a click [action].
     */
    protected inline fun addActionBtn(title: String,
                                      crossinline action: (MenuButton) -> Unit): MenuButton {
        val btn = MenuButton(skin, btnFontStyle, title, null)
        btn.onClick { action(btn) }
        btnTable.add(btn).grow().pad(0f, 10f, 0f, 10f).expand()
        return btn
    }

    /**
     * Add a button with an on and off state and a title for each, with a click [action].
     */
    protected inline fun addTwoStateActionBtn(titleOn: String, titleOff: String,
                                              crossinline action: (MenuButton, Boolean) -> Unit): MenuButton {
        var state = true
        return addActionBtn(titleOn) {
            state = !state
            it.title = if (state) titleOn else titleOff
            action(it, state)
        }
    }

    /**
     * Add a button that can be toggled on or off with a click [action].
     */
    protected inline fun addToggleBtn(title: String, startState: Boolean = false,
                                      crossinline action: (MenuButton, state: Boolean) -> Unit): MenuButton {
        var state = startState
        val btn = addActionBtn(title) {
            state = !state
            it.checked = state
            action(it, state)
        }
        btn.checked = state
        return btn
    }

    /**
     * Add a button that goes through a list of [values].
     */
    protected fun <T> addEnumBtn(title: String, values: List<T>,
                                 valueTitles: List<Any?>? = values,
                                 initialIndex: Int = 0,
                                 action: (MenuButton, value: T) -> Unit): MenuButton {
        var selectedIndex = initialIndex
        fun getTitle() = title + if (valueTitles != null) ": ${valueTitles[selectedIndex]}" else ""
        return addActionBtn(getTitle()) {
            selectedIndex = (selectedIndex + 1) % values.size
            it.title = getTitle()
            action(it, values[selectedIndex])
        }
    }

    /**
     * Add a button that shows a value between [minValue] and [maxValue], starting at [startValue]
     * and incremented by a [step] value. Left-click to increment and right-click to decrement.
     * [step] value can be negative to change the value in the inverse order.
     * A custom number format can be set for percentages for example.
     */
    protected fun addValueBtn(title: String,
                              minValue: Float, maxValue: Float, startValue: Float, step: Float,
                              numberFmt: NumberFormat? = NumberFormat.getInstance(),
                              action: (MenuButton, Float, Float) -> Unit): ValueMenuButton {
        val btn = ValueMenuButton(skin, btnFontStyle, title, minValue, maxValue,
                startValue, step, numberFmt, action)
        btnTable.add(btn).grow().pad(0f, 10f, 0f, 10f).expand()
        return btn
    }

    class ValueMenuButton(skin: Skin, fontStyle: FontStyle, private val valueTitle: String,
                          minValue: Float, maxValue: Float, startValue: Float, step: Float,
                          private val numberFmt: NumberFormat? = NumberFormat.getInstance(),
                          private val action: (MenuButton, value: Float, oldValue: Float) -> Unit) :
            MenuButton(skin, fontStyle) {

        private val min = step * round(minValue / step)
        private val max = step * round(maxValue / step)

        var value = step * round(startValue / step)
            set(value) {
                val oldValue = field
                field = value
                if (field < min) field = max
                if (field > max) field = min
                updateTitle()
                action(this, field, oldValue)
            }

        init {
            addListener(object : ClickListener(Input.Buttons.LEFT) {
                override fun clicked(event: InputEvent, x: Float, y: Float) {
                    value = step * round((value + step) / step)
                }
            })
            addListener(object : ClickListener(Input.Buttons.RIGHT) {
                override fun clicked(event: InputEvent, x: Float, y: Float) {
                    value = step * round((value - step) / step)
                }
            })
            updateTitle()
        }

        private fun updateTitle() {
            title = if (numberFmt != null) "$valueTitle: ${numberFmt.format(value)}" else valueTitle
        }

    }

}
