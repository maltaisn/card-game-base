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

package io.github.maltaisn.cardgame.widget.prefs

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.Align
import io.github.maltaisn.cardgame.prefs.PrefCategory
import io.github.maltaisn.cardgame.widget.SdfLabel


class PrefCategoryView(skin: Skin, category: PrefCategory) :
        PrefEntryView<PrefCategory>(skin, category) {

    init {
        val style = skin[PrefCategoryViewStyle::class.java]
        val label = SdfLabel(category.title, skin, style.titleFontStyle)
        label.setWrap(true)
        label.setAlignment(Align.left)
        add(label).grow().pad(20f, 10f, 10f, 10f)
    }

    class PrefCategoryViewStyle : PrefEntryView.PrefEntryViewStyle()

}