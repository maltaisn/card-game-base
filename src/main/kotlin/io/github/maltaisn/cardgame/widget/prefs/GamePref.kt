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

import com.badlogic.gdx.Preferences
import com.badlogic.gdx.scenes.scene2d.utils.Drawable


/**
 * The base class for a preference in [GamePrefs].
 */
abstract class GamePref : PrefEntry {

    /** Key under which the preference value is saved. */
    lateinit var key: String

    /** Optional help message shown to the user. */
    var help: String? = null

    // JSON reflection constructor
    constructor() : super()

    constructor(key: String, title: String, help: String? = null) : super(title) {
        this.key = key
        this.help = help
    }

    /**
     * Load the value of this preference from [prefs] into [progress].
     */
    abstract fun loadValue(prefs: Preferences)

    /**
     * Save the value of this preference to [prefs], if it's not null.
     * Doesn't flush the preferences, must be done after.
     */
    abstract fun saveValue(prefs: Preferences, flush: Boolean = true)

    override fun toString() = "[key: \"$key\", title: \"$title\"]"


    abstract class GamePrefStyle : PrefEntryStyle() {
        lateinit var helpIcon: Drawable
    }

}