pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev")
            content {
                includeGroupByRegex("org\\.jetbrains\\..*")
            }
        }
    }
}

rootProject.name = "amsakkarrar"
include(":shared")
include(":androidApp")
