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

import com.badlogic.gdx.Preferences


/**
 * A statistic that relies other statistics to calculate its value.
 */
abstract class CompositeStat<T> : Statistic<T>() {

    /**
     * Set the references to the other stats with a [stats] object.
     */
    internal abstract fun setOtherStats(stats: Statistics)

    override fun initialize(variants: Int) = Unit

    override fun loadValue(prefs: Preferences) = Unit

    override fun saveValue(prefs: Preferences) = Unit

}
