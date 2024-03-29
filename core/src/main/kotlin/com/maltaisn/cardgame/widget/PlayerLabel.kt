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

package com.maltaisn.cardgame.widget

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.utils.Scaling
import com.maltaisn.cardgame.widget.action.ActionDelegate
import com.maltaisn.cardgame.widget.action.TimeAction
import com.maltaisn.msdfgdx.FontStyle
import com.maltaisn.msdfgdx.widget.MsdfLabel
import ktx.actors.alpha
import ktx.style.get
import kotlin.math.abs


class PlayerLabel(skin: Skin, name: CharSequence? = null) : Table() {

    val style: PlayerLabelStyle = skin.get()

    /**
     * The player name displayed, `null` for none.
     */
    var name: CharSequence?
        get() = nameLabel.text
        set(value) {
            nameLabel.txt = value
        }

    /**
     * The player score displayed, `null` for none.
     */
    var score: CharSequence? = null
        set(value) {
            if (field == value) return
            scoreTransitionAction?.end()
            field = value
            scoreTransitionAction = ScoreTransitionAction()
        }

    private val nameLabel = MsdfLabel(name, skin, style.nameFontStyle)
    private val scoreLabel = MsdfLabel(null, skin, style.scoreFontStyle)

    private var scoreTransitionAction by ActionDelegate<TimeAction>()

    init {
        val arrowImage = Image(style.arrowDrawable, Scaling.fit)

        pad(20f)
        add(arrowImage).size(60f, 60f).row()
        add(nameLabel).row()
        add(scoreLabel)
    }

    override fun clearActions() {
        super.clearActions()
        scoreTransitionAction?.end()
    }

    private inner class ScoreTransitionAction :
            TimeAction(0.25f, Interpolation.linear) {

        override fun update(progress: Float) {
            // Fade to 50% alpha, change text, then fade back to full alpha.
            scoreLabel.alpha = abs(0.5f - progress) + 0.5f
            if (progress >= 0.5f) {
                scoreLabel.txt = score
            }
        }

        override fun end() {
            scoreLabel.txt = score
            scoreTransitionAction = null
        }
    }

    class PlayerLabelStyle(
            val arrowDrawable: Drawable,
            val nameFontStyle: FontStyle,
            val scoreFontStyle: FontStyle)

}
