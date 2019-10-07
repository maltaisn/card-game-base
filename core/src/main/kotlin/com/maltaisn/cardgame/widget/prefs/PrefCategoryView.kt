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

package com.maltaisn.cardgame.widget.prefs

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.Align
import com.maltaisn.cardgame.prefs.PrefCategory
import com.maltaisn.cardgame.widget.Separator
import com.maltaisn.cardgame.widget.menu.ScrollSubMenu
import com.maltaisn.msdfgdx.FontStyle
import com.maltaisn.msdfgdx.widget.MsdfLabel
import ktx.style.get


class PrefCategoryView(skin: Skin, category: PrefCategory) :
        PrefEntryView<PrefCategory>(skin, category), ScrollSubMenu.Section {

    override var enabled
        get() = super.enabled
        set(value) {
            super.enabled = value
            titleLabel.isDisabled = !enabled

            // Enable or disable children views too
            for (child in children) {
                if (child is GamePrefView<*, *>) {
                    child.enabled = enabled && child.pref.enabled
                }
            }
        }

    private var titleLabel: MsdfLabel


    init {
        val style: PrefCategoryViewStyle = skin.get()
        titleLabel = MsdfLabel(category.title, skin, style.titleFontStyle).apply {
            setWrap(true)
            setAlignment(Align.left)
        }
        add(titleLabel).growX().pad(60f, 20f, 60f, 20f).row()

        val prefs = category.prefs.values
        for (pref in prefs) {
            // Preference view
            add(pref.createView(skin)).growX().row()

            // Separator between preferences
            if (pref !== prefs.last()) {
                add(Separator(skin)).growX().pad(10f, 30f, 20f, 0f).row()
            }
        }

        this.enabled = enabled
    }


    class PrefCategoryViewStyle(titleFontStyle: FontStyle)
        : PrefEntryView.PrefEntryViewStyle(titleFontStyle)

}
