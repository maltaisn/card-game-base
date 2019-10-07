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

package com.maltaisn.cardgame.widget.menu

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.maltaisn.cardgame.game.Card
import com.maltaisn.cardgame.utils.padV
import com.maltaisn.cardgame.widget.AutoSizeLabel
import com.maltaisn.cardgame.widget.action.TimeAction
import com.maltaisn.cardgame.widget.card.CardActor
import com.maltaisn.cardgame.widget.card.CardTrick
import com.maltaisn.msdfgdx.FontStyle
import ktx.actors.alpha
import ktx.actors.onClick
import ktx.style.get
import kotlin.math.PI


/**
 * The main menu of the game, has a row of items on top and one on bottom.
 */
class MainMenu(skin: Skin, cardStyle: CardActor.CardStyle) : MenuTable(skin) {

    private val style: MainMenuStyle = skin.get()

    /**
     * The title of the game displayed in the center, or `null` for none.
     */
    var title: CharSequence?
        get() = titleLabel.txt
        set(value) {
            titleLabel.txt = value
        }

    /**
     * The cards shown in the main menu, can be empty for none.
     * There should be an odd number of cards. The cards set are placed in the same order,
     * with the middle card on top of the others.
     */
    var cards = emptyList<Card>()
        set(value) {
            require(value.size % 2 == 1) { "There must be an odd number of cards" }
            field = value

            // Put them in correct order of Z-index
            val halfSize = value.size / 2
            val cards = mutableListOf<Card?>()
            cards += value.subList(0, halfSize)
            cards += value.subList(halfSize + 1, value.size).reversed()
            cards += value[halfSize]
            cardTrick.cards = cards + arrayOfNulls(16 - value.size)

            // Find the angle of each card
            val angles = mutableListOf<Float>()
            for (i in 0 until halfSize) {
                val distance = CARDS_SPACING * (i + 1)
                angles.add(i, distance)
                angles.add(0, -distance)
            }
            angles += List(16 - angles.size) { 0f }
            cardTrick.cardAngles = angles
        }


    private val leftSide = Table()
    private val rightSide = Table()
    private val titleLabel = AutoSizeLabel(null, skin, style.titleFontStyle)
    private val cardTrick = CardTrick(cardStyle, 16)


    override var shown
        get() = super.shown
        set(value) {
            if (super.shown == value) return
            super.shown = value

            if (transitionAction == null) {
                // Fixes weird bug were main menu collapses horizontally...
                invalidate()

                transitionAction = TransitionAction()
            }
        }


    init {
        checkable = false

        titleLabel.apply {
            setWrap(true)
            setAlignment(Align.center)
            minTextSize = 128f
            maxTextSize = 384f
            granularity = 16f
        }

        cardTrick.apply {
            enabled = false
            cardSize = CardActor.SIZE_BIG
            radius.set(450f, 300f)
            startAngle = PI.toFloat() / 2
        }

        // Do the layout
        add(leftSide).width(MENU_COL_WIDTH).growY().padV(40f)
        add(titleLabel).growX().align(Align.bottom).height(420f)
                .pad(0f, 100f, 400f, 100f)
        add(rightSide).width(MENU_COL_WIDTH).growY().padV(40f)
        addActor(cardTrick)
    }

    override fun layout() {
        super.layout()

        cardTrick.setSize(cardTrick.prefWidth, cardTrick.prefHeight)
        cardTrick.setPosition((width - cardTrick.width) / 2,
                -cardTrick.height + CARD_TRICK_HEIGHT)

        (transitionAction as TransitionAction?)?.let { transition ->
            transition.leftStartX = leftSide.x
            transition.rightStartX = rightSide.x
            transition.trickStartY = cardTrick.y
        }
    }

    override fun doMenuLayout() {
        leftSide.clearChildren()
        rightSide.clearChildren()

        for (item in items) {
            val onLeftSide = (item.position == ITEM_POS_LEFT)
            val fontStyle = if (item.important) style.importantItemFontStyle else style.itemFontStyle
            val btn = MenuButton(skin, fontStyle, item.title, item.icon,
                    if (onLeftSide) MenuButton.AnchorSide.LEFT else MenuButton.AnchorSide.RIGHT).apply {
                onClick { onItemBtnClicked(item) }
                add(iconImage).size(style.itemIconSize).row()
                add(titleLabel).growX().padTop(30f)
                titleLabel.setWrap(true)
                titleLabel.setAlignment(Align.center)
                enabled = item.enabled
            }
            item.button = btn

            if (item.shown) {
                (if (onLeftSide) leftSide else rightSide).add(btn)
                        .grow().padV(20f).row()
            }
        }
    }


    private inner class TransitionAction :
            TimeAction(TRANSITION_DURATION, Interpolation.smooth, reversed = !shown) {

        var leftStartX = leftSide.x
        var rightStartX = rightSide.x
        var trickStartY = cardTrick.y

        init {
            isVisible = true
            alpha = if (shown) 0f else 1f
            renderToFrameBuffer = true
        }

        override fun update(progress: Float) {
            reversed = !shown

            val invProgress = 1 - progress
            leftSide.x = leftStartX - invProgress * MENU_COL_WIDTH
            rightSide.x = rightStartX + invProgress * MENU_COL_WIDTH
            cardTrick.y = trickStartY - invProgress * CARD_TRICK_HEIGHT * 0.5f
            alpha = progress
        }

        override fun end() {
            isVisible = shown
            renderToFrameBuffer = false
            transitionAction = null

            // Place all animated widgets to their correct position
            leftSide.x = leftStartX
            rightSide.x = rightStartX
            cardTrick.y = trickStartY
        }
    }


    class MainMenuStyle(
            itemFontStyle: FontStyle,
            importantItemFontStyle: FontStyle,
            itemIconSize: Float,
            val titleFontStyle: FontStyle)
        : MenuTableStyle(itemFontStyle, importantItemFontStyle, itemIconSize)


    companion object {
        const val ITEM_POS_LEFT = 0
        const val ITEM_POS_RIGHT = 1

        const val TRANSITION_DURATION = 0.3f

        private const val MENU_COL_WIDTH = 400f
        private const val CARD_TRICK_HEIGHT = 360f

        private const val CARDS_SPACING = PI.toFloat() / 8
    }

}
