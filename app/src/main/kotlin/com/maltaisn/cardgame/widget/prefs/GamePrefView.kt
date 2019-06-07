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
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.utils.Align
import com.maltaisn.cardgame.prefs.GamePref
import ktx.style.get


/**
 * The view for a preference with a value (a [GamePref]).
 */
abstract class GamePrefView<T : GamePref>(skin: Skin, pref: T) : PrefEntryView<T>(skin, pref) {

    override var enabled
        get() = super.enabled
        set(value) {
            super.enabled = value
            titleLabel.enabled = value
        }

    protected val titleLabel: PrefTitleLabel

    /** The listener called when the help icon is clicked. */
    var helpListener: (() -> Unit)?
        get() = titleLabel.iconClickListener
        set(value) {
            titleLabel.iconClickListener = value
        }


    init {
        val style: GamePrefViewStyle = skin.get()
        titleLabel = PrefTitleLabel(skin, style.titleFontStyle, pref.title,
                if (pref.help == null) null else style.helpIcon).apply {
            setWrap(true)
            setAlignment(Align.left)
        }
    }


    class GamePrefViewStyle : PrefEntryView.PrefEntryViewStyle() {
        lateinit var helpIcon: Drawable
    }

}