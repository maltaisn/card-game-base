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

package com.maltaisn.cardgame.prefs

import com.badlogic.gdx.Gdx
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import kotlin.test.*


@RunWith(MockitoJUnitRunner::class)
internal class GamePrefsTest {

    @Before
    fun mockGdx() {
        Gdx.app = mock()
        whenever(Gdx.app.getPreferences(any())).thenReturn(mock())
    }

    @Test
    fun shouldBuildGamePrefs_empty() {
        val prefs = GamePrefs("test")
        assertEquals("test", prefs.name)
        assertEquals(0, prefs.prefs.size)
    }

    @Test
    fun shouldBuildGamePrefs_withCategory() {
        val prefs = GamePrefs("test") {
            category("category") {
                title = "title"
            }
        }

        assertEquals(1, prefs.prefs.size)
        val catg = assertNotNull(prefs.prefs["category"])
        assertTrue { catg is PrefCategory }
        assertEquals("category", catg.key)
        assertEquals("title", catg.title)
    }

    @Test
    fun shouldGetPref_null() {
        val prefs = GamePrefs("test", mapOf())
        assertNull(prefs["pref"])
    }

    @Test
    fun shouldGetPref_level1() {
        val pref = SwitchPref.Builder("pref").build()
        val prefs = GamePrefs("test", mapOf("pref" to pref))
        assertEquals(pref, prefs["pref"])
    }

    @Test
    fun shouldGetPref_level2() {
        val pref = SwitchPref.Builder("pref").build()
        val category = PrefCategory.Builder("catg").apply {
            prefs["pref"] = pref
        }.build()
        val prefs = GamePrefs("test", mapOf("catg" to category))
        assertEquals(pref, prefs["pref"])
    }

    @Test
    fun dependantShouldBeDisabled() {
        val prefs = GamePrefs("test") {
            switch("enabled") {
                disableDependentsState = false
            }
            switch("dependant") {
                dependency = "enabled"
            }
        }

        val switch = prefs["enabled"] as SwitchPref
        val dependant = prefs["dependant"] as SwitchPref

        switch.value = true
        assertTrue(dependant.enabled)

        switch.value = false
        assertFalse(dependant.enabled)
    }

    @Test
    fun dependantShouldBeDisabled_inverse() {
        val prefs = GamePrefs("test") {
            switch("enabled") {
                disableDependentsState = true
            }
            switch("dependant") {
                dependency = "enabled"
            }
        }

        val switch = prefs["enabled"] as SwitchPref
        val dependant = prefs["dependant"] as SwitchPref

        switch.value = true
        assertFalse(dependant.enabled)

        switch.value = false
        assertTrue(dependant.enabled)
    }

}
