rootProject.name = "najo"

pluginManagement {
    repositories {
        // Google's Maven Central mirror is used first because it is fast and
        // reliable on this network. Add/remove mirrors to taste.
        maven("https://maven-central.storage-download.googleapis.com/maven2/")
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        maven("https://maven-central.storage-download.googleapis.com/maven2/")
        google()
        mavenCentral()
    }
}
