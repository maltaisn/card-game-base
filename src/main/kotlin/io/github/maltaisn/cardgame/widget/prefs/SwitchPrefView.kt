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
import io.github.maltaisn.cardgame.prefs.SwitchPref
import io.github.maltaisn.cardgame.widget.Switch


class SwitchPrefView(skin: Skin, pref: SwitchPref) : GamePrefView<SwitchPref>(skin, pref) {

    override var enabled
        set(value) {
            super.enabled = value
            switch.enabled = value
        }
        get() = super.enabled

    private val switch: Switch

    init {
        val style = skin[SwitchPrefViewStyle::class.java]

        switch = Switch(style.switchStyle)
        switch.check(pref.value, false)
        switch.checkListener = { pref.value = it }
        switch.enabled = enabled

        pad(5f, 0f, 5f, 0f)
        add(titleLabel).growX().pad(5f, 10f, 5f, 15f)
        add(switch).pad(5f, 5f, 5f, 10f)
    }

    override fun onPreferenceValueChanged() {
        switch.checked = pref.value
    }

    class SwitchPrefViewStyle {
        lateinit var switchStyle: Switch.SwitchStyle
    }

}