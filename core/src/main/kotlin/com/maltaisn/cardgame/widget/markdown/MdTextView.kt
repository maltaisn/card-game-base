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

import com.badlogic.gdx.scenes.scene2d.ui.Container
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.maltaisn.cardgame.markdown.MdElement
import com.maltaisn.msdfgdx.FontStyle
import com.maltaisn.msdfgdx.widget.MsdfLabel
import ktx.style.get


class MdTextView(skin: Skin, element: MdElement.Text) : Container<MsdfLabel>() {

    init {
        val style: MdTextViewStyle = skin.get()
        actor = MsdfLabel(element.text, skin, style.fontStyle)
        actor.setWrap(true)
        fill().pad(10f, 0f, 20f, 0f)
    }

    class MdTextViewStyle(val fontStyle: FontStyle)

}
