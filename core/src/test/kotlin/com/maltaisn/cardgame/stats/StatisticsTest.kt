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

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyFloat
import org.mockito.junit.MockitoJUnitRunner
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue


@RunWith(MockitoJUnitRunner::class)
internal class StatisticsTest {

    @Before
    fun mockGdx() {
        Gdx.app = mock()
        val prefs: Preferences = mock()
        whenever(Gdx.app.getPreferences(any())).thenReturn(prefs)
        whenever(prefs.getFloat(any(), anyFloat())).thenAnswer { it.arguments[1] }
    }

    @Test
    fun shouldBuildStats_empty() {
        val stats = Statistics("stats")
        assertEquals("stats", stats.name)
        assertEquals(0, stats.stats.size)
    }

    @Test
    fun shouldBuildStats_all_types() {
        val stats = Statistics("stats") {
            number("number1") {
                title = "Hey"
                defaultValue = 100f
            }
            number("number2") {
                title = "Hey again"
            }
            average("average") {
                totalStatKey = "number1"
                countStatKey = "number2"
            }
            percent("percent") {
                fracStatKey = "number1"
                totalStatKey = "number2"
            }
        }
        assertTrue { stats["number1"] is NumberStat }
        assertTrue { stats["number2"] is NumberStat }
        assertTrue { stats["average"] is AverageStat }
        assertTrue { stats["percent"] is PercentStat }
        assertEquals("Hey", stats["number1"]!!.title)
    }

    @Test
    fun shouldGetStat() {
        val stat = NumberStat("stat", "stat", 0, false, 0f)
        val stats = Statistics("stats", listOf("default"), mapOf("stat" to stat))
        assertSame(stat, stats["stat"])
    }

    @Test
    fun shouldGetStat_null() {
        val stats = Statistics("stats", listOf("default"), mapOf())
        assertNull(stats["stat"])
    }

    @Test
    fun shouldSetCompositeReferencedStats() {
        val stat1 = NumberStat("stat1", "stat1", 0, true, 0f)
        val stat2 = NumberStat("stat2", "stat2", 0, true, 0f)
        val average = AverageStat("stat2", "stat2", 0, false,
                "stat1", "stat2")
        val percent = PercentStat("stat2", "stat2", 0, false,
                "stat1", "stat2", false, 0)

        Statistics("stats") {
            stats["stat1"] = stat1
            stats["stat2"] = stat2
            stats["average"] = average
            stats["percent"] = percent
        }

        assertSame(stat1, average.totalStat)
        assertSame(stat2, average.countStat)
        assertSame(stat1, percent.fracStat)
        assertSame(stat2, percent.totalStat)
    }

    @Test
    fun shouldGetNumber() {
        val stats = Statistics("stats") {
            number("number") {
                defaultValue = 100f
            }
        }
        val numberStat = stats["number"] as NumberStat

        assertEquals(100f, numberStat[0])
        assertEquals(100f, stats.getNumber("number")[0])
    }

}
