buildscript {
    val kotlinVersion by extra("1.3.31")
    val gdxVersion by extra("1.9.9")
    val ktxVersion by extra("1.9.9-b1")
    val junitVersion by extra("4.12")
    val cardGameVersion by extra("0.0.1")

    repositories {
        gradlePluginPortal()
        google()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:3.4.1")
        classpath(kotlin("gradle-plugin", kotlinVersion))
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:0.9.18")
    }
}

plugins {
    base
}

allprojects {
    repositories {
        mavenCentral()
        jcenter()
        google()
    }
}