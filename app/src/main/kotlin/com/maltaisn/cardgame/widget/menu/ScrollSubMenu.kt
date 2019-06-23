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

package com.maltaisn.cardgame.widget.menu

import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.ui.Container
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.maltaisn.cardgame.widget.ScrollView
import com.maltaisn.cardgame.widget.menu.ScrollSubMenu.Section
import ktx.actors.setScrollFocus


/**
 * A sub menu with a single page of scrollable content, where items scroll to different sections.
 *
 * Items that should scroll to a content section must have sequential IDs starting at 0.
 * The content group children will be iterated on to determine which section to scroll to.
 * Actors that implements [Section] are considered section containers.
 */
class ScrollSubMenu(skin: Skin) : SubMenu(skin) {

    /** The container in the submenu scroll pane. Change its actor to change the scrollable content. */
    val scrollContent = Container<Group>()

    /** The scroll view containing the [scrollContent] container. */
    val scrollView = ScrollView(scrollContent)

    /** Should be shown after being added to the stage to properly gain scroll focus. */
    override var shown
        get() = super.shown
        set(value) {
            if (super.shown == value) return
            super.shown = value

            scrollView.setScrollFocus(value)
        }

    override var itemClickListener: ((item: MenuItem) -> Unit)?
        get() = super.itemClickListener
        set(value) {
            super.itemClickListener = {
                if (listenerEnabled) {
                    scrollToItem(it)
                    value?.invoke(it)
                }
            }
        }

    private var listenerEnabled = true

    init {
        itemClickListener = null  // Adds the scroll on click action

        content.add(scrollView).grow()

        scrollContent.fill()
        scrollView.scrollListener = { scrollView: ScrollView, _: Float, y: Float, _: Float, _: Float ->
            // Change the checked menu item if needed
            var newId = MenuItem.NO_ID
            val oldId = checkedItem?.id ?: MenuItem.NO_ID
            when {
                scrollView.isTopEdge -> {
                    // Scrolled to top edge, check the item with ID 0
                    for (item in items) {
                        if (item.checkable && item.id == 0) {
                            newId = item.id
                            break
                        }
                    }
                }
                scrollView.isBottomEdge -> {
                    // Scrolled to bottom edge, check last item with a corresponding section
                    newId = -1
                    val content = scrollContent.actor
                    for (child in content.children) {
                        if (child is Section) {
                            newId++
                        }
                    }
                }
                else -> {
                    // Find the view of the currently checked category
                    var id = 0
                    val content = scrollContent.actor
                    for (child in content.children) {
                        if (child is Section) {
                            if (id == oldId) {
                                val bottom = child.y
                                val top = bottom + child.height

                                // If currently checked category is completely hidden, check another
                                val maxY = content.height - y
                                val minY = maxY - scrollView.height
                                val tooHigh = top > maxY && bottom > maxY
                                val tooLow = top < minY && bottom < minY
                                if (tooHigh || tooLow) {
                                    // If too high check next category, otherwise check previous.
                                    newId = (oldId + if (tooHigh) 1 else -1).coerceIn(0, items.size - 1)
                                }
                                break
                            }
                            id++
                        }
                    }
                }
            }
            if (newId != MenuItem.NO_ID && newId != oldId) {
                listenerEnabled = false
                items.find { it.id == newId }?.checked = true
                listenerEnabled = true
            }
        }

        doMenuLayout()
    }

    private fun scrollToItem(item: MenuItem) {
        // Scroll the scroll view to the top of the item's section.
        var id = 0

        if (scrollContent.actor != null) {
            for (child in scrollContent.actor.children) {
                if (child is Section) {
                    if (id == item.id) {
                        val top = child.y + child.height + 20f - scrollView.height
                        val height = scrollView.height
                        scrollView.scrollTo(0f, top, 0f, height)
                        break
                    }
                    id++
                }
            }
        }
    }

    /**
     * Scroll to the top of the content and check the first item.
     */
    fun scrollToTop() {
        // Check the first checkable item
        if (items.isNotEmpty() && checkable) {
            for (item in items) {
                if (item.checkable) {
                    item.checked = true
                    break
                }
            }
        }

        scrollView.scrollToTop()
    }

    override fun doMenuLayout() {
        super.doMenuLayout()
        scrollView.setScrollFocus(shown)
    }

    /**
     * A marker interface for a content section in a scroll submenu content.
     * The actor that implements this must be a group that contains all actors of its section.
     */
    interface Section

}