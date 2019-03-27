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

package io.github.maltaisn.cardgame.widget

import com.badlogic.gdx.scenes.scene2d.Action
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane


/**
 * Custom class that provides extended functionality to the built-in [ScrollPane].
 */
class ScrollView(actor: Actor? = null, style: ScrollPane.ScrollPaneStyle? = null) :
        ScrollPane(actor, style ?: ScrollPaneStyle()) {

    /**
     * The listener called when the scroll view is scrolled in any direction, or `null` for none.
     * There is no scroll listener so this adds an action checking every frame whether the scroll pane has scrolled.
     */
    var scrollListener: ((scrollView: ScrollView, x: Float, y: Float,
                          dx: Float, dy: Float) -> Unit)? = null
        set(listener) {
            if (field != null) {
                // Remove old listener action
                removeAction(scrollListenerAction)
                scrollListenerAction = null
            }
            field = listener
            if (listener != null) {
                // Add new listener action
                scrollListenerAction = object : Action() {
                    private var lastScrollX = 0f
                    private var lastScrollY = 0f

                    override fun act(delta: Float): Boolean {
                        val view = this@ScrollView
                        val scrollX = view.scrollX
                        val scrollY = view.scrollY
                        if (scrollX != lastScrollX || scrollY != lastScrollY) {
                            // Scroll changed
                            listener(view, scrollX, scrollY, scrollX - lastScrollX, scrollY - lastScrollY)
                            lastScrollX = scrollX
                            lastScrollY = scrollY
                        }
                        return false
                    }
                }
                addAction(scrollListenerAction)
            } else {

            }
        }

    private var scrollListenerAction: Action? = null

}