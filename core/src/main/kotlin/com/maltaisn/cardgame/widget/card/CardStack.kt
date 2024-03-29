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

package com.maltaisn.cardgame.widget.card

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Vector2
import ktx.collections.lastIndex
import ktx.math.vec2


/**
 * A card container that stacks a list of cards.
 */
class CardStack(cardStyle: CardActor.CardStyle) : CardContainer(cardStyle) {

    /**
     * Whether a slot should be drawn under all cards.
     * The slot will only be visible if there are no cards in the stack.
     */
    var drawSlot = false

    private var cardsPosition = vec2()


    override fun drawChildren(batch: Batch, parentAlpha: Float) {
        if (drawSlot && children.isEmpty) {
            // Draw the slot if there's no cards in the stack.
            val offset = computeAlignOffset(cardWidth, cardHeight)
            val slot = cardStyle.slot
            batch.setColor(1f, 1f, 1f, parentAlpha)
            slot.draw(batch, offset.x - slot.leftWidth * cardScale,
                    offset.y - slot.bottomHeight * cardScale,
                    cardWidth + slot.leftWidth + slot.rightWidth,
                    cardHeight + slot.bottomHeight + slot.topHeight)
        }

        super.drawChildren(batch, parentAlpha)
    }

    override fun onAnimationEnd() {
        super.onAnimationEnd()
        updateActorVisibility()
    }

    ////////// LAYOUT //////////
    override fun layout() {
        super.layout()
        updateActorVisibility()
    }

    override fun findCardPositionForCoordinates(x: Float, y: Float) = size - 1

    override fun findInsertPositionForCoordinates(x: Float, y: Float) = size

    override fun computeActorsPosition(): List<Vector2> {
        // Recompute minimum size
        computeSize()

        cardsPosition = computeAlignOffset(cardWidth, cardHeight)
        return List(actors.size) { cardsPosition.cpy() }
    }

    override fun computeSize() {
        if (!sizeInvalid) return
        super.computeSize()

        computedWidth = cardWidth
        computedHeight = cardHeight
    }

    private fun updateActorVisibility() {
        // Only show the top card, hide others. This prevents useless overdrawing.
        if (children.size > 0) {
            for (i in 0 until children.lastIndex) {
                children[i].isVisible = false
            }
            children.last().isVisible = true
        }
    }

}
