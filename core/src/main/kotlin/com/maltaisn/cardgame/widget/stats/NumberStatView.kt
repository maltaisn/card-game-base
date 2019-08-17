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
import com.maltaisn.cardgame.stats.Statistic


class NumberStatView(skin: Skin, stat: Statistic<Float>) : StatView<Statistic<Float>>(skin, stat) {

    override fun refresh() {
        val value = stat[shownVariant]
        valueLabel.setText(if (value.isFinite()) {
            stat.numberFmt.format(value)
        } else {
            INVALID_PLACEHOLDER
        })
    }

}
