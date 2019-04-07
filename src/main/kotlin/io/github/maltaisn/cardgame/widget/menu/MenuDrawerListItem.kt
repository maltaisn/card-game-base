package io.github.maltaisn.cardgame.widget.menu

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.utils.Pool
import io.github.maltaisn.cardgame.widget.CheckableWidget
import io.github.maltaisn.cardgame.widget.SdfLabel


class MenuDrawerListItem(skin: Skin) : CheckableWidget(), Pool.Poolable {

    /** The item text. */
    var text: CharSequence?
        set(value) = label.setText(value)
        get() = label.text

    private val itemStyle = skin[DrawerListItemStyle::class.java]
    private val label = SdfLabel(null, skin, itemStyle.fontStyle)

    private val inputListener = SelectionListener()

    init {
        label.setWrap(true)
        label.touchable = Touchable.disabled

        touchable = Touchable.enabled
        add(label).grow().pad(15f, 30f, 15f, 30f)

        setSize(prefWidth, prefHeight)

        addListener(inputListener)
    }


    override fun reset() {
        text = null
        check(false, false)
        hoverAction = null
        hoverAlpha = 0f
        pressAction = null
        pressAlpha = 0f
        clearActions()
        clearListeners()
        addListener(inputListener)
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        super.draw(batch, parentAlpha)

        // Draw selection/hover overlay
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha *
                (checkAlpha * 0.2f + pressAlpha * 0.1f + hoverAlpha * 0.1f))
        itemStyle.selectionOverlay.draw(batch, x, y, width, height)
    }


    class DrawerListItemStyle {
        lateinit var fontStyle: SdfLabel.FontStyle
        lateinit var selectionOverlay: Drawable
    }

}