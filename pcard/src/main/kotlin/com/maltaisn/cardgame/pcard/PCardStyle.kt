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

package com.maltaisn.cardgame.pcard

import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.maltaisn.cardgame.widget.card.CardActor


/**
 * Style for a [PCard] in a [CardActor].
 */
class PCardStyle(
        cards: List<Drawable>,
        back: Drawable,
        background: Drawable,
        hover: Drawable,
        selection: Drawable,
        slot: Drawable,
        cardWidth: Float,
        cardHeight: Float,
        val suitIcons: List<Drawable>
) : CardActor.CardStyle(cards, back, background, hover,
        selection, slot, cardWidth, cardHeight)
