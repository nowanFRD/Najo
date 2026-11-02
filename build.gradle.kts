import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.4.20"
    kotlin("plugin.serialization") version "2.4.20"
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.20"
    id("org.jetbrains.compose") version "1.12.2"
}

group = "com.najo"
version = "1.0.0"

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
    sourceSets["main"].kotlin.srcDir("desktop/src/main/kotlin")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.10.2")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:6.1.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:6.1.3")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "failed", "skipped")
    }
}

compose.desktop {
    application {
        mainClass = "com.najo.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Deb, TargetFormat.AppImage)
            packageName = "najo"
            packageVersion = "1.0.0"
            description = "Najo - a calm day planner"
            vendor = "Najo"
            linux {
                iconFile.set(project.file("src/main/resources/icon.png"))
                menuGroup = "Utility"
            }
        }
    }
}
