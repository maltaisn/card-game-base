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

package com.maltaisn.cardgame.widget.prefs

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.I18NBundle
import com.maltaisn.cardgame.CoreRes
import com.maltaisn.cardgame.prefs.GamePref
import com.maltaisn.cardgame.widget.AlertDialog
import ktx.actors.onClick
import ktx.style.get


class ResetGameDialog(skin: Skin) : AlertDialog(skin) {

    //@GDXAssets(propertiesFiles = ["assets/core/strings.properties"])
    private val strings: I18NBundle = skin[CoreRes.CORE_STRINGS_NAME]

    var pref: GamePref<*>? = null

    var callback: ((keep: Boolean) -> Unit)? = null


    init {
        dialogWidth = 800f
        shadowType = ShadowType.DISMISSABLE

        title = strings["prefs_reset_game"]

        val noBtn = addButton(strings["action_no"])
        noBtn.onClick {
            dismiss()
        }

        val yesBtn = addButton(strings["action_yes"])
        yesBtn.onClick {
            callback?.invoke(true)
            hide()
        }
    }


    override fun onShow() {
        val pref = checkNotNull(pref) { "ResetGameDialog cannot be shown without a preference set." }
        message = strings.format("prefs_reset_game_message", pref.shortTitle ?: pref.title)
    }

    override fun onHide() {
        pref = null
        callback = null

        // Remove keyboard focus because if a text field was being edited, dialog was shown during
        // keyboard focus change and dialog set it back to text field when hiding instead of removing it.
        stage?.keyboardFocus = null
    }

    override fun onDismiss() {
        callback?.invoke(false)
    }

}
