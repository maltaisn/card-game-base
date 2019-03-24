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
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import io.github.maltaisn.cardgame.widget.Switch


/**
 * A boolean preference that shows a switch to the user.
 */
class SwitchPref : GamePref {

    /** The switch value. */
    var value = false

    /** The switch default value. */
    var defaultValue = false

    // JSON reflection constructor
    constructor() : super()

    constructor(key: String, title: String, help: String? = null,
                default: Boolean) : super(key, title, help) {
        defaultValue = default
    }

    override fun loadValue(prefs: Preferences) {
        value = prefs.getBoolean(key, defaultValue)
    }

    override fun saveValue(prefs: Preferences, flush: Boolean) {
        prefs.putBoolean(key, value)
        if (flush) prefs.flush()
    }

    override fun createView(skin: Skin): Table {
        val style = skin[SwitchPrefStyle::class.java]
        val table = Table()

        val helpIcon = if (help == null) null else style.helpIcon
        val label = PrefTitleLabel(title, skin, style.titleFontStyle, helpIcon)
        val switch = Switch(style.switchStyle)

        label.setWrap(true)
        label.setAlignment(Align.left)

        switch.check(value, false)
        switch.checkListener = { value = it }

        table.pad(5f, 0f, 5f, 0f)
        table.add(label).growX().pad(5f, 10f, 5f, 15f)
        table.add(switch).pad(5f, 5f, 5f, 10f)
        return table
    }

    override fun toString() = super.toString().dropLast(1) + ", value: $value, defaultValue: $defaultValue]"

    class SwitchPrefStyle : GamePrefStyle() {
        lateinit var switchStyle: Switch.SwitchStyle
    }

}