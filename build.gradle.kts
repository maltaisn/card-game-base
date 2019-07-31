buildscript {
    val cardGameVersion by extra("0.0.1")
    val pcardVersion by extra("0.0.1")

    val kotlinVersion by extra("1.3.41")
    val gdxVersion by extra("1.9.10")
    val ktxVersion by extra("1.9.10-SNAPSHOT")
    val junitVersion by extra("4.12")

    repositories {
        gradlePluginPortal()
        google()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:3.4.2")
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
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        jcenter()
        google()
    }
}
