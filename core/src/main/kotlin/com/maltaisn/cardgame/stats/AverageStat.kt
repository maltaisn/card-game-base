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
import com.maltaisn.cardgame.widget.stats.NumberStatView


/**
 * A statistic for a single number.
 * Average calculation is `total / count`.
 *
 * @property totalStatKey The statistic key for the fractional part use to calculate the percentage.
 * @property countStatKey The statistic key for the total part use to calculate the percentage.
 */
class AverageStat(
        key: String,
        title: String,
        precision: Int,
        internal: Boolean,
        val totalStatKey: String,
        val countStatKey: String)
    : CompositeStat<Float>(key, title, precision, internal) {

    lateinit var totalStat: NumberStat
        private set
    lateinit var countStat: NumberStat
        private set


    /**
     * The average calculated with the two other stats.
     * The value may be invalid if the values make an invalid calculation eg: division by 0.
     */
    override fun get(variant: Int) = totalStat[variant] / countStat[variant]

    override fun setOtherStats(stats: Statistics) {
        totalStat = getOtherStat(stats, totalStatKey)
        countStat = getOtherStat(stats, countStatKey)
    }

    override fun reset() {
        totalStat.reset()
        countStat.reset()
    }

    override fun createView(skin: Skin) = NumberStatView(skin, this)


    class Builder(key: String) : Statistic.Builder(key) {
        var totalStatKey = ""
        var countStatKey = ""

        fun build() = AverageStat(key, title, precision, internal, totalStatKey, countStatKey)
    }


    override fun toString() = "AverageStat[" +
            "totalStatKey: $totalStatKey, " +
            "countStatKey: $totalStatKey, " +
            super.toString().substringAfter("[")

}
