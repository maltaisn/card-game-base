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

package com.maltaisn.cardgame.stats

import com.badlogic.gdx.assets.AssetDescriptor
import com.badlogic.gdx.assets.AssetLoaderParameters
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader
import com.badlogic.gdx.assets.loaders.FileHandleResolver
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.I18NBundle
import ktx.collections.GdxArray


/**
 * Asset loader for statistics defined by JSON.
 * The loader expects to find a strings bundle file with the same name as the statistics file,
 * unless otherwise specified by the loader parameter.
 */
class StatsLoader(resolver: FileHandleResolver) :
        AsynchronousAssetLoader<Statistics, StatsLoader.Parameter>(resolver) {

    private var stats: Statistics? = null

    override fun getDependencies(fileName: String, file: FileHandle, parameter: Parameter?): Array<AssetDescriptor<*>>? {
        // Add a dependency to the strings bundle for string references
        val dependencies = GdxArray<AssetDescriptor<*>>()
        dependencies.add(AssetDescriptor(getBundleName(file, parameter), I18NBundle::class.java))
        return dependencies
    }

    override fun loadAsync(manager: AssetManager, fileName: String, file: FileHandle, parameter: Parameter?) {
        val bundle: I18NBundle = manager.get(getBundleName(file, parameter))
        stats = Statistics(file, bundle)
    }

    override fun loadSync(manager: AssetManager, fileName: String, file: FileHandle, parameter: Parameter?): Statistics {
        val stats = this.stats
        this.stats = null
        return stats!!
    }

    private fun getBundleName(file: FileHandle, parameter: Parameter?) =
            parameter?.bundlePath ?: file.pathWithoutExtension()

    /**
     * Parameters for the statistics loader.
     * @property bundlePath The path of the strings bundle used to resolve string references. (without .properties).
     * By default a bundle with the same name as the statistics file is used.
     */
    class Parameter(var bundlePath: String? = null) : AssetLoaderParameters<Statistics>()

}
