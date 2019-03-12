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

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.Action
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.I18NBundle
import com.gmail.blueboxware.libgdxplugin.annotations.GDXAssets
import io.github.maltaisn.cardgame.Resources
import io.github.maltaisn.cardgame.applyBounded
import ktx.actors.alpha
import ktx.actors.plusAssign
import ktx.style.get


class MainMenu(skin: Skin) : Table() {

    val style = skin.get(MainMenuStyle::class.java)

    val topButtons = ArrayList<MenuButton>()
    val bottomButtons = ArrayList<MenuButton>()

    private val topRow = FrameBufferTable()
    private val bottomRow = FrameBufferTable()

    /** Whether the menu is shown or not. Like [isVisible] but with the correct value during a transition. */
    var shown = true
        private set

    init {
        // Do the layout
        add(topRow).growX().height(100f).pad(0f, 10f, 0f, 10f).row()
        add().expand().row()
        add(bottomRow).height(100f).pad(0f, 10f, 0f, 10f).growX()

        buildDefaultMenu(skin)
    }

    private fun buildDefaultMenu(skin: Skin) {
        @GDXAssets(propertiesFiles = ["assets/core/strings.properties"])
        val bundle: I18NBundle = skin[Resources.CORE_STRINGS_NAME]
        val icons = style.menuIcons
        val btnStyle = style.buttonStyle

        val rulesBtn = MenuButton(skin, btnStyle, bundle.get("mainmenu_rules"), icons.book)
        rulesBtn.clickListener = object : MenuButton.ClickListener {
            override fun onMenuButtonClicked(button: MenuButton) {
                // Nothing for now
            }
        }
        topButtons += rulesBtn

        val statsBtn = MenuButton(skin, btnStyle, bundle.get("mainmenu_stats"), icons.list)
        statsBtn.clickListener = object : MenuButton.ClickListener {
            override fun onMenuButtonClicked(button: MenuButton) {
                // Nothing for now
            }
        }
        topButtons += statsBtn

        val aboutBtn = MenuButton(skin, btnStyle, bundle.get("mainmenu_about"), icons.info)
        aboutBtn.clickListener = object : MenuButton.ClickListener {
            override fun onMenuButtonClicked(button: MenuButton) {
                // Nothing for now
            }
        }
        topButtons += aboutBtn

        val newGameBtn = MenuButton(skin, btnStyle, bundle.get("mainmenu_new_game"), icons.cards)
        newGameBtn.clickListener = object : MenuButton.ClickListener {
            override fun onMenuButtonClicked(button: MenuButton) {
                // Nothing for now
            }
        }
        bottomButtons += newGameBtn

        val continueBtn = MenuButton(skin, btnStyle, bundle.get("mainmenu_continue"), icons.arrowRight)
        continueBtn.clickListener = object : MenuButton.ClickListener {
            override fun onMenuButtonClicked(button: MenuButton) {
                // Nothing for now
            }
        }
        bottomButtons += continueBtn

        val settingsBtn = MenuButton(skin, btnStyle, bundle.get("mainmenu_settings"), icons.settings)
        settingsBtn.clickListener = object : MenuButton.ClickListener {
            override fun onMenuButtonClicked(button: MenuButton) {
                // Nothing for now
            }
        }
        bottomButtons += settingsBtn

        updateMenuLayout()
    }

    /**
     * Redo the menu layout, must be called if new items are added.
     */
    fun updateMenuLayout() {
        topRow.clearChildren()
        for (btn in topButtons) {
            btn.anchorSide = MenuButton.Side.TOP
            btn.iconSide = MenuButton.Side.LEFT
            btn.iconSize = style.iconSize
            topRow.add(btn).grow().pad(0f, 15f, 0f, 15f)
        }

        bottomRow.clearChildren()
        for (btn in bottomButtons) {
            btn.anchorSide = MenuButton.Side.BOTTOM
            btn.iconSide = MenuButton.Side.LEFT
            btn.iconSize = style.iconSize
            bottomRow.add(btn).grow().pad(0f, 15f, 0f, 15f)
        }
    }

    /**
     * Animate a visibility change by sliding the top row up and the bottom row down
     * when hiding, the oppose when showing. A slide can be performed during
     * another slide, the previous one will be canceled.
     * @param shown New visibility.
     */
    fun slide(shown: Boolean) {
        if (this.shown == shown) return
        this.shown = shown

        if (topRow.actions.isEmpty) {
            topRow += TransitionAction(topRow, false)
            bottomRow += TransitionAction(bottomRow, true)
        }
    }

    private inner class TransitionAction(private val table: FrameBufferTable,
                                         private val slideDown: Boolean) : Action() {
        private var elapsed = if (shown) 0f else TRANSITION_DURATION

        init {
            table.isVisible = true
            table.drawOffset.setZero()
            table.alpha = if (shown) 0f else 1f
            table.renderToFrameBuffer = true
        }

        override fun act(delta: Float): Boolean {
            elapsed += if (shown) delta else -delta
            val progress = TRANSITION_INTERPOLATION.applyBounded(elapsed / TRANSITION_DURATION)
            table.drawOffset.y = (1 - progress) * table.height * if (slideDown) -1 else 1
            table.alpha = progress

            if (shown && progress >= 1 || !shown && progress <= 0) {
                table.isVisible = shown
                table.renderToFrameBuffer = false
                return true
            }
            return false
        }
    }

    class MainMenuStyle {
        lateinit var menuIcons: MenuIcons
        lateinit var buttonStyle: MenuButton.MenuButtonStyle
        var iconSize = 0f
    }

    companion object {
        private const val TRANSITION_DURATION = 0.5f

        private val TRANSITION_INTERPOLATION = Interpolation.smooth
    }

}