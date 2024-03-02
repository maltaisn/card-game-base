plugins {
    kotlin("jvm")
}

val assetsDir = file("../assets")
val mainClassName = "com.maltaisn.cardgame.tests.desktop.DesktopLauncher"

dependencies {
    val gdxVersion: String by project

    implementation(project(":tests:tests-core"))

    implementation(kotlin("stdlib-jdk8"))

    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:$gdxVersion")
    implementation("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-desktop")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(8)
    }
}

tasks.register<JavaExec>("run") {
    dependsOn(tasks.build)
    mainClass = mainClassName
    classpath = sourceSets.main.get().runtimeClasspath
    standardInput = System.`in`
    workingDir = assetsDir
    isIgnoreExitValue = true
}

tasks.register<Jar>("dist") {
    from(files(sourceSets.main.get().output.classesDirs))
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    from(assetsDir)

    manifest {
        attributes["Main-Class"] = mainClassName
    }
}
