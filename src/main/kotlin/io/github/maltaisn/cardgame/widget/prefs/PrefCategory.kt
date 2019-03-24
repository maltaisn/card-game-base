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

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Container
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.Align
import io.github.maltaisn.cardgame.widget.SdfLabel
import io.github.maltaisn.cardgame.widget.menu.MenuIcons


/**
 * A preference category for [GamePrefs], used to group and separate preferences.
 */
class PrefCategory : PrefEntry {

    /**
     * The category icon name, `null` to use the default icon.
     * The icons are stored by name in [MenuIcons].
     */
    var icon: String? = null


    // JSON reflection constructor
    constructor() : super()

    constructor(title: String, icon: String? = null) : super(title) {
        this.icon = icon
    }

    override fun createView(skin: Skin): Actor {
        val container = Container<Actor>()
        val label = SdfLabel(title, skin, skin[PrefCategoryStyle::class.java].titleFontStyle)
        label.setWrap(true)
        label.setAlignment(Align.left)
        container.actor = label
        container.fill().pad(20f, 10f, 10f, 10f)
        return container
    }

    class PrefCategoryStyle : PrefEntryStyle()

}