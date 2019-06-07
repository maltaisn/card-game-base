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

import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.maltaisn.cardgame.markdown.MdElement
import com.maltaisn.cardgame.widget.FontStyle
import com.maltaisn.cardgame.widget.SdfLabel
import com.maltaisn.cardgame.widget.menu.MenuContentSection
import ktx.style.get


class MdListView(skin: Skin, list: MdElement.List) : Table(), MenuContentSection {

    init {
        val style: MdListViewStyle = skin.get()
        pad(0f, 5f, 10f, 0f)

        var n = 1
        for (element in list.elements) {
            if (element !is MdElement.List) {
                // Add list item marker
                if (list.type == MdElement.List.Type.NUMBER) {
                    val label = SdfLabel(skin, style.numberFontStyle, list.getItemMarker(n) + '.')
                    label.setAlignment(Align.right)
                    add(label).minWidth(30f).pad(5f, 5f, 10f, 5f)
                } else {
                    val bullet = Image(style.bulletDrawables[list.level], Scaling.fit)
                    add(bullet).size(10f, 30f).pad(5f, 5f, 10f, 5f)
                }.align(Align.top)
                n++
            } else {
                add()
            }

            add(element.createView(skin)).growX().padLeft(10f).row()
        }
    }


    class MdListViewStyle {
        lateinit var numberFontStyle: FontStyle
        lateinit var bulletDrawables: Array<Drawable>
    }

}