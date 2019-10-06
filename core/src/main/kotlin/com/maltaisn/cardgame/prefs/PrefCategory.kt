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

package com.maltaisn.cardgame.prefs

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.maltaisn.cardgame.widget.prefs.PrefCategoryView


/**
 * A preference category for grouping multiple [GamePref] under a section.
 *
 * @property prefs The map of preferences in the category by key.
 * @property icon The category icon name, a drawable in the core skin, or `null` to use the default icon.
 */
class PrefCategory(
        key: String,
        title: String,
        dependency: String?,
        val prefs: Map<String, GamePref<*>>,
        val icon: String?)
    : PrefEntry(key, title, dependency) {


    override fun createView(skin: Skin) = PrefCategoryView(skin, this)


    class Builder(val key: String) : GamePrefs.CategoryBuilder() {

        var title = ""
        var dependency: String? = null
        var icon: String? = null

        @Suppress("UNCHECKED_CAST")
        fun build() = PrefCategory(key, title, dependency,
                prefs as Map<String, GamePref<*>>, icon)
    }

    override fun toString() = "PrefCategory[${prefs.size} prefs, icon: $icon, " +
            super.toString().substringAfter("[")

}
