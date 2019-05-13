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
import com.maltaisn.cardgame.CardGameLayout
import com.maltaisn.cardgame.tests.core.ActionBarTest
import com.maltaisn.cardgame.widget.Popup
import com.maltaisn.cardgame.widget.PopupButton
import ktx.actors.onClick


/**
 * Test for [Popup] anchor actor and side, show and hide animations, background rendering, etc.
 */
class PopupTest : ActionBarTest() {

    override fun layout(layout: CardGameLayout) {
        super.layout(layout)

        val popupGroup = layout.popupGroup

        val popupSimple = Popup(coreSkin)
        val popupBtn = PopupButton(coreSkin, "Click me!")
        popupBtn.onClick { popupSimple.hide() }
        popupSimple.add(popupBtn)
        popupGroup.addActor(popupSimple)

        val popupComplex = Popup(coreSkin)
        popupComplex.add(PopupButton(coreSkin, "Last two")).fillX()
        popupComplex.add(PopupButton(coreSkin, "Hearts")).fillX().row()
        popupComplex.add(PopupButton(coreSkin, "Barbu")).fillX()
        popupComplex.add(PopupButton(coreSkin, "Queens")).fillX().row()
        popupComplex.add(PopupButton(coreSkin, "Domino")).fillX()
        popupComplex.add(PopupButton(coreSkin, "Tricks")).fillX().row()
        popupComplex.add(PopupButton(coreSkin, "Trump")).colspan(2)
        popupGroup.addActor(popupComplex)

        val top = Actor()
        val bottom = Actor()
        val left = Actor()
        val right = Actor()

        // Do the layout
        val contentTable = Table().apply {
            add()
            add(top).height(50f)
            add().row()
            add(left).width(50f)
            add().grow()
            add(right).width(50f).row()
            add()
            add(bottom).height(50f)
            add().row()
        }
        layout.gameLayer.centerTable.add(contentTable).grow()

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