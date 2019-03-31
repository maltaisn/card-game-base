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
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.utils.Align
import io.github.maltaisn.cardgame.prefs.GamePref


/**
 * The view for a preference with a value (a [GamePref]).
 */
abstract class GamePrefView<T : GamePref>(skin: Skin, pref: T) : PrefEntryView<T>(skin, pref) {

    override var enabled
        set(value) {
            super.enabled = value
            titleLabel.enabled = value
        }
        get() = super.enabled

    protected val titleLabel: PrefTitleLabel

    /** The listener called when the help icon is clicked. */
    var helpListener: (() -> Unit)?
        set(value) {
            titleLabel.iconClickListener = value
        }
        get() = titleLabel.iconClickListener


    init {
        val style = skin[GamePrefViewStyle::class.java]
        titleLabel = PrefTitleLabel(pref.title, skin, style.titleFontStyle,
                if (pref.help == null) null else style.helpIcon).apply {
            setWrap(true)
            setAlignment(Align.left)
        }
    }


    class GamePrefViewStyle : PrefEntryView.PrefEntryViewStyle() {
        lateinit var helpIcon: Drawable
    }

}