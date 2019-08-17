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

package com.maltaisn.cardgame.stats

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.maltaisn.cardgame.widget.stats.PercentStatView
import java.text.NumberFormat


/**
 * A statistic for a single number.
 */
class PercentStat : CompositeStat<Float>() {

    lateinit var fracStatKey: String
    lateinit var fracStat: NumberStat

    lateinit var totalStatKey: String
    lateinit var totalStat: NumberStat

    /**
     * Whether to show both the percentage and the fractional part in the view.
     */
    var showFrac = false

    /** A number format to be used to format this statistic percent value. */
    val percentFmt: NumberFormat
        get() = NumberFormat.getPercentInstance().apply {
            maximumFractionDigits = precision
        }

    /**
     * The percentage calculated with the two other stats.
     * The value may be invalid if the values make an invalid calculation eg: division by 0.
     */
    override fun get(variant: Int) = fracStat[variant] / totalStat[variant]


    override fun setOtherStats(stats: Statistics) {
        fracStat = stats[fracStatKey] as NumberStat
        totalStat = stats[totalStatKey] as NumberStat
    }

    override fun reset() {
        fracStat.reset()
        totalStat.reset()
    }

    override fun createView(skin: Skin) = PercentStatView(skin, this)

}
