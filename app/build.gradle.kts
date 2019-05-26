import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    kotlin("jvm")
    `maven-publish`
    id("org.jetbrains.dokka")
}

dependencies {
    val gdxVersion: String by project
    val ktxVersion: String by project
    val junitVersion: String by project

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

sourceSets {
    main {
        java.srcDir("src/main/kotlin")
        // Assets folder is not specified to prevent bundling it in jar
    }
    test {
        java.srcDir("src/test/kotlin")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_6
    targetCompatibility = JavaVersion.VERSION_1_6
}

// Maven publishing
tasks.register<Jar>("sourcesJar") {
    dependsOn(tasks.classes)
    from(sourceSets.main.get().allSource)
    archiveClassifier.set("sources")
}

tasks.register<Jar>("javadocJar") {
    dependsOn(tasks.javadoc, tasks["dokkaJavadoc"])
    from(tasks.javadoc.get().destinationDir!!.path)
    archiveClassifier.set("javadoc")
}

tasks.create<DokkaTask>("dokkaJavadoc") {
    outputFormat = "html"  // Use "javadoc" for standard style
    outputDirectory = tasks.javadoc.get().destinationDir!!.path
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.maltaisn"
            artifactId = "cardgame"
            version = rootProject.extra["cardGameVersion"] as String
            pom {
                name.set("Card game")
                description.set("Card game base application")
                url.set("https://github.com/maltaisn/cardgame")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("maltaisn")
                    }
                }
            }
            from(components["java"])
            artifact(tasks["sourcesJar"])
            //artifact(tasks["javadocJar"])
        }
    }
}