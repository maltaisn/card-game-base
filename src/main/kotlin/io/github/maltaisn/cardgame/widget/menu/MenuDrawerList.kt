package io.github.maltaisn.cardgame.widget.menu

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup
import ktx.actors.onClick
import ktx.assets.pool


class MenuDrawerList(skin: Skin) : VerticalGroup() {

    /** The list of text items in the list. */
    var items = emptyList<String>()
        set(value) {
            field = value

            // Clear and free previous items
            selectedIndex = -1
            for (actor in itemActors) {
                itemActorPool.free(actor)
            }
            itemActors.clear()
            clearChildren()

            // Add new items
            for ((i, item) in value.withIndex()) {
                val actor = itemActorPool.obtain()
                actor.text = item
                actor.onClick { selectedIndex = i }
                itemActors += actor
                addActor(actor)
            }
        }

    /** The currently selected index in the list of [items], or `-1` for none. */
    var selectedIndex = -1
        set(value) {
            if (field != value) {
                if (field != -1) itemActors[field].checked = false
                if (value != -1) itemActors[value].checked = true
                field = value
                selectionChangeListener?.invoke(value)
            }
        }

    /** The listener called when the selected item is changed. */
    var selectionChangeListener: ((selectedIndex: Int) -> Unit)? = null

    private val itemActors = mutableListOf<MenuDrawerListItem>()
    private val itemActorPool = pool { MenuDrawerListItem(skin) }

    init {
        grow().space(5f)
    }

}