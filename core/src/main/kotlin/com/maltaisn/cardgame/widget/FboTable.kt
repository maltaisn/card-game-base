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

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table


open class FboTable(skin: Skin? = null) : Table(skin), FboActor {

    override var renderToFrameBuffer = false

    override fun draw(batch: Batch, parentAlpha: Float) {
        drawFboActor(batch, parentAlpha)
    }

    override fun delegateDraw(batch: Batch, parentAlpha: Float) {
        super.draw(batch, parentAlpha)
    }

}