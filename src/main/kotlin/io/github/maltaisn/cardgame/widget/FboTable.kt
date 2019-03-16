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

package io.github.maltaisn.cardgame.widget

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import io.github.maltaisn.cardgame.CardGameScreen
import ktx.actors.alpha


/**
 * A table that can draw its content on an offscreen
 * frame buffer before drawing it to the screen batch.
 * Note that any child of this table may not use the frame buffer for drawing.
 */
open class FboTable(skin: Skin? = null) : Table(skin) {

    /** Whether to render table to frame buffer first, then to screen. */
    var renderToFrameBuffer = false

    override fun setStage(stage: Stage?) {
        require(stage == null || stage is CardGameScreen) {
            "FboTable must be added to a CardGameScreen stage."
        }
        super.setStage(stage)
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        if (renderToFrameBuffer) {
            val stage = stage as CardGameScreen
            val fbo = stage.offscreenFbo

            // Change blending function to avoid blending twice: when drawn to FBO and when FBO is drawn to screen
            // https://gist.github.com/mattdesl/4393861
            batch.enableBlending()
            batch.setBlendFunctionSeparate(GL20.GL_SRC_ALPHA,
                    GL20.GL_ONE_MINUS_SRC_ALPHA, GL20.GL_ONE, GL20.GL_ONE)

            fbo.begin()

            // Clear the frame buffer
            Gdx.gl.glClearColor(0f, 0f, 0f, 0f)
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

            // Draw the table content
            // Since the alpha of this actor and its parent is handled with the frame buffer, draw children with no transparency.
            val oldAlpha = alpha
            alpha = 1f
            super.draw(batch, 1f)
            alpha = oldAlpha

            fbo.end()

            // Draw the frame buffer to the screen batch
            val a = alpha * parentAlpha
            batch.setColor(a, a, a, a)
            batch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA)  // Premultiplied alpha blending mode
            batch.draw(stage.offscreenFboRegion, 0f, 0f, stage.width, stage.height)
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)

        } else {
            super.draw(batch, parentAlpha)
        }
    }

}