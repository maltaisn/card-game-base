package io.github.maltaisn.cardgame.widget.markdown

import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import io.github.maltaisn.cardgame.markdown.MdElement
import io.github.maltaisn.cardgame.widget.FontStyle
import io.github.maltaisn.cardgame.widget.SdfLabel
import io.github.maltaisn.cardgame.widget.menu.MenuContentSection


class MdListView(skin: Skin, list: MdElement.List) : Table(), MenuContentSection {

    init {
        val style = skin[MdListViewStyle::class.java]
        pad(0f, 5f, 10f, 0f)

        var n = 1
        for ((i, element) in list.elements.withIndex()) {
            if (element !is MdElement.List) {
                // Add list item marker
                if (list.type == MdElement.List.Type.NUMBER) {
                    val label = SdfLabel(skin, style.numberFontStyle, list.getItemMarker(n) + '.')
                    label.setAlignment(Align.right)
                    add(label).minWidth(30f).pad(5f, 5f, 10f, 5f)
                } else {
                    val bullet = Image(style.bulletDrawables[list.level], Scaling.fit)
                    add(bullet).size(10f, 30f).pad(5f, 5f, 10f, 5f)
                }.align(Align.top)
                n++
            } else {
                add()
            }

            add(element.createView(skin)).growX().padLeft(10f).row()
        }
    }


    class MdListViewStyle {
        lateinit var numberFontStyle: FontStyle
        lateinit var bulletDrawables: Array<Drawable>
    }

}