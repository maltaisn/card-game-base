package io.github.maltaisn.cardgame.widget.markdown

import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.utils.Scaling
import io.github.maltaisn.cardgame.markdown.MdElement
import io.github.maltaisn.cardgame.widget.FontStyle
import io.github.maltaisn.cardgame.widget.SdfLabel
import io.github.maltaisn.cardgame.widget.menu.MenuContentSection


class MdHeaderView(skin: Skin, header: MdElement.Header) : Table(), MenuContentSection {

    init {
        val style = skin[MdHeaderViewStyle::class.java]
        val headerLabel = SdfLabel(skin, style.fontStyles[header.size - 1], header.text)
        headerLabel.setWrap(true)
        add(headerLabel).growX().pad(20f, 0f, 5f, 0f).row()

        if (header.size == 1) {
            val separator = Image(style.separator, Scaling.stretchX)
            add(separator).growX().pad(10f, 0f, 10f, 0f).row()
        }

        for (element in header.elements) {
            add(element.createView(skin)).growX().padBottom(5f).row()
        }
    }

    class MdHeaderViewStyle {
        /** Array of header font styles, from big to small. */
        lateinit var fontStyles: Array<FontStyle>

        lateinit var separator: Drawable
    }

}