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

package com.maltaisn.cardgame.widget.markdown

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.maltaisn.cardgame.markdown.MdElement
import com.maltaisn.cardgame.widget.Separator
import com.maltaisn.cardgame.widget.menu.ScrollSubMenu
import com.maltaisn.msdfgdx.FontStyle
import com.maltaisn.msdfgdx.widget.MsdfLabel
import ktx.style.get


class MdHeaderView(skin: Skin, header: MdElement.Header) : Table(), ScrollSubMenu.Section {

    init {
        val style: MdHeaderViewStyle = skin.get()
        val headerLabel = MsdfLabel(header.text, skin, style.fontStyles[header.size - 1])
        headerLabel.setWrap(true)
        add(headerLabel).growX().pad(40f, 0f, 10f, 0f).row()

        if (header.size == 1) {
            add(Separator(skin)).growX().pad(40f, 0f, 20f, 0f).row()
        }

        for (element in header.elements) {
            add(element.createView(skin)).growX().padBottom(10f).row()
        }
    }

    /**
     * @property fontStyles Array of header font styles, from big to small.
     */
    class MdHeaderViewStyle(val fontStyles: List<FontStyle>)

}
