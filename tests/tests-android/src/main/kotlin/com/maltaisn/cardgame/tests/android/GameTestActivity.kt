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

package com.maltaisn.cardgame.tests.android

import android.os.Bundle
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.maltaisn.cardgame.CardGameApp
import com.maltaisn.cardgame.CardGameListener
import com.maltaisn.cardgame.tests.core.CardGameTests


class GameTestActivity : AndroidApplication(), CardGameListener {

    private lateinit var inputDialog: AlertDialog
    private lateinit var inputField: TextView

    override val isTextInputDelegated = true


    override fun onCreate(state: Bundle?) {
        super.onCreate(state)

        // Input dialog
        val view = layoutInflater.inflate(R.layout.dialog_input, null, false)
        inputField = view.findViewById(R.id.input_field)
        inputField.setOnEditorActionListener { _, actionId, _ ->
            return@setOnEditorActionListener if (actionId == EditorInfo.IME_ACTION_DONE) {
                inputDialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick()
                true
            } else {
                false
            }
        }
        inputDialog = AlertDialog.Builder(this).apply {
            setView(view)
            setTitle("")
            setPositiveButton(R.string.action_ok, null)
            setNegativeButton(R.string.action_cancel, null)
        }.create()

        // Start game
        val config = AndroidApplicationConfiguration()
        config.useAccelerometer = false
        config.useCompass = false
        initialize(object : CardGameApp<CardGameListener>(this) {
            override fun create() {
                val testName = checkNotNull(intent?.extras?.getString(EXTRA_TEST_NAME)) { "Test cannot be null." }
                setScreen(CardGameTests.newTest<CardGameListener>(testName, this@GameTestActivity))
            }
        }, config)
    }

    override fun onTextInput(text: CharSequence?, title: CharSequence?,
                             onTextEntered: (String) -> Unit) {
        runOnUiThread {
            inputField.text = text
            inputField.requestFocus()
            inputDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

            inputDialog.apply {
                setTitle(title)
                setOnShowListener {
                    getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        inputDialog.dismiss()
                        onTextEntered(inputField.text.toString())
                    }
                }
                show()
            }
        }
    }

    companion object {
        const val EXTRA_TEST_NAME = "test_name"
    }

}
