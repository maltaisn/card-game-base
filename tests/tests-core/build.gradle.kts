plugins {
    kotlin("jvm")
}

dependencies {
    val gdxVersion: String by project
    val ktxVersion: String by project
    val junitVersion: String by project

    api(project(":core"))
    api(project(":pcard"))

    implementation(kotlin("stdlib"))

    implementation("com.badlogicgames.gdx:gdx:$gdxVersion")

    implementation("io.github.libktx:ktx-actors:$ktxVersion")
    implementation("io.github.libktx:ktx-assets:$ktxVersion")
    implementation("io.github.libktx:ktx-collections:$ktxVersion")
    implementation("io.github.libktx:ktx-math:$ktxVersion")
    implementation("io.github.libktx:ktx-log:$ktxVersion")
    implementation("io.github.libktx:ktx-style:$ktxVersion")

    testImplementation("junit:junit:$junitVersion")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_6
    targetCompatibility = JavaVersion.VERSION_1_6
}

// Tasks to copy the card game assets to the tests module assets dir
tasks.register("copyAssets") {
    file("../assets").mkdirs()
    copy {
        from("../../assets")
        into("../assets")
    }
}

tasks.register<Delete>("cleanAssets") {
    delete("../assets/core")
    delete("../assets/pcard")
}

tasks.clean {
    finalizedBy("cleanAssets")
}

tasks.build {
    finalizedBy("copyAssets")
}
