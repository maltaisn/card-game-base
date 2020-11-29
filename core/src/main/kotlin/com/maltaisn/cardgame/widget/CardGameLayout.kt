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

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Stack
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.maltaisn.cardgame.widget.card.CardAnimationGroup
import ktx.actors.onKeyboardFocusEvent
import ktx.actors.setKeyboardFocus
import ktx.style.get


/**
 * The game layout that contains all game components (card containers, popups, menus, etc).
 */
open class CardGameLayout(val skin: Skin) : Stack() {

    private val style: CardGameLayoutStyle = skin.get()

    /**
     * The size of the tables hidden on each side in pixels.
     * These can be used to put other player hands or other hidden card containers.
     */
    var sideTableSize = 500f
        set(value) {
            field = value
            topTable.height = value
            bottomTable.height = value
            leftTable.width = value
            rightTable.width = value
            invalidateHierarchy()
        }

    // All the tables that contain the card containers.
    private val gameTable = Table()
    val centerTable = Table()
    val topTable = Table()
    val bottomTable = Table()
    val leftTable = Table()
    val rightTable = Table()

    /** Group in which the card actors are added when animated. */
    val cardAnimationGroup: CardAnimationGroup

    /** Group in which the popups are added. */
    val popupGroup: AbsoluteLayout


    init {
        setFillParent(true)

        // Create the layout
        gameTable.apply {
            add(topTable).height(sideTableSize).colspan(3).growX().row()
            add(leftTable).width(sideTableSize).growY()
            add(centerTable).grow()
            add(rightTable).width(sideTableSize).growY().row()
            add(bottomTable).height(sideTableSize).colspan(3).growX()
        }
        topTable.isVisible = false
        bottomTable.isVisible = false
        leftTable.isVisible = false
        rightTable.isVisible = false

        cardAnimationGroup = CardAnimationGroup()
        popupGroup = AbsoluteLayout()

        addActor(gameTable)
        addActor(cardAnimationGroup)
        addActor(popupGroup)

        onKeyboardFocusEvent { event ->
            if (!event.isFocused && event.relatedActor == null) {
                // When the keyboard focus is set to null, set it to the layout
                // This is useful for an eventual back key handling.
                event.cancel()
                setKeyboardFocus(true)
            }
        }
    }

    override fun layout() {
        super.layout()

        // Game table needs special bounds since it
        // has side tables on each side that must not be visible.
        val side = sideTableSize
        gameTable.setBounds(-side, -side, width + 2 * side, height + 2 * side)
    }

    override fun drawChildren(batch: Batch, parentAlpha: Float) {
        // Draw the background
        batch.setColor(1f, 1f, 1f, parentAlpha)
        style.background.draw(batch, 0f, 0f, width, height)

        // Draw the background border
        batch.setColor(1f, 1f, 1f, parentAlpha * 0.2f)
        style.border.draw(batch, 25f, 25f, width - 50f, height - 50f)

        super.drawChildren(batch, parentAlpha)
    }

    class CardGameLayoutStyle(
            val background: Drawable,
            val border: Drawable)

}
