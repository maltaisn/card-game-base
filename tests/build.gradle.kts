plugins {
    kotlin("jvm")
}

// Tasks to copy the card game assets to the tests module assets dir
tasks.register("copyAssets") {
    file("assets").mkdirs()
    copy {
        from("../assets")
        into("assets")
    }
}

tasks.register<Delete>("cleanAssets") {
    delete("assets/core")
}

tasks.named("clean") {
    finalizedBy("cleanAssets")
}

tasks.named("build") {
    finalizedBy("copyAssets")
}