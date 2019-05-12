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

import com.badlogic.gdx.Preferences
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.maltaisn.cardgame.widget.prefs.GamePrefView


/**
 * The base class for a preference in [GamePrefs].
 */
abstract class GamePref : PrefEntry() {

    /** Optional help message shown to the user, use `null` for no help message. */
    var help: String? = null

    /** Optional shorter title to use in menu drawer, use `null` to use normal title. */
    var shortTitle: String? = null

    /**
     * Load the value of this preference from [prefs] into [progress].
     */
    internal abstract fun loadValue(prefs: Preferences)

    /**
     * Save the value of this preference to [prefs], if it's not null.
     * Doesn't flush the preferences, must be done after.
     */
    internal abstract fun saveValue(prefs: Preferences)


    abstract override fun createView(skin: Skin): GamePrefView<out GamePref>

}