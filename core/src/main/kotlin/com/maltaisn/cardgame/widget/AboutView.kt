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
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.maltaisn.cardgame.utils.padV
import com.maltaisn.msdfgdx.FontStyle
import com.maltaisn.msdfgdx.widget.MsdfLabel
import ktx.style.get


class AboutView(private val skin: Skin,
                appName: String, version: String, author: String) : ScrollView() {

    private val style: AboutViewStyle = skin.get()

    private val content = Table()

    init {
        val appNameLabel = MsdfLabel(appName, skin, style.appNameFontStyle)
        val versionLabel = MsdfLabel("v$version", skin, style.versionFontStyle)
        val authorLabel = MsdfLabel(author, skin, style.authorFontStyle)
        val separator = Separator(skin)

        // Do the layout
        actor = content
        content.apply {
            pad(100f, 50f, 100f, 50f)
            add(appNameLabel).pad(0f, 0f, 5f, 0f).expandX().row()
            add(versionLabel).padV(5f).expandX().row()
            add(authorLabel).padV(20f).expandX().row()
            add(separator).pad(40f, 100f, 40f, 100f).growX().row()
        }
    }

    /**
     * Add a new button with a [title] and an [icon].
     */
    fun addButton(title: String, icon: Drawable): Button {
        // Create image button
        val image = Image(icon)
        image.color = style.buttonIconColor

        val btn = Button(skin, style.buttonStyle, title).apply {
            clearChildren()
            add(image).pad(0f, 10f, 0f, 0f)
            add(label).pad(0f, 50f, 0f, 30f)
        }

        // Add it to the content table
        content.add(btn).pad(10f).minWidth(400f).expandX().row()

        return btn
    }

    class AboutViewStyle {
        lateinit var appNameFontStyle: FontStyle
        lateinit var authorFontStyle: FontStyle
        lateinit var versionFontStyle: FontStyle
        lateinit var buttonStyle: Button.ButtonStyle
        lateinit var buttonIconColor: Color
    }

}
