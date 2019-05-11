/*
 * Copyright 2019 Nicolas Maltais
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.maltaisn.cardgame.markdown

import com.badlogic.gdx.assets.AssetLoaderParameters
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader
import com.badlogic.gdx.assets.loaders.FileHandleResolver
import com.badlogic.gdx.assets.loaders.I18NBundleLoader
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.I18NBundle
import java.util.*


/**
 * Asset loader for a markdown file (.md).
 * The loader takes a base file handle parameter and appends a locale string to it.
 * This file was heavily inspired from [I18NBundle] and [I18NBundleLoader].
 */
class MdLoader(resolver: FileHandleResolver) :
        AsynchronousAssetLoader<Markdown, MdLoader.Parameter>(resolver) {

    private var markdown: Markdown? = null

    override fun getDependencies(fileName: String, file: FileHandle, parameter: Parameter?) = null

    override fun loadAsync(manager: AssetManager, fileName: String, file: FileHandle, parameter: Parameter?) {
        val param = parameter ?: Parameter()
        markdown = Markdown(getLocalizedFileHandle(file, param.locale), param.encoding)
    }

    override fun loadSync(manager: AssetManager, fileName: String, file: FileHandle, parameter: Parameter?): Markdown {
        val markdown = this.markdown
        this.markdown = null
        return markdown!!
    }

    /**
     * Get the localized file handle from a [baseFile] and a [locale].
     * @see I18NBundle.toFileHandle
     */
    private fun getLocalizedFileHandle(baseFile: FileHandle, locale: Locale): FileHandle {
        val sb = StringBuilder(baseFile.name())

        val files = mutableListOf<FileHandle>(baseFile.sibling("$sb.md"))

        val language = locale.language
        if (language.isNotEmpty()) {
            sb.append("_")
            sb.append(language)
            files += baseFile.sibling("$sb.md")
        }

        val country = locale.country
        if (country.isNotEmpty()) {
            sb.append("_")
            sb.append(country)
            files += baseFile.sibling("$sb.md")
        }

        val variant = locale.variant
        if (variant.isNotEmpty()) {
            sb.append("_")
            sb.append(variant)
            files += baseFile.sibling("$sb.md")
        }

        // Use most locale specific file available
        files.reverse()
        for (file in files) {
            if (fileExists(file)) {
                return file
            }
        }
        error("Can't find markdown file for '$baseFile'")
    }

    /** @see I18NBundle.checkFileExistence */
    private fun fileExists(fh: FileHandle) = try {
        fh.read().close()
        true
    } catch (e: Exception) {
        false
    }

    /**
     * Parameters for the markdown loader.
     * @property locale Markdown file locale, default is [Locale.getDefault].
     * @property encoding File encoding, default is UTF-8.
     */
    class Parameter(var locale: Locale = Locale.getDefault(),
                    val encoding: String = "UTF-8") : AssetLoaderParameters<Markdown>()

}