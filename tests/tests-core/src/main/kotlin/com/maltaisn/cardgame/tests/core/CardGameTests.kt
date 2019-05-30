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

package com.maltaisn.cardgame.tests.core

import com.maltaisn.cardgame.tests.core.tests.*


object CardGameTests {

    private val TESTS = listOf(
            CardAnimationTest::class.java,
            CardDealTest::class.java,
            FontTest::class.java,
            CardNullDealTest::class.java,
            DefaultGameMenuTest::class.java,
            MenuButtonTest::class.java,
            PrefWidgetsTest::class.java,
            SolitaireTest::class.java,
            CardTrickTest::class.java,
            PopupTest::class.java,
            ContainerTransitionTest::class.java,
            CardHandHighlightTest::class.java,
            CardHandClipTest::class.java,
            CardStackTest::class.java,
            MarkdownViewTest::class.java,
            PrefsViewTest::class.java,
            MenuDrawerTest::class.java,
            InGameMenuTest::class.java,
            SubMenuTest::class.java,
            TimeActionTest::class.java
    )

    val TESTS_MAP = TESTS.associateBy { it.simpleName }.toSortedMap()

    fun newTest(name: String) = TESTS_MAP[name]?.constructors?.first()?.newInstance() as CardGameTest

}