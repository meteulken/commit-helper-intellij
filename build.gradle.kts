plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.17.3"
    kotlin("jvm") version "1.9.10"
}

group = "io.github.meteulken"
version = "1.1.0"

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
        changeNotes.set("""
            <h3>v1.1.0</h3>
            <ul>
                <li><b>New:</b> Added support for <b>Ollama (Local)</b> AI provider.</li>
                <li><b>New:</b> Added support for <b>11 languages</b> (English, Turkish, Spanish, German, French, Italian, Portuguese, Russian, Chinese, Japanese, Korean).</li>
                <li><b>Improved:</b> Diff collection now accurately processes <b>only selected files</b> in the commit dialog.</li>
                <li><b>Optimized:</b> Reduced prompt size for faster and more cost-effective generation.</li>
            </ul>
        """.trimIndent())
    }

    buildSearchableOptions {
        enabled = false
    }
}
