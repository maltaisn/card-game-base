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

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup
import com.badlogic.gdx.scenes.scene2d.utils.Layout
import com.maltaisn.cardgame.CoreRes
import com.maltaisn.cardgame.core.CardGame
import com.maltaisn.cardgame.core.CardGameEvent
import com.maltaisn.cardgame.prefs.GamePrefs
import com.maltaisn.cardgame.prefs.PrefEntry
import com.maltaisn.cardgame.widget.card.CardAnimationLayer
import com.maltaisn.cardgame.widget.menu.GameMenu


/**
 * Custom container that places its children on top of each other, matching its size.
 * Game layer uses custom bounds to account for its side tables.
 *
 * The game layout manages the game actors (card containers, popups, markers, etc)
 * and updates them after a game event.
 */
abstract class CardGameLayout(assetManager: AssetManager,
                              val settings: GamePrefs) :
        WidgetGroup(), PrefEntry.PrefListener {

    protected var game: CardGame<*>? = null

    protected val coreSkin: Skin = assetManager[CoreRes.CORE_SKIN]

    /** Layer where the game takes place, contains the card containers. */
    val gameLayer: GameLayer

    /** Layer where the card are placed when animated. */
    val cardAnimationLayer: CardAnimationLayer

    /** Group where popups are shown. */
    val popupGroup: PopupGroup

    /** The game menu. */
    var gameMenu: GameMenu? = null
        set(value) {
            check(field == null) { "Game menu can only be set once." }
            if (value != null) {
                field = value
                addActor(value)
            }
        }


    init {
        setFillParent(true)

        // Create the layout
        gameLayer = GameLayer(coreSkin)
        cardAnimationLayer = CardAnimationLayer()
        popupGroup = PopupGroup()

        addActor(gameLayer)
        addActor(cardAnimationLayer)
        addActor(popupGroup)
    }

    override fun setStage(stage: Stage?) {
        super.setStage(stage)
        if (stage == null) {
            settings.removeListener(this)

            // Dispose game
            game?.dispose()
            game = null
        } else {
            settings.addListener(this)
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

    /**
     * Initialize layout for a [game]. The game could be at any state.
     * When this is called, the layout is always hidden.
     */
    open fun initGame(game: CardGame<*>) {
        this.game?.dispose()
        this.game = game
        game.eventListener = { doEvent(it) }

        gameMenu?.showInGameMenu()
    }

    /**
     * Called when a game [event] happens.
     * Layout should be updated accordingly.
     */
    abstract fun doEvent(event: CardGameEvent)

}
