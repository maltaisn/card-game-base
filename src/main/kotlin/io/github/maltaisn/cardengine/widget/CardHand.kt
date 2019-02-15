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
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Action
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack
import com.badlogic.gdx.utils.Align
import io.github.maltaisn.cardengine.Animation
import io.github.maltaisn.cardengine.CardSpriteLoader
import io.github.maltaisn.cardengine.applyBounded
import io.github.maltaisn.cardengine.core.Card
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min


/**
 * A card container that displays a list of card actors to form a hand, that can be sorted.
 * When using [setCards], sort is not immediately called.
 */
class CardHand(cardLoader: CardSpriteLoader) : CardContainer(cardLoader) {

    /**
     * Sorter to use for sorting the group before displaying. Use `null` for no sorting.
     * If changed and group has cards, [sort] must be called to sort the cards.
     */
    var sorter: Card.Sorter<out Card>? = null

    /**
     * Whether the cards highlight state can be changed by clicking on them.
     * When a card is highlighted, the [highlightListeners] are called.
     */
    var highlightable = false

    /** The group direction, either true for horizontal or false for vertical */
    var horizontal = true

    /**
     * The minimum spacing between the left border of two cards.
     * `0f` would be showing the cards on top of each other.
     * When there's no more space for cards, the minimum spacing will be
     * respected but cards will be drawn out of the group bounds.
     */
    var minCardSpacing = 30f

    /** The maximum spacing between the borders of two cards. */
    var maxCardSpacing = cardSize / 2

    /**
     * When a card is highlighted, the size of the translation to do.
     * When horizontal, cards are translated up and when vertical, right.
     * To invert this direction, use negative values.
     */
    var highlightPercent = 0.15f

    /**
     * The percentage of the card that is hidden.
     * When horizontal, clip starts from the bottom and when vertical, from the left.
     * To invert this direction, use negative values.
     */
    var clipPercent = 0f


    private var highlightSize = 0f  // Translation applied on highlighted cards.
    private var clipSize = 0f  // Size of the displayed portion of the card.


    /** Listener triggered if user clicks on a highlightable card actor. */
    val highlightListener: HighlightListener? = null


    init {
        addClickListener(object : ClickListener {
            override fun onCardClicked(actor: CardActor, index: Int) {
                if (highlightable) {
                    highlightActor(actor, !actor.highlighted)
                }
            }
        })
    }

    /**
     * Sort the cards in the group if there's a [sorter] set.
     * If the hand contains null cards, it cannot be sorted.
     */
    fun sort() {
        assertCanBeSorted()

        if (sorter != null && size > 1) {
            // Apply new sorting order.
            @Suppress("UNCHECKED_CAST")
            val sorter = sorter as Card.Sorter<Card>
            if (!sorter.transitive) {
                sorter.initialize(cards.requireNoNulls())
            }
            actors.sortWith(Comparator { o1, o2 -> sorter.compare(o1!!.card, o2!!.card) })
            cards.clear()
            for (actor in actors) {
                cards += actor!!.card
            }
        }
    }

    /**
     * Find the index for adding a [card] in sorted order to the group. This can only be used if the
     * group is already sorted or it will produce unpredictible results.
     * If [sorter] is not transitive, [sort] must be called after insertions to keep the order.
     * The hand must not contain any null card.
     */
    fun findSortedInsertionPos(card: Card): Int {
        if (sorter == null) return 0
        assertCanBeSorted()

        @Suppress("UNCHECKED_CAST")
        val sorter = sorter as Card.Sorter<Card>
        val i = cards.requireNoNulls().binarySearch(card, sorter)
        return if (i < 0) i.inv() else i
    }

    private fun assertCanBeSorted() {
        // Check if there are any null cards. If there are, the hand cannot be sorted.
        for (card in cards) {
            checkNotNull(card) { "Card hand cannot be sorted if it contains null cards." }
        }
    }

    override fun findInsertPositionForCoordinates(x: Float, y: Float): Int {
        val positions = computeActorsPosition()
        val py = if (horizontal) y else y - cardHeight
        for (i in 0..positions.lastIndex) {
            val pos = positions[i]
            if (horizontal && x < pos.x || !horizontal && py > pos.y) {
                return i
            }
        }
        return size
    }

    /**
     * Change the highlighted state of [cards].
     */
    fun highlightCards(highlighted: Boolean, vararg cards: Card) {
        for (card in cards) {
            val actor = actors.find { it?.card == card } ?: continue
            highlightActor(actor, highlighted)
        }
    }

    /**
     * Change the highlighted state of all cards.
     */
    fun highlightAllCards(highlighted: Boolean) {
        for (actor in actors) {
            highlightActor(actor ?: continue, highlighted)
        }
    }

