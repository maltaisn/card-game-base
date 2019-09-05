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

package com.maltaisn.cardgame.tests.core

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.maltaisn.msdfgdx.FontStyle as _FontStyle


/**
 * Utility function to create a new font style
 * with constructor-like syntax and default parameters.
 */
fun fontStyle(fontName: String = FONT_STYLE_DEFAULTS.fontName,
              size: Float = FONT_STYLE_DEFAULTS.size,
              weight: Float = FONT_STYLE_DEFAULTS.weight,
              color: Color = FONT_STYLE_DEFAULTS.color.cpy(),
              isAllCaps: Boolean = FONT_STYLE_DEFAULTS.isAllCaps,
              shadowColor: Color = FONT_STYLE_DEFAULTS.shadowColor.cpy(),
              shadowOffset: Vector2 = FONT_STYLE_DEFAULTS.shadowOffset.cpy(),
              shadowSmoothing: Float = FONT_STYLE_DEFAULTS.shadowSmoothing,
              innerShadowColor: Color = FONT_STYLE_DEFAULTS.innerShadowColor.cpy(),
              innerShadowRange: Float = FONT_STYLE_DEFAULTS.innerShadowRange) = _FontStyle().apply {
    this.fontName = fontName
    this.size = size
    this.weight = weight
    this.color = color
    this.isAllCaps = isAllCaps
    this.shadowColor = shadowColor
    this.shadowOffset = shadowOffset
    this.shadowSmoothing = shadowSmoothing
    this.innerShadowColor = innerShadowColor
    this.innerShadowRange = innerShadowRange
}

private val FONT_STYLE_DEFAULTS = _FontStyle()
