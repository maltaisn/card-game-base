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

import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.maltaisn.cardgame.prefs.PrefEntry
import com.maltaisn.cardgame.widget.FontStyle


/**
 * Base class for a preference view.
 * The view attaches a listener to the preference when added to the stage and
 * the listener is removed when removed from the stage.
 */
abstract class PrefEntryView<T : PrefEntry>(skin: Skin, val pref: T) :
        Table(skin), PrefEntry.PrefListener {

    /** Whether the preference view is enabled or not. */
    open var enabled = pref.enabled


    override fun setStage(stage: Stage?) {
        super.setStage(stage)
        if (stage == null) {
            pref.listeners -= this
        } else {
            pref.listeners += this
        }
    }

    final override fun onPreferenceEnabledStateChanged(pref: PrefEntry, enabled: Boolean) {
        val categoryEnabled = (parent as? PrefCategoryView)?.pref?.enabled ?: true
        this.enabled = categoryEnabled && enabled
    }


    abstract class PrefEntryViewStyle {
        lateinit var titleFontStyle: FontStyle
    }

}