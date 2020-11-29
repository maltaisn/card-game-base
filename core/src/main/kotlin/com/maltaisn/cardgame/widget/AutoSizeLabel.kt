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

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.maltaisn.msdfgdx.FontStyle
import com.maltaisn.msdfgdx.widget.MsdfLabel
import kotlin.math.max


/**
 * A MSDF label widget that adjusts its text size to fill its entire size.
 * @param fontStyle Label font style. A copy is made so that the original
 * doesn't change when the label changes its text size.
 */
class AutoSizeLabel(text: CharSequence?, skin: Skin, fontStyle: FontStyle) :
        MsdfLabel(text, skin, FontStyle(fontStyle)) {

    /**
     * The minimum font size that can be displayed.
     */
    var minTextSize = 32f
        set(value) {
            require(value in 1f..1024f) { "Min text size must be between 1 and 1024" }
            field = value
            textSizes = null
            invalidate()
        }

    /**
     * The maximum font size that can be displayed.
     */
    var maxTextSize = 64f
        set(value) {
            require(value in 1f..1024f) { "Max text size must be between 1 and 1024" }
            field = value
            textSizes = null
            invalidate()
        }

    /**
     * The amount by which the font size changes.
     */
    var granularity = 4f
        set(value) {
            require(value in 1f..1024f) { "Granularity must be between 1 and 1024." }
            field = value
            textSizes = null
            invalidate()
        }


    private var ellipsis: String? = null

    private var textSizes: FloatArray? = null


    init {
        createTextSizes()
    }


    override fun layout() {
        createTextSizes()

        val size = findBestTextSize()
        if (size != fontStyle.size) {
            // Update text size, will relayout.
            fontStyle.size = size
            fontStyle = fontStyle
        }

        super.layout()
    }

    override fun setEllipsis(ellipsis: Boolean) {
        super.setEllipsis(ellipsis)
        this.ellipsis = if (ellipsis) "..." else null
        invalidate()
    }

    override fun setEllipsis(ellipsis: String?) {
        super.setEllipsis(ellipsis)
        this.ellipsis = ellipsis
    }

    /**
     * Create the array of text sizes from the options.
     */
    private fun createTextSizes() {
        if (textSizes != null) return

        check(minTextSize <= maxTextSize) {
            "Max text size must be greater or equal to min font size."
        }

        var count = max(1, ((maxTextSize - minTextSize) / granularity).toInt())
        if (maxTextSize % granularity == 0f) count++
        textSizes = FloatArray(count) { minTextSize + it * granularity }
    }

    /**
     * Return the largest text size that fits in the current label bounds.
     */
    private fun findBestTextSize(): Float {
        val sizes = checkNotNull(textSizes)

        // Calculate available space
        val bg = background
        val width = width - if (bg != null) bg.leftWidth + bg.rightWidth else 0f
        val height = height - if (bg != null) bg.topHeight + bg.bottomHeight else 0f

        // Find maximum fitting size with binary search
        var best = 0
        var low = 1
        var high = sizes.lastIndex
        while (low <= high) {
            val middle = (low + high) / 2
            if (textSizeFitsIn(sizes[middle], width, height)) {
                best = low
                low = middle + 1
            } else {
                high = middle - 1
                best = high
            }
        }

        return sizes[best]
    }

    /**
     * Returns true if the text of this label fits into a [width] and [height] at a [size].
     */
    private fun textSizeFitsIn(size: Float, width: Float, height: Float): Boolean {
        val data = font.font.data
        val oldScaleX = data.scaleX
        val oldScaleY = data.scaleY
        data.setScale(size / font.glyphSize)
        glyphLayout.setText(font.font, txt, 0, txt.length,
                Color.WHITE, width, lineAlign, wrap, ellipsis)
        data.setScale(oldScaleX, oldScaleY)
        return glyphLayout.width < width && glyphLayout.height < height
    }

}
