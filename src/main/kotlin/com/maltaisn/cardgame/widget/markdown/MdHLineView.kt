package com.maltaisn.cardgame.widget.markdown

import com.badlogic.gdx.scenes.scene2d.ui.Container
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.utils.Scaling
import com.maltaisn.cardgame.widget.menu.MenuContentSection


class MdHLineView(skin: Skin) : Container<Image>(), MenuContentSection {

    init {
        val style = skin[MdHLineViewStyle::class.java]
        actor = Image(style.separator, Scaling.stretchX)
        fillX().pad(15f, 0f, 15f, 0f)
    }

    class MdHLineViewStyle {
        lateinit var separator: Drawable
    }

}