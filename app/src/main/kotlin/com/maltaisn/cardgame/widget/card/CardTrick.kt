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

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.maltaisn.cardgame.game.Card
import com.maltaisn.cardgame.widget.GameLayer
import ktx.math.vec2
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin


/**
 * A card container that displays cards along a circle path.
 */
class CardTrick : CardContainer {

    /**
     * The number of cards this trick can have.
     * Changing the capacity resets the radius and card angles.
     * Existing cards are truncated or extended to new capacity.
     */
    var capacity = 0
        set(value) {
            field = value
            cards = MutableList(value) { actors.getOrNull(it)?.card }
            cardAngles = List(value) { it * TAU / value }
            setAutoRadius()
        }

    /**
     * The angle of the first card on the circle in radians, counterclockwise positive.
     * `0f` will place the first card at a 3 o'clock position. This can be changed
     * to tune Z-index (eg: when a card is played first it goes on bottom).
     */
    var startAngle = 0f

    /**
     * The angles at which the cards are placed. The list must be the same size as the
     * capacity. Use `null` for auto-placement around a circle.
     * Angles are relative to [startAngle] and are affected by [clockwisePlacement].
     * Must be set after [capacity].
     */
    var cardAngles: List<Float> = emptyList()
        set(value) {
            require(value.size == capacity) {
                "Card angles array must be the same size as capacity."
            }

            // Sort and clamp angles
            val angles = value.toMutableList()
            angles.sort()
            for (i in angles.indices) {
                angles[i] = (angles[i] + TAU) % TAU
            }
            field = angles
            intersectionAngles = null
        }

    private var intersectionAngles: List<Float>? = null

    /**
     * The radius of the ellipse in pixels.
     * Must be set after [capacity].
     */
    val radius = vec2()

    /** Whether cards are displayed in clockwise order or counterclockwise order. */
    var clockwisePlacement = true


    private val center = vec2()


    constructor(coreSkin: Skin, cardSkin: Skin, capacity: Int) : super(coreSkin, cardSkin) {
        this.capacity = capacity
    }

    constructor(coreStyle: GameLayer.CoreStyle, cardStyle: CardActor.CardStyle,
                capacity: Int) : super(coreStyle, cardStyle) {
        this.capacity = capacity
    }

    override fun drawDebugChildren(shapes: ShapeRenderer) {
        super.drawDebugChildren(shapes)

        if (!debug || capacity == 1) return

        // Draw the circle path on which the cards are placed
        shapes.set(ShapeRenderer.ShapeType.Line)
        shapes.color = Color.BLUE
        shapes.ellipse(center.x - radius.x, center.y - radius.y,
                radius.x * 2, radius.y * 2)

        // Draw a mark on the circle for each card position
        val r1x = radius.x - 5f
        val r1y = radius.y - 5f
        val r2x = radius.x + 5f
        val r2y = radius.y + 5f
        for (i in 0 until capacity) {
            var angle = cardAngles[i] - startAngle
            if (clockwisePlacement) angle = -angle
            val x = cos(angle)
            val y = sin(angle)
            shapes.line(center.x + x * r1x, center.y + y * r1y,
                    center.x + x * r2x, center.y + y * r2y)
        }
    }

    ////////// CARDS //////////
    override fun updateCards(newCards: List<Card?>) {
        require(newCards.size == capacity) {
            "Must set the same number of cards on trick as its capacity"
        }
        super.updateCards(newCards)
    }

    ////////// LAYOUT //////////
    override fun computeActorsPosition(): List<Vector2> {
        // Recompute minimum size
        computeSize()

        // Find the card positions
        // They are placed in a circle at equal angles.
        val positions = List(actors.size) { vec2() }
        for (i in actors.indices) {
            val pos = positions[i]
            var angle = cardAngles[i] - startAngle
            if (clockwisePlacement) angle = -angle
            pos.x = center.x + cos(angle) * radius.x - cardWidth / 2
            pos.y = center.y + sin(angle) * radius.y - cardHeight / 2
        }

        return positions
    }

    override fun computeSize() {
        if (!sizeInvalid) return
        super.computeSize()

        // Compute size
        computedWidth = 2 * radius.x + cardWidth
        computedHeight = 2 * radius.y + cardHeight

        // Compute center
        val offset = computeAlignOffset(computedWidth, computedHeight)
        center.x = offset.x + radius.x + cardWidth / 2
        center.y = offset.y + radius.y + cardHeight / 2
    }

    override fun findCardPositionForCoordinates(x: Float, y: Float): Int {
        if (capacity == 1) return 0

        var angles = intersectionAngles
        if (angles == null) {
            // Find intersection angles, the angles between each card angle.
            angles = cardAngles.toMutableList()
            angles.add(0, angles.last() - TAU)
            angles.add(angles[1] + TAU)
            for (i in 0..capacity) {
                angles[i] = (angles[i] + angles[i + 1]) / 2
            }
            angles[angles.size - 1] = angles[1] + TAU
            intersectionAngles = angles
        }

        // Find the angle of the coordinates relative to the center between 0 and tau.
        var angle = atan2(y - center.y, x - center.x) - startAngle
        if (clockwisePlacement) angle = -angle
        angle = (angle + TAU) % TAU

        // Find the index of the card at the angle
        for (i in 0..capacity) {
            if (angle >= angles[i] && angle < angles[i + 1]) {
                return i % capacity
            }
        }

        error("Could not find card position")  // Should never happen
    }

    /** Returns the same as [findCardPositionForCoordinates] since cards can only be replaced in a trick. */
    override fun findInsertPositionForCoordinates(x: Float, y: Float) =
            findCardPositionForCoordinates(x, y)

    /** Set radius auto adjusted for capacity. */
    fun setAutoRadius() {
        computeSize()

        val r = if (capacity == 1) 0f else (capacity * cardWidth * 0.75f) / TAU
        radius.set(r, r)

        sizeInvalid = true
    }

    companion object {
        private const val TAU = (PI * 2).toFloat()
    }

}