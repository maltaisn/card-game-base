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

package com.maltaisn.cardgame.widget

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup
import com.badlogic.gdx.scenes.scene2d.utils.Layout
import com.maltaisn.cardgame.prefs.PrefEntry
import com.maltaisn.cardgame.widget.card.CardAnimationLayer
import ktx.actors.onKeyboardFocusEvent
import ktx.actors.setKeyboardFocus


/**
 * Custom container that places its children on top of each other, matching its size.
 * Game layer uses custom bounds to account for its side tables.
 *
 * The game layout manages the game actors (card containers, popups, markers, etc)
 * and updates them after a game event.
 */
open class CardGameLayout(val skin: Skin) : WidgetGroup(), PrefEntry.PrefListener {

    /** Layer where the game takes place, contains the card containers. */
    val gameLayer: GameLayer

    /** Layer where the card are placed when animated. */
    val cardAnimationLayer: CardAnimationLayer

    /** Group where popups are shown. */
    val popupGroup: PopupGroup


    init {
        setFillParent(true)

        // Create the layout
        gameLayer = GameLayer(skin)
        cardAnimationLayer = CardAnimationLayer()
        popupGroup = PopupGroup()

        addActor(gameLayer)
        addActor(cardAnimationLayer)
        addActor(popupGroup)

        onKeyboardFocusEvent { event, _ ->
            if (!event.isFocused && event.relatedActor == null) {
                // When the keyboard focus is set to null, set it to the layout
                event.cancel()
                setKeyboardFocus(true)
            }
        }
    }

    override fun layout() {
        super.layout()

        for (child in children) {
            if (child is GameLayer) {
                val side = child.sideTableSize
                child.setBounds(-side, -side, width + 2 * side, height + 2 * side)
            } else {
                child.setBounds(0f, 0f, width, height)
            }
            if (child is Layout) {
                child.validate()
            }
        }
    }

}
