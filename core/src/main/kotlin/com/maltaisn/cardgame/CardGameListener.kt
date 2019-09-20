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


/**
 * Listener to be implemented for each backend to provide backend dependent behavior
 */
interface CardGameListener {

    /**
     * Whether to delegate text input to [onTextInput] or use default input method.
     */
    val isTextInputDelegated: Boolean

    /**
     * Prompt user for text input.
     * @param text Current text, null for none.
     * @param title Input title/hint, null for none.
     * @param onTextEntered Callback to call when text has been entered.
     */
    fun onTextInput(text: CharSequence?, title: CharSequence?,
                    onTextEntered: ((String) -> Unit)) = Unit

}
