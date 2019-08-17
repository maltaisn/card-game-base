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
import com.maltaisn.cardgame.stats.Statistics
import com.maltaisn.cardgame.widget.Separator


/**
 * The group containing the statistics views and managing them.
 */
class StatsGroup(skin: Skin, val stats: Statistics) : Table() {

    /** The shown variant. Can be changed and the views will be refresh automatically. */
    var shownVariant = 0
        set(value) {
            field = value
            for (child in children) {
                if (child is StatView<*>) {
                    child.shownVariant = value
                }
            }
        }


    init {
        // Do the layout
        pad(20f, 0f, 40f, 0f)
        align(Align.top)

        val visibleStats = stats.stats.values.filter { !it.internal }
        for ((i, stat) in visibleStats.withIndex()) {
            // Add the statistic view
            add(stat.createView(skin)).growX().row()

            // Add a separator between stat views
            if (i < visibleStats.size - 1) {
                add(Separator(skin)).growX().pad(20f, 30f, 20f, 0f).row()
            }
        }
    }


    /**
     * Refresh the values displayed by all the stat views.
     */
    fun refresh() {
        for (child in children) {
            if (child is StatView<*>) {
                child.refresh()
            }
        }
    }

}
