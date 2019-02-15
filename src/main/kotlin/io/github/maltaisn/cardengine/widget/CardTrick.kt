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

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import io.github.maltaisn.cardengine.CardSpriteLoader
import io.github.maltaisn.cardengine.core.Card
import kotlin.math.*


/**
 * A card container that displays cards along a circle path.
 */
class CardTrick(cardLoader: CardSpriteLoader, capacity: Int) : CardContainer(cardLoader) {

    /**
     * The number of cards this trick can have.
     * Changing the capacity empties the trick.
     */
    var capacity = 0
        set(value) {
            field = value
            setCards(MutableList(value) { null })
        }

    /**
     * The angle of the first card on the circle in radians, counterclockwise positive.
     * `0f` will place the first card at a 3 o'clock position.
     */
    var startAngle = 0f

    /** The radius of the circle in pixels, or [RADIUS_AUTO] to set it automatically. */
    var radius = RADIUS_AUTO

    /** Whether cards are displayed in clockwise order or counterclocwise order. */
    var clockwisePlacement = true


    private var computedRadius = 0f
    private var center = Vector2()


    init {
        this.capacity = capacity
    }

    override fun setCards(newCards: Collection<Card?>) {
        require(newCards.size == capacity) {
            "Must set the same number of cards on trick as its capacity"
        }
        super.setCards(newCards)
    }

    override fun findInsertPositionForCoordinates(x: Float, y: Float): Int {
        // Find the angle of the coordinates relative to the center
        var angle = atan2(y - center.y, x - center.x) - startAngle
        if (clockwisePlacement) angle = -angle

        // Find the index of the card at the angle
        val index = (angle / PI2 * capacity).roundToInt()
        return (index % capacity + capacity) % capacity
    }

    override fun computeActorsPosition(): Array<Vector2> {
        // Recompute minimum size
        computeSize()

        // Find the card positions
        // They are placed in a circle at equal angles.
        val positions = Array(actors.size) { Vector2() }
        for (i in actors.indices) {
            val pos = positions[i]
            var angle = i.toFloat() / size * PI2 - startAngle
            if (clockwisePlacement) angle = -angle
            pos.x = center.x + cos(angle) * computedRadius - cardWidth / 2
            pos.y = center.y + sin(angle) * computedRadius - cardHeight / 2
        }

        return positions
    }

    override fun computeSize() {
        if (!sizeInvalid) return
        super.computeSize()

        if (capacity == 1) {
            computedRadius = 0f
            computedWidth = cardWidth
            computedHeight = cardHeight
            return
        }

        // Compute radius
        computedRadius = if (radius == RADIUS_AUTO) {
            (capacity * cardWidth * 0.75f) / PI2
        } else {
            radius
        }

        // Compute size
        val d = 2 * computedRadius
        computedWidth = d + cardWidth
        computedHeight = d + cardHeight

        // Compute center
        val offset = computeAlignmentOffset(computedWidth, computedHeight)
        center.x = offset.x + computedRadius + cardWidth / 2
        center.y = offset.y + computedRadius + cardHeight / 2
    }

    override fun drawDebugChildren(shapes: ShapeRenderer) {
        super.drawDebugChildren(shapes)

        if (!debug || capacity == 1) return

        // Draw the circle path on which the cards are placed
        shapes.set(ShapeRenderer.ShapeType.Line)
        shapes.color = Color.BLUE
        shapes.circle(center.x, center.y, computedRadius)

        // Draw a mark on the circle for each card position
        val r1 = computedRadius - 5f
        val r2 = computedRadius + 5f
        for (i in 0 until capacity) {
            var angle = i.toFloat() / capacity * PI2 - startAngle
            if (clockwisePlacement) angle = -angle
            val x = cos(angle)
            val y = sin(angle)
            shapes.line(center.x + x * r1, center.y + y * r1,
                    center.x + x * r2, center.y + y * r2)
        }
    }

    companion object {
        const val RADIUS_AUTO = -1f

        private const val PI2 = (PI * 2).toFloat()
    }

}