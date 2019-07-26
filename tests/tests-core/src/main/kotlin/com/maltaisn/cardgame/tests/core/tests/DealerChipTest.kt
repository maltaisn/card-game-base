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
import com.badlogic.gdx.utils.Align
import com.maltaisn.cardgame.tests.core.ActionBarTest
import com.maltaisn.cardgame.widget.CardGameLayout
import com.maltaisn.cardgame.widget.DealerChip


/**
 * Test for [DealerChip] anchor actor and side, show/hide/move animations.
 */
class DealerChipTest : ActionBarTest() {

    override fun layout(layout: CardGameLayout) {
        super.layout(layout)

        val popupGroup = layout.popupGroup

        val chip = DealerChip(skin)
        popupGroup.addActor(chip)

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
        layout.gameLayer.centerTable.add(contentTable).grow()

        // Action buttons
        var fadeModeEnabled = true
        addTwoStateActionBtn("Fade", "Move") { _, state ->
            fadeModeEnabled = state
        }

        fun doAction(actor: Actor, side: Int) {
            if (chip.shown) {
                if (fadeModeEnabled) {
                    chip.hide()
                } else {
                    chip.moveTo(actor, side)
                }
            } else {
                chip.show(actor, side)
            }
        }

        addActionBtn("Top") { doAction(top, Align.bottom) }
        addActionBtn("Left") { doAction(left, Align.right) }
        addActionBtn("Bottom") { doAction(bottom, Align.top) }
        addActionBtn("Right") { doAction(right, Align.left) }
        addActionBtn("Center") { doAction(popupGroup, Align.center) }

        addToggleBtn("Debug") { _, debug ->
            contentTable.setDebug(debug, true)
            popupGroup.setDebug(debug, true)
        }
    }

}
