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

package io.github.maltaisn.cardengine

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Interpolation.*
import io.github.maltaisn.cardengine.widget.AnimationLayer
import io.github.maltaisn.cardengine.widget.CardHand

/**
 * Stores all constants related to animation.
 * All durations and in seconds.
 */
internal object Animation {

    /** The duration of the update animation for [AnimationLayer.update] */
    const val UPDATE_DURATION = 0.4f

    /** The duration between each card dealt with [AnimationLayer.deal]
     * Values less than the update duration look worse. */
    const val DEAL_DELAY: Float = 0.4f

    /** The duration of the card size animation when the dragged card hovers a container */
    const val DRAG_SIZE_CHANGE_DURATION = 0.25f

    /** The duration of rearrangement animation when cards are dragged.  */
    const val DRAG_REARRANGE_DURATION: Float = 0.3f

    /** The duration of card container's transitions, slide and fade */
    const val TRANSITION_DURATION = 0.5f

    /** The duration of the highlight animation for [CardHand]. */
    const val HIGHLIGHT_DURATION = 0.1f

    /** The minimum dragging distance in pixels for a card to be effectively dragged. */
    const val MIN_DRAG_DISTANCE = 10f

    /** The duration of the hover fade for a card actor. */
    const val HOVER_FADE_DURATION = 0.3f

    /** The duration of the selection fade for a card actor. */
    const val SELECTION_FADE_DURATION = 0.3f

    /** The delay before long click is triggered in a card actor. */
    const val LONG_CLICK_DELAY = 0.5f


    // Interpolation
    val SELECTION_IN_INTERPOLATION: Interpolation = smooth
    val SELECTION_OUT_INTERPOLATION: Interpolation = smooth
    val HOVER_IN_INTERPOLATION: Interpolation = circleOut
    val HOVER_OUT_INTERPOLATION: Interpolation = smooth
    val UPDATE_INTERPOLATION: Interpolation = smooth
    val DRAG_SIZE_INTERPOLATION: Interpolation = pow2Out
    val REARRANGE_INTERPOLATION: Interpolation = pow2Out
    val TRANSITION_INTERPOLATION: Interpolation = smooth
    val HIGHLIGHT_INTERPOLATION: Interpolation = smooth

}