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

import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.utils.Scaling
import com.maltaisn.cardgame.widget.FontStyle
import com.maltaisn.cardgame.widget.SdfLabel
import ktx.style.get


class PlayerLabel(skin: Skin, name: CharSequence? = null) : Table() {

    val style: PlayerLabelStyle = skin.get()

    /**
     * The player name displayed, `null` for none.
     */
    var name: CharSequence?
        get() = nameLabel.text
        set(value) {
            nameLabel.setText(value)
        }

    /**
     * The player score displayed, `null` for none.
     */
    var score: CharSequence?
        get() = scoreLabel.text
        set(value) {
            scoreLabel.setText(value)
        }

    val nameLabel = SdfLabel(skin, style.nameFontStyle, name)

    val scoreLabel = SdfLabel(skin, style.scoreFontStyle)

    init {
        val arrowImage = Image(style.arrowDrawable, Scaling.fit)

        pad(10f)
        add(arrowImage).size(30f, 30f).row()
        add(nameLabel).row()
        add(scoreLabel)
    }

    class PlayerLabelStyle {
        lateinit var arrowDrawable: Drawable
        lateinit var nameFontStyle: FontStyle
        lateinit var scoreFontStyle: FontStyle
    }

}