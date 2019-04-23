package io.github.maltaisn.cardgame.widget.markdown

import com.badlogic.gdx.scenes.scene2d.ui.Container
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import io.github.maltaisn.cardgame.markdown.MdElement
import io.github.maltaisn.cardgame.widget.FontStyle
import io.github.maltaisn.cardgame.widget.SdfLabel


class MdTextView(skin: Skin, element: MdElement.Text) : Container<SdfLabel>() {

    init {
        val style = skin[MdTextViewStyle::class.java]
        actor = SdfLabel(skin, style.fontStyle, element.text)
        actor.setWrap(true)
        fill().pad(5f, 0f, 10f, 0f)
    }

    class MdTextViewStyle {
        lateinit var fontStyle: FontStyle
    }

}