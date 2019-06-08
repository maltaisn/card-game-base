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
 * A preference category for [GamePrefs], used to group preferences.
 */
class PrefCategory : PrefEntry() {

    /** The map of preferences in the category. */
    var prefs = linkedMapOf<String, GamePref>()

    /** The category icon name, a drawable in the core skin, or `null` to use the default icon. */
    var icon: String? = null


    override var enabled: Boolean
        get() = super.enabled
        set(value) {
            super.enabled = value

            // Change the enabled state for children too.
            for (pref in prefs.values) {
                pref.enabled = value
            }
        }


    override fun createView(skin: Skin) = PrefCategoryView(skin, this)

    override fun toString() = super.toString().dropLast(1) + ", ${prefs.size} preferences]"

}