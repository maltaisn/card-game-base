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

import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.maltaisn.cardgame.prefs.GamePref
import com.maltaisn.cardgame.prefs.ListPref
import com.maltaisn.cardgame.prefs.PrefCategory
import com.maltaisn.cardgame.widget.SdfLabel
import com.maltaisn.cardgame.widget.menu.MenuContentSection


class PrefCategoryView(skin: Skin, category: PrefCategory) :
        PrefEntryView<PrefCategory>(skin, category), MenuContentSection {

    override var enabled
        get() = super.enabled
        set(value) {
            super.enabled = value
            titleLabel.enabled = enabled

            // Enable or disable all children views
            for (view in children) {
                if (view is PrefEntryView<*>) {
                    view.enabled = value && view.pref.enabled
                }
            }
        }

    /** Listener called when a preference help icon is clicked in a preference of the category. */
    var helpListener: ((GamePref) -> Unit)? = null

    /** Listener called when a list preference value is clicked. */
    var listClickListener: ((ListPref) -> Unit)? = null

    private var titleLabel: SdfLabel


    init {
        val style = skin[PrefCategoryViewStyle::class.java]
        titleLabel = SdfLabel(skin, style.titleFontStyle, category.title).apply {
            setWrap(true)
            setAlignment(Align.left)
        }
        add(titleLabel).growX().pad(30f, 10f, 30f, 10f).row()

        val prefs = category.prefs.values
        for (pref in prefs) {
            // Preference view
            val view = pref.createView(skin)
            view.helpListener = {
                helpListener?.invoke(view.pref)
            }
            if (view is ListPrefView) {
                view.valueClickListener = {
                    listClickListener?.invoke(view.pref)
                }
            }
            add(view).growX().row()

            // Separator between preferences
            if (pref !== prefs.last()) {
                val separator = Image(style.separator, Scaling.stretchX)
                add(separator).growX().pad(10f, 15f, 10f, 0f).row()
            }
        }

        this.enabled = enabled
    }

    override fun detachListener() {
        // Detach all preferences listeners
        super.detachListener()
        for (child in children) {
            if (child is GamePrefView<*>) {
                child.detachListener()
            }
        }
        helpListener = null
        listClickListener = null
    }


    class PrefCategoryViewStyle : PrefEntryView.PrefEntryViewStyle() {
        lateinit var separator: Drawable
    }

}