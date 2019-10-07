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

package com.maltaisn.cardgame

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.maltaisn.cardgame.utils.Color
import com.maltaisn.cardgame.utils.FontStyle
import com.maltaisn.cardgame.widget.*
import com.maltaisn.cardgame.widget.markdown.MdHeaderView
import com.maltaisn.cardgame.widget.markdown.MdListView
import com.maltaisn.cardgame.widget.markdown.MdTextView
import com.maltaisn.cardgame.widget.menu.*
import com.maltaisn.cardgame.widget.prefs.*
import com.maltaisn.cardgame.widget.stats.StatView
import com.maltaisn.cardgame.widget.table.ScoresTable
import com.maltaisn.cardgame.widget.table.TableView
import com.maltaisn.cardgame.widget.table.TableViewContent
import com.maltaisn.cardgame.widget.table.TricksTable
import ktx.math.vec2
import ktx.style.*


internal fun buildCoreSkin(atlas: TextureAtlas) = skin(atlas) {
    // Colors
    add("black", Color("#000000"))
    add("white", Color("#ffffff"))
    add("colorPrimary", Color("#a3a3a3"))
    add("colorPrimaryDisabled", Color("#747474"))
    add("colorAccent", Color("#38a047"))
    add("textColorBlack", Color("#1b1b1b"))
    add("textColorLightGray", Color("#ababab"))
    add("textColorGray", Color("#797979"))
    add("textColorDarkGray", Color("#3b3b3b"))
    add("textColorWhite", Color("#e3e3e3"))

    // Font styles
    add("normal", FontStyle {
        size = 44f
        color = get("textColorBlack")
    })
    add("normalBold", FontStyle {
        weight = 0.1f
        size = 44f
        color = get("textColorBlack")
    })
    add("normalInverse", FontStyle {
        size = 44f
        color = get("white")
    })
    add("menuItem", FontStyle {
        weight = 0.1f
        isAllCaps = true
        size = 42f
        color = get("textColorWhite")
        shadowColor = get("black")
    })
    add("importantMenuItem", FontStyle {
        weight = 0.1f
        isAllCaps = true
        size = 52f
        color = get("white")
        shadowColor = get("black")
    })
    add("prefValue", FontStyle {
        size = 48f
        color = get("textColorGray")
    })
    add("title", FontStyle {
        weight = 0.1f
        size = 64f
        color = get("textColorBlack")
    })

    // Base widget styles
    add(CardGameLayout.CardGameLayoutStyle(
            background = get("background"),
            border = get("background-border")
    ))
    add(Dialog.DialogStyle(
            shadow = get("shadow-background"),
            background = get("dialog")
    ))
    add(AlertDialog.AlertDialogStyle(
            titleFontStyle = get("title"),
            messageFontStyle = get("normal")
    ))
    add(Popup.PopupStyle(
            body = get("popup"),
            bodyOffsetX = 0f,
            bodyOffsetY = 14f,
            topTip = get("popup-tip-top"),
            bottomTip = get("popup-tip-bottom"),
            leftTip = get("popup-tip-left"),
            rightTip = get("popup-tip-right"),
            bottomTipOffsetX = -24f,
            bottomTipOffsetY = 5f,
            topTipOffsetX = -24f,
            topTipOffsetY = -7f,
            leftTipOffsetX = -23f,
            leftTipOffsetY = -26f,
            rightTipOffsetX = -2f,
            rightTipOffsetY = -26f
    ))
    add(Button.ButtonStyle(
            background = get("btn-background"),
            selectionOverlay = get("btn-overlay"),
            fontStyle = FontStyle {
                size = 44f
                color = get("textColorBlack")
                shadowColor = get("white")
            },
            disabledAlpha = 0.6f,
            hoverOverlayAlpha = 0.1f,
            pressOverlayAlpha = 0.1f,
            disabledOverlayAlpha = 0.1f
    ))
    add("borderless", Button.ButtonStyle(
            background = null,
            selectionOverlay = get("btn-menu-center"),
            fontStyle = get("normal"),
            disabledAlpha = 0.6f,
            hoverOverlayAlpha = 0.05f,
            pressOverlayAlpha = 0.1f,
            disabledOverlayAlpha = 0.03f
    ))
    add(Separator.SeparatorStyle(
            drawable = get("separator")
    ))
    add(DealerChip.DealerChipStyle(
            drawable = get("dealer-chip")
    ))
    add(Switch.SwitchStyle(
            background = get("switch-background"),
            backgroundColor = get("colorPrimary"),
            checkedColor = get("colorAccent"),
            checkedDisabledColor = get("colorPrimaryDisabled"),
            thumb = get("thumb"),
            thumbOverlay = get("thumb-overlay"),
            thumbSize = 84f
    ))
    add(Slider.SliderStyle(
            track = get("slider-track"),
            trackEmptyColor = get("colorPrimary"),
            trackFilledColor = get("colorAccent"),
            trackFilledDisabledColor = get("colorPrimaryDisabled"),
            thumb = get("thumb"),
            thumbOverlay = get("thumb-overlay"),
            thumbSize = 96f
    ))
    addStyle(defaultStyle, TextField.TextFieldStyle()) {
        background = get("text-field-background")
        focusedBackground = get("text-field-background-focused")
        cursor = get("text-field-cursor")
        selection = get("text-field-selection")
    }
    add(PlayerLabel.PlayerLabelStyle(
            arrowDrawable = get("player-arrow"),
            nameFontStyle = FontStyle {
                weight = 0.1f
                size = 50f
                color = Color("#0000007f")
                isShadowClipped = true
                shadowColor = Color("#ffffff7f")
                shadowOffset = vec2(0.3f, 0.3f)
            },
            scoreFontStyle = FontStyle {
                size = 40f
                color = get("textColorWhite")
                shadowColor = get("black")
            }
    ))
    add(TableViewContent.TableContentGroupStyle(
            background = get("table-view-background"),
            foreground = get("table-view-foreground")
    ))
    add(TableView.TableViewStyle(
            separator = get("table-view-separator"),
            evenRowBackground = get("table-view-row-1"),
            oddRowBackground = get("table-view-row-2")
    ))
    add(MenuButton.MenuButtonStyle(
            backgroundCenter = get("btn-menu-center"),
            backgroundTop = get("btn-menu-top"),
            backgroundBottom = get("btn-menu-bottom"),
            backgroundLeft = get("btn-menu-left"),
            backgroundRight = get("btn-menu-right"),
            selectedColor = get("textColorLightGray"),
            disabledColor = get("textColorLightGray")
    ))

    // Menu styles
    add(MainMenu.MainMenuStyle(
            itemFontStyle = get("menuItem"),
            importantItemFontStyle = get("importantMenuItem"),
            itemIconSize = 80f,
            titleFontStyle = FontStyle {
                weight = 0.07f
                color = Color("#0000003f")
                innerShadowColor = Color("#0000007f")
                innerShadowRange = 0.2f
            }
    ))
    add(SubMenu.SubMenuStyle(
            titleStyle = FontStyle {
                size = 96f
                color = get("textColorWhite")
                shadowColor = get("black")
            },
            backArrowIcon = get("icon-chevron-left"),
            backArrowSize = 96f,
            itemFontStyle = get("menuItem"),
            importantItemFontStyle = get("importantMenuItem"),
            itemIconSize = 64f,
            contentBackground = get("submenu-content-background")
    ))
    add(InGameMenu.InGameMenuStyle(
            itemFontStyle = get("menuItem"),
            importantItemFontStyle = get("importantMenuItem"),
            itemIconSize = 96f
    ))
    add(MenuDrawer.MenuDrawerStyle(
            background = get("shadow-background"),
            drawerBackground = get("menu-drawer"),
            backBtnFontStyle = FontStyle {
                size = 40f
                color = get("textColorBlack")
            },
            backBtnIcon = get("icon-chevron-left"),
            backBtnIconColor = get("textColorDarkGray"),
            titleFontStyle = FontStyle {
                weight = 0.1f
                size = 64f
                color = get("textColorBlack")
            }
    ))
    add(MenuDrawerListItem.DrawerListItemStyle(
            fontStyle = get("normal"),
            selectionOverlay = get("menu-drawer-item-selection")
    ))

    // Scoreboard styles
    add(ScoresTable.ScoresTableStyle(
            headerTitleFontStyle = get("normalBold"),
            headerSubtitleFontStyle = FontStyle {
                size = 40f
                color = get("textColorGray")
            },
            scoreFontStyle = get("normal"),
            scoreFontStyleHighlighted = get("normalInverse"),
            scoreHighlightPositive = get("scores-table-highlight-positive"),
            scoreHighlightNegative = get("scores-table-highlight-negative")
    ))
    add(TricksTable.TricksTableStyle(
            headerFontStyle = get("normalBold"),
            checkIcon = get("table-view-checkbox")
    ))

    // Preference styles
    add(PrefsGroup.PrefsGroupStyle(
            prefsHelpFontStyle = get("normal")
    ))
    add(GamePrefView.GamePrefViewStyle(
            titleFontStyle = get("normal"),
            helpIcon = get("pref-help")
    ))
    add(PrefCategoryView.PrefCategoryViewStyle(
            titleFontStyle = get("title")
    ))
    add(SliderPrefView.SliderPrefViewStyle(
            valueFontStyle = get("prefValue")
    ))
    add(ListPrefView.ListPrefViewStyle(
            valueFontStyle = get("prefValue"),
            arrowIcon = get("icon-chevron-right"),
            arrowIconColor = get("textColorGray")
    ))
    add(TextPrefView.TextPrefViewStyle(
            fieldFontStyle = get("normal")
    ))
    add(PlayerNamesPrefView.PlayerNamesPrefViewStyle(
            fieldFontStyle = get("normal")
    ))

    // Markdown styles
    add(MdHeaderView.MdHeaderViewStyle(
            fontStyles = listOf(
                    get("title"),
                    FontStyle {
                        weight = 0.1f
                        size = 48f
                        color = get("textColorBlack")
                    })
    ))
    add(MdListView.MdListViewStyle(
            numberFontStyle = get("normal"),
            bulletDrawables = listOf(
                    get("md-bullet-1"),
                    get("md-bullet-2"),
                    get("md-bullet-3"))
    ))
    add(MdTextView.MdTextViewStyle(
            fontStyle = get("normal")
    ))

    // Statistics styles
    add(StatView.StatViewStyle(
            titleFontStyle = get("normal"),
            valueFontStyle = get("prefValue")
    ))

    // Compound views styles
    add(DefaultGameMenu.DefaultGameMenuStyle(
            defaultIcon = get("icon-cards"),
            newGameIcon = get("icon-cards"),
            continueIcon = get("icon-arrow-right"),
            settingsIcon = get("icon-settings"),
            rulesIcon = get("icon-book"),
            statsIcon = get("icon-list"),
            aboutIcon = get("icon-info"),
            startGameIcon = get("icon-arrow-right"),
            backBtnIcon = get("icon-chevron-left"),
            scoreboardBtnIcon = get("icon-chart")
    ))
    add(AboutView.AboutViewStyle(
            appNameFontStyle = FontStyle {
                size = 80f
                color = get("textColorBlack")
            },
            authorFontStyle = FontStyle {
                size = 44f
                color = get("textColorGray")
            },
            versionFontStyle = FontStyle {
                size = 40f
                color = get("textColorDarkGray")
            },
            buttonStyle = get("borderless"),
            buttonIconColor = get("textColorGray")
    ))
}
