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

package com.maltaisn.cardgame.widget.stats

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.maltaisn.cardgame.stats.Statistic
import com.maltaisn.cardgame.utils.padV
import com.maltaisn.msdfgdx.FontStyle
import com.maltaisn.msdfgdx.widget.MsdfLabel
import ktx.style.get


/**
 * The view for a statistic.
 */
abstract class StatView<S : Statistic<*>>(skin: Skin, val stat: S) : Table(skin) {

    /** The shown variant value for this statistic. */
    var shownVariant = 0
        set(value) {
            field = value
            refresh()
        }

    protected val titleLabel: MsdfLabel
    protected val valueLabel: MsdfLabel


    init {
        val style: StatViewStyle = skin.get()
        titleLabel = MsdfLabel(stat.title, skin, style.titleFontStyle).apply {
            setWrap(true)
            setAlignment(Align.left)
        }
        valueLabel = MsdfLabel(null, skin, style.valueFontStyle)

        padV(20f)
        add(titleLabel).growX().pad(10f, 20f, 10f, 20f)
        add(valueLabel).pad(10f, 10f, 10f, 40f).row()

        refresh()
    }


    /**
     * Update the view to match the statistic value.
     */
    abstract fun refresh()


    class StatViewStyle(
            val titleFontStyle: FontStyle,
            val valueFontStyle: FontStyle)


    companion object {
        const val INVALID_PLACEHOLDER = "--"
    }

}
