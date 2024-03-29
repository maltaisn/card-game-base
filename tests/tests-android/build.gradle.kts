plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.maltaisn.cardgame.tests.android"

    buildToolsVersion = "34.0.0"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.maltaisn.cardgame.tests.android"
        minSdk = 16
        targetSdk = 34
        versionCode = 1
        versionName = "0.0.1"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    sourceSets {
        named("main") {
            java.srcDir("src/main/kotlin")  // Not necessary but works better with IntelliJ
        }
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

val natives: Configuration by configurations.creating

dependencies {
    val gdxVersion: String by project

    implementation(project(":tests:tests-core"))

    implementation(kotlin("stdlib-jdk8"))

    api("com.badlogicgames.gdx:gdx-backend-android:$gdxVersion")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    natives("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-armeabi-v7a")
    natives("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-arm64-v8a")
    natives("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-x86")
    natives("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-x86_64")
}

// Called every time gradle gets executed, takes the native dependencies of
// the natives configuration, and extracts them to the proper libs/ folders
// so they get packed with the APK.
tasks.register("copyAndroidNatives") {
    doFirst {
        val jniLibsPath = android.sourceSets.named("main").get().jniLibs.srcDirs.last().path
        natives.files.forEach { jar ->
            val nativeName = jar.nameWithoutExtension.substringAfterLast("natives-")
            val outputDir = File(jniLibsPath, nativeName)
            outputDir.mkdirs()
            copy {
                from(zipTree(jar))
                into(outputDir)
                include("*.so")
            }
        }
    }
}
tasks.whenTaskAdded {
    if ("package" in name) {
        dependsOn("copyAndroidNatives")
    }
}

// Tasks to copy the tests assets to the android module assets dir
val assetsPath: String = android.sourceSets.named("main").get().assets.srcDirs.last().path

tasks.register("copyTestAssets") {
    file(assetsPath).mkdirs()
    copy {
        from("../assets")
        into(assetsPath)
    }
}

tasks.register<Delete>("cleanTestAssets") {
    delete(assetsPath)
}

tasks.named("clean") {
    finalizedBy("cleanTestAssets")
}

tasks.named("build") {
    finalizedBy("copyTestAssets")
}
