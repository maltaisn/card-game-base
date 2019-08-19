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

import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.maltaisn.cardgame.utils.findScrollFocus
import com.maltaisn.cardgame.widget.CardGameLayout


/**
 * An action bar test with a predefined content group with the same background
 * as the submenu content group. This allows to test components on the right background.
 */
abstract class SubmenuContentTest : ActionBarTest() {

    override fun layout(layout: CardGameLayout) {
        super.layout(layout)

        val content = Table()
        content.background = skin.getDrawable("submenu-content-background")
        layout.centerTable.add(content).grow()
                .pad(0f, 80f, 0f, 80f)
        layoutContent(layout, content)
        content.findScrollFocus()
    }

    protected abstract fun layoutContent(layout: CardGameLayout, content: Table)

}
