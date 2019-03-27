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

package io.github.maltaisn.cardgame.prefs

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import io.github.maltaisn.cardgame.widget.prefs.PrefEntryView


/**
 * An entry in a [GamePrefs] object.
 * Can be either a preference or a category.
 */
abstract class PrefEntry {

    /** The preference entry title. */
    var title = ""


    /** Create a view for this preference. */
    abstract fun createView(skin: Skin): PrefEntryView<out PrefEntry>


    override fun toString() = "[title: $title]"

}