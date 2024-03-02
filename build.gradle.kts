buildscript {
    val kotlinVersion: String by project
    val androidGradlePluginVersion: String by project
    repositories {
        gradlePluginPortal()
        google()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:$androidGradlePluginVersion")
        classpath(kotlin("gradle-plugin", kotlinVersion))
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:1.9.10")
    }
}

plugins {
    base
}

tasks.clean {
    delete(rootProject.layout.buildDirectory)
}

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        google()
    }
}
