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

package io.github.maltaisn.cardengine.widget

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Vector2
import io.github.maltaisn.cardengine.CardSpriteLoader
import io.github.maltaisn.cardengine.drawSprite
import ktx.collections.lastIndex


/**
 * A card container that stacks a list of cards.
 */
class CardStack(cardLoader: CardSpriteLoader) : CardContainer(cardLoader) {

    /**
     * Whether a slot should be drawn under all cards.
     * The slot will only be visible if there are no cards in the stack.
     */
    var drawSlot = false

    private var cardsPosition = Vector2()

    override fun layout() {
        super.layout()
        updateActorVisibility()
    }

    override fun drawChildren(batch: Batch, parentAlpha: Float) {
        if (drawSlot && children.isEmpty) {
            drawSprite(batch, cardLoader.getSprite(CardSpriteLoader.SLOT),
                    cardScale, 0f, parentAlpha)
        }

        super.drawChildren(batch, parentAlpha)
    }

    override fun onAnimationEnd() {
        super.onAnimationEnd()
        updateActorVisibility()
    }

    override fun findCardPositionForCoordinates(x: Float, y: Float) = size - 1

    override fun findInsertPositionForCoordinates(x: Float, y: Float) = size

    override fun computeActorsPosition(): List<Vector2> {
        // Recompute minimum size
        computeSize()

        cardsPosition = computeAlignmentOffset(cardWidth, cardHeight)
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