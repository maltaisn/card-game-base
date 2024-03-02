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
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyFloat
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals


@RunWith(MockitoJUnitRunner::class)
internal class NumberStatTest {

    val prefs: Preferences = mock()

    @Before
    fun mockGdx() {
        whenever(prefs.getFloat(any(), anyFloat())).thenAnswer { it.arguments[1] }
    }

    @Test
    fun variant_value_get_set() {
        val stat = NumberStat("stat", "", 0, false, 0f)
        stat.initialize(3)

        stat[0] = 12
        assertEquals(12f, stat[0])
        stat[1]++
        assertEquals(1f, stat[1])
        stat[2] = stat[0] + stat[1]
        assertEquals(13f, stat[2])
    }

    @Test
    fun reset() {
        val stat = NumberStat("stat", "", 0, false, 10f)
        stat.initialize(3)

        stat[0] = 16f
        stat[2] = 8f
        stat.reset()

        for (i in 0 until 3) {
            assertEquals(10f, stat[i])
        }
    }

    @Test
    fun loadValue() {
        val stat = NumberStat("stat", "", 0, false, 10f)
        stat.initialize(3)

        stat.loadValue(prefs)
        for (i in 0 until 3) {
            assertEquals(10f, stat[i])
        }
    }

    @Test
    @Suppress("LibGDXMissingFlush")
    fun saveValue() {
        val stat = NumberStat("stat", "", 0, false, 0f)
        stat.initialize(3)

        for (i in 0 until 3) {
            stat[i] = 100f
        }
        stat.saveValue(prefs)

        for (i in 0 until 3) {
            verify(prefs).putFloat("stat_$i", 100f)
        }
    }

}