    override fun computeActorsPosition(): Array<Vector2> {
        // Recompute minimum size
        computeSize()

        // Given this space, find the card spacing and the
        // total dimensions of the layout excluding padding.
        var cardSpacing = 0f
        var reqWidth = cardWidth
        var reqHeight = cardHeight
        if (size > 1) {
            if (horizontal) {
                cardSpacing = ((width - cardWidth) / (size - 1))
                        .coerceIn(minCardSpacing, maxCardSpacing)
                reqWidth = cardSpacing * (size - 1) + cardWidth
                reqHeight = prefHeight
            } else {
                cardSpacing = ((height - cardHeight) / (size - 1))
                        .coerceIn(minCardSpacing, maxCardSpacing)
                reqWidth = prefWidth
                reqHeight = cardSpacing * (size - 1) + cardHeight
            }
        }

        val offset = computeAlignmentOffset(reqWidth, reqHeight)

        // Clipping offset
        if (clipSize > 0) {
            if (horizontal) {
                offset.y -= clipSize
            } else {
                offset.x -= clipSize
            }
        }

        // If vertical, invert Y axis to layout in correct order.
        if (!horizontal) {
            offset.y += reqHeight - cardHeight
        }

        // Find the card positions.
        val positions = Array(actors.size) { Vector2() }
        for (i in actors.indices) {
            val actor = actors[i]
            val pos = positions[i]

            // Translate for highlighting
            var dx = 0f
            var dy = 0f
            if (actor != null && actor.highlighted) {
                if (horizontal) {
                    dy = highlightSize
                } else {
                    dx = highlightSize
                }
            }

            pos.x = offset.x + dx
            pos.y = offset.y + dy

            // Translate for next card.
            if (horizontal) {
                offset.x += cardSpacing
            } else {
                offset.y -= cardSpacing
            }
        }

        return positions
    }

    override fun drawChildren(batch: Batch, parentAlpha: Float) {
        // Clip the region to draw if needed
        val clipped = clipSize.absoluteValue != 0f
        if (clipped) {
            val margin = min(highlightSize, 0f) + 100f  // Space for highlighting + card shadow/hover
            val clip = if (horizontal) {
                val height = cardHeight - clipSize.absoluteValue + margin
                if (clipSize > 0) {
                    Rectangle(-1E9f, 0f, 2E9f, height)
                } else {
                    Rectangle(-1E9f, -margin, 2E9f, height)
                }
            } else {
                val width = cardWidth - clipSize.absoluteValue + margin
                if (clipSize > 0) {
                    Rectangle(0f, -1E9f, width, 2E9f)
                } else {
                    Rectangle(-margin, -1E9f, width, 2E9f)
                }
            }
            val scissors = Rectangle()
            ScissorStack.calculateScissors(stage.camera, batch.transformMatrix, clip, scissors)
            ScissorStack.pushScissors(scissors)
        }

        super.drawChildren(batch, parentAlpha)

        // Unclip
        if (clipped) {
            batch.flush()
            ScissorStack.popScissors()
        }
    }

    override fun computeSize() {
        if (!sizeInvalid) return
        super.computeSize()

        // Find clip and highlight sizes
        require(alignment != Align.center || clipPercent.absoluteValue == 0f) {
            "Cannot clip a CardHand with center alignement."
        }

        val cardSize = if (horizontal) cardHeight else cardWidth
        highlightSize = highlightPercent * cardSize
        clipSize = clipPercent * cardSize

        when {
            horizontal -> {
                computedWidth = max(0, size - 1) * minCardSpacing + cardWidth
                computedHeight = cardHeight - clipSize.absoluteValue
            }
            else -> {
                computedWidth = cardWidth - clipSize.absoluteValue
                computedHeight = max(0, size - 1) * minCardSpacing + cardHeight
            }
        }
    }

    /**
     * If hand and actor are highlightable, change the highlighted state of a card actor.
     * The change is animated and the animation is cancelled is state is changed during it.
     */
    private fun highlightActor(actor: CardActor, highlighted: Boolean) {
        if (!actor.highlightable || actor.highlighted == highlighted) return

        // Call listeners and check if highlighted is allowed.
        if (highlightListener?.onCardActorHighlighted(actor, highlighted) != false) {
            actor.highlighted = highlighted

            // Do the highlight animation.
            val restPos = if (horizontal) actor.y else actor.x
            if (actor.highlighted) {
                actor.addAction(HighlightAction(restPos, highlightSize,
                        horizontal, true, 0f))
            } else {
                val action = actor.actions.find { it is HighlightAction } as HighlightAction?
                if (action != null) {
                    action.highlighted = false
                } else {
                    actor.addAction(HighlightAction(restPos - highlightSize, highlightSize,
                            horizontal, false, Animation.HIGHLIGHT_DURATION))
                }
            }
        }
    }

    /**
     * An action to translate a card actor from its resting
     * position to the highlight position or the opposite.
     */
    private class HighlightAction(private val restPos: Float, private val translate: Float,
                                  private val horizontal: Boolean, var highlighted: Boolean,
                                  var elapsed: Float) : Action() {
        override fun act(delta: Float): Boolean {
            val actor = actor as CardActor
            elapsed += if (highlighted) delta else -delta

            val pos = Interpolation.smooth.applyBounded(elapsed / Animation.HIGHLIGHT_DURATION) * translate + restPos
            if (horizontal) {
                actor.y = pos
            } else {
                actor.x = pos
            }

            return elapsed < 0 || elapsed > Animation.HIGHLIGHT_DURATION
        }
    }

    interface HighlightListener {
        /**
         * Called when a card [actor] in the group is clicked and
         * its [highlighted] state is changed. Return false if highlighting should
         * be blocked. If therre are many listeners, only one needs to return false.
         */
        fun onCardActorHighlighted(actor: CardActor, highlighted: Boolean): Boolean
    }

}