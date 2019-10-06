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
 * A statistic for a single percentage value.
 * Percentage calculation is `(frac / total) * 100%`.
 *
 * @property fracStatKey The statistic key for the fractional part use to calculate the percentage.
 * @property totalStatKey The statistic key for the total part use to calculate the percentage.
 * @property showFrac Whether to show both the percentage and the fractional part in the view.
 * @property percentPrecision The maximum number of fraction digits of the percentage shown.
 */
class PercentStat(
        key: String,
        title: String,
        precision: Int,
        internal: Boolean,
        val fracStatKey: String,
        val totalStatKey: String,
        val showFrac: Boolean,
        val percentPrecision: Int)
    : CompositeStat<Float>(key, title, precision, internal) {

    lateinit var fracStat: NumberStat
        private set
    lateinit var totalStat: NumberStat
        private set

    /**
     * A number format to be used to format this statistic percent value.
     */
    val percentFmt = NumberFormat.getPercentInstance().apply {
        maximumFractionDigits = percentPrecision
    }


    /**
     * The percentage calculated with the two other stats.
     * The value may be invalid if the values make an invalid calculation eg: division by 0.
     */
    override fun get(variant: Int) = fracStat[variant] / totalStat[variant]


    override fun setOtherStats(stats: Statistics) {
        fracStat = getOtherStat(stats, fracStatKey)
        totalStat = getOtherStat(stats, totalStatKey)
    }

    override fun reset() {
        fracStat.reset()
        totalStat.reset()
    }

    override fun createView(skin: Skin) = PercentStatView(skin, this)


    class Builder(key: String) : Statistic.Builder(key) {
        var fracStatKey = ""
        var totalStatKey = ""
        var showFrac = false
        var percentPrecision = 0

        fun build() = PercentStat(key, title, precision, internal,
                fracStatKey, totalStatKey, showFrac, percentPrecision)
    }


    override fun toString() = "PercentStat[" +
            "fracStatKey: $fracStatKey, " +
            "totalStatKey: $totalStatKey, " +
            "showFrac: $showFrac, " +
            "percentPrecision: $percentPrecision, " +
            super.toString().substringAfter("[")

}
