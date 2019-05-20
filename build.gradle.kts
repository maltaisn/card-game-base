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

buildscript {
    val kotlinVersion by project.extra("1.3.31")
    val gdxVersion by project.extra("1.9.9")
    val ktxVersion by project.extra("1.9.9-b1")
    val junitVersion by project.extra("4.12")
    val cardGameVersion by project.extra("0.0.1")

    repositories {
        mavenCentral()
        maven("https://plugins.gradle.org/m2/")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        jcenter()
        google()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:3.4.1")
        classpath(kotlin("gradle-plugin", kotlinVersion))
    }
}

allprojects {
    repositories {
        mavenCentral()
        jcenter()
        google()
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://oss.sonatype.org/content/repositories/releases/")
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}

buildDir = file("build/")