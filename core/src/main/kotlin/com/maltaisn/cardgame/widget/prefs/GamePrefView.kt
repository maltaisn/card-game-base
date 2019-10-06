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
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.utils.Align
import com.maltaisn.cardgame.prefs.GamePref
import ktx.style.get


/**
 * The view for a preference with a value (a [GamePref]).
 */
abstract class GamePrefView<P : GamePref<T>, T : Any>(skin: Skin, pref: P) : PrefEntryView<P>(skin, pref) {

    override var enabled
        get() = super.enabled
        set(value) {
            super.enabled = value
            titleLabel.isDisabled = !value
        }

    protected val titleLabel: PrefTitleLabel

    /**
     * The [PrefsGroup] in which the game preference view is added, or `null` if none is found.
     */
    var prefsGroup: PrefsGroup? = null
        private set


    init {
        val style: GamePrefViewStyle = skin.get()
        titleLabel = PrefTitleLabel(pref.title, skin, style.titleFontStyle,
                if (pref.help == null) null else style.helpIcon).apply {
            setWrap(true)
            setAlignment(Align.left)
            iconClickListener = { prefsGroup?.showHelpText(pref) }
        }
    }


    override fun setStage(stage: Stage?) {
        super.setStage(stage)
        if (stage == null) {
            pref.valueListeners -= ::onPreferenceValueChanged
            prefsGroup = null
        } else {
            pref.valueListeners += ::onPreferenceValueChanged

            var parent = parent
            while (parent !is PrefsGroup?) {
                parent = parent.parent
            }
            prefsGroup = parent
        }
    }


    /**
     * Called when the value of the preference attached to this view is changed.
     */
    protected abstract fun onPreferenceValueChanged(pref: GamePref<T>, value: T)

    /**
     * Must be called when the value of the preference has to be changed to [newValue]
     * by the view. If the preference resets the current game, a dialog will be shown
     * and [revert] may be called if the user decides not to keep the change.
     */
    internal fun changePreferenceValue(newValue: T, revert: () -> Unit) {
        if (pref.confirmChanges) {
            // Confirm if user wants to change preference value or not.
            prefsGroup?.confirmCallback?.invoke(pref) { keep ->
                if (keep) {
                    pref.value = newValue
                } else {
                    revert()
                }
            }
        } else {
            pref.value = newValue
        }
    }


    class GamePrefViewStyle : PrefEntryView.PrefEntryViewStyle() {
        lateinit var helpIcon: Drawable
    }

}
