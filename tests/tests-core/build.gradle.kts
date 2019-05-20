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

plugins {
    kotlin("jvm")
}

sourceSets {
    main {
        java.srcDir("src/main/kotlin")
    }
}

dependencies {
    val gdxVersion: String by project
    val ktxVersion: String by project
    val junitVersion: String by project

    api(project(":app"))

    implementation(kotlin("stdlib"))

    implementation("com.badlogicgames.gdx:gdx:$gdxVersion")

    implementation("io.github.libktx:ktx-actors:$ktxVersion")
    implementation("io.github.libktx:ktx-assets:$ktxVersion")
    implementation("io.github.libktx:ktx-collections:$ktxVersion")
    implementation("io.github.libktx:ktx-math:$ktxVersion")
    implementation("io.github.libktx:ktx-log:$ktxVersion")
    implementation("io.github.libktx:ktx-style:$ktxVersion")

    compileOnly("com.gmail.blueboxware:libgdxpluginannotations:1.16")

    testImplementation("junit:junit:$junitVersion")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_6
    targetCompatibility = JavaVersion.VERSION_1_6
}