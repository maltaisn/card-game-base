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

package com.maltaisn.cardgame.tests.core.tests

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.maltaisn.cardgame.CardGameListener
import com.maltaisn.cardgame.tests.core.ActionBarTest
import com.maltaisn.cardgame.widget.Button
import com.maltaisn.cardgame.widget.CardGameLayout
import com.maltaisn.cardgame.widget.Popup
import ktx.actors.onClick


/**
 * Test for [Popup] anchor actor and side, show and hide animations, background rendering, etc.
 */
class PopupTest(listener: CardGameListener) : ActionBarTest(listener) {

    override fun layout(layout: CardGameLayout) {
        super.layout(layout)

        val popupGroup = layout.popupGroup

        val popupSimple = Popup(skin)
        val popupBtn = Button(skin, "Click me!")
        popupBtn.onClick { popupSimple.hide() }
        popupSimple.add(popupBtn)
        popupGroup.addActor(popupSimple)

        val popupComplex = Popup(skin)
        popupComplex.add(Button(skin, "Last two")).fillX().pad(5f)
        popupComplex.add(Button(skin, "Hearts")).fillX().pad(5f).row()
        popupComplex.add(Button(skin, "Barbu")).fillX().pad(5f)
        popupComplex.add(Button(skin, "Queens")).fillX().pad(5f).row()
        popupComplex.add(Button(skin, "Domino")).fillX().pad(5f)
        popupComplex.add(Button(skin, "Tricks")).fillX().pad(5f).row()
        popupComplex.add(Button(skin, "Trump")).pad(5f).colspan(2).fillX()
        popupGroup.addActor(popupComplex)

        val top = Actor()
        val bottom = Actor()
        val left = Actor()
        val right = Actor()

        // Do the layout
        val contentTable = Table().apply {
            add()
            add(top).height(100f)
            add().row()
            add(left).width(100f)
            add().grow()
            add(right).width(100f).row()
            add()
            add(bottom).height(100f)
            add().row()
        }
        layout.centerTable.add(contentTable).grow()

        // Action buttons
        var selectedPopup = popupSimple
        addTwoStateActionBtn("Simple", "Complex") { _, isSimple ->
            selectedPopup = if (isSimple) {
                popupComplex.hide()
                popupSimple
            } else {
                popupSimple.hide()
                popupComplex
            }
        }

        addActionBtn("Top") { selectedPopup.testShow(top, Popup.Side.BELOW) }
        addActionBtn("Left") { selectedPopup.testShow(left, Popup.Side.RIGHT) }
        addActionBtn("Bottom") { selectedPopup.testShow(bottom, Popup.Side.ABOVE) }
        addActionBtn("Right") { selectedPopup.testShow(right, Popup.Side.LEFT) }
        addActionBtn("Center") { selectedPopup.testShow(popupGroup, Popup.Side.CENTER) }

        addToggleBtn("Debug") { _, debug ->
            contentTable.setDebug(debug, true)
            popupGroup.setDebug(debug, true)
        }
    }

    private fun Popup.testShow(actor: Actor, side: Popup.Side) {
        if (shown) {
            hide()
        } else {
            show(actor, side)
        }
    }

}
