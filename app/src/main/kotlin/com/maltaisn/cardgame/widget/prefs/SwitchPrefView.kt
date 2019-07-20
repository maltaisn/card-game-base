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
import com.maltaisn.cardgame.prefs.PrefEntry
import com.maltaisn.cardgame.prefs.SwitchPref
import com.maltaisn.cardgame.widget.Switch


class SwitchPrefView(skin: Skin, pref: SwitchPref) : GamePrefView<SwitchPref>(skin, pref) {

    override var enabled
        get() = super.enabled
        set(value) {
            super.enabled = value
            switch.enabled = value
        }

    private val switch = Switch(skin)

    init {
        switch.check(pref.value, false)
        switch.checkListener = { pref.value = it }

        pad(20f, 0f, 20f, 0f)
        add(titleLabel).growX().pad(0f, 20f, 0f, 20f)
        add(switch).growY().pad(0f, 20f, 0f, 20f)

        this.enabled = enabled
    }

    override fun onPreferenceValueChanged(pref: PrefEntry) {
        switch.checked = this.pref.value
    }

}
