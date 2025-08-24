plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.17.3"
    kotlin("jvm") version "1.9.10"
}

group = "io.github.meteulken"
version = "1.0.0"

repositories {
    mavenCentral()
}

intellij {
    version.set("2023.3")
    type.set("IC")
    plugins.set(listOf())
    pluginName.set("CommitHelper")
}

dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.json:json:20240303")
}

kotlin {
    jvmToolchain(17)
}

tasks {
    patchPluginXml {
        sinceBuild.set("233")
        untilBuild.set("")
    }

    buildSearchableOptions {
        enabled = false
    }
}
