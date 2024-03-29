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

import com.badlogic.gdx.utils.Align
import com.maltaisn.cardgame.CardGameListener
import com.maltaisn.cardgame.tests.core.SingleActionTest
import com.maltaisn.cardgame.widget.CardGameLayout
import com.maltaisn.cardgame.widget.PlayerLabel


/**
 * Test [PlayerLabel] layout and style.
 * The layout shows what could be used for most 4 players games.
 */
class PlayerLabelTest(listener: CardGameListener) : SingleActionTest(listener) {

    override fun layout(layout: CardGameLayout) {
        super.layout(layout)

        val northLabel = PlayerLabel(skin, "North")
        val eastLabel = PlayerLabel(skin, "East")
        val southLabel = PlayerLabel(skin, "South")
        val westLabel = PlayerLabel(skin, "West")

        northLabel.score = "+10"
        eastLabel.score = "(13)"
        southLabel.score = "7 (1)"

        // Do the layout
        layout.centerTable.apply {
            pad(60f)
            add(northLabel).expand().align(Align.topRight).padRight(300f).colspan(2).row()
            add(westLabel).expand().align(Align.topLeft).padLeft(100f)
            add(eastLabel).expand().align(Align.bottomRight).padRight(100f).row()
            add(southLabel).expand().align(Align.bottomLeft).padLeft(300f).colspan(2)
        }

        action = {
            isDebugAll = !isDebugAll
        }
    }

}
