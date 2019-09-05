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

package com.maltaisn.cardgame.pcard

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.maltaisn.cardgame.widget.ShadowImage
import com.maltaisn.msdfgdx.FontStyle
import com.maltaisn.msdfgdx.widget.MsdfLabel
import ktx.math.vec2
import ktx.style.get


/**
 * A label for displaying a [PCard] value without showing the whole card.
 */
class PCardLabel(skin: Skin, card: PCard, fontStyle: FontStyle) : Table(skin) {

    private val pcardStyle: PCardStyle = skin.get()

    /**
     * The label font style. All styling attributes are
     * applied to both the rank label and the suit icon.
     */
    var fontStyle = fontStyle
        set(value) {
            field = value
            val size = value.size

            rankLabel.fontStyle = value

            suitIcon.color.set(value.color)
            suitIcon.shadowColor.set(value.shadowColor)
            suitIcon.shadowOffset = vec2(size / 16f, size / 16f)
            getCell(suitIcon).size(size).padLeft(size / 4)
        }

    /**
     * The card shown by the label.
     * TODO Jokers cannot be shown since they have no suit icon.
     */
    var card = card
        set(value) {
            require(value.rank != PCard.JOKER) { "PCardLabel can't show a joker." }

            field = value
            rankLabel.txt = PCard.RANK_STR[card.rank - 1]
            suitIcon.drawable = pcardStyle.suitIcons[card.suit]
        }


    private val rankLabel = MsdfLabel(null, skin, fontStyle)
    private val suitIcon = ShadowImage()


    init {
        pad(10f)
        add(rankLabel)
        add(suitIcon)

        this.fontStyle = fontStyle
        this.card = card
    }

}
