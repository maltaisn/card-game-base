/*
 * Copyright 2019 Nicolas Maltais
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.maltaisn.cardgame.tests.android

import android.os.Bundle
import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.maltaisn.cardgame.CardGameApp
import com.maltaisn.cardgame.tests.core.CardGameTests


class GameTestActivity : AndroidApplication() {

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)

        val config = AndroidApplicationConfiguration()
        config.useAccelerometer = false
        config.useCompass = false

        val testName = checkNotNull(intent?.extras?.getString(EXTRA_TEST_NAME)) {
            "Test cannot be null."
        }

        initialize(object : CardGameApp() {
            override fun create() {
                setScreen(CardGameTests.newTest(testName))
            }
        }, config)
    }

    companion object {
        const val EXTRA_TEST_NAME = "test_name"
    }

}