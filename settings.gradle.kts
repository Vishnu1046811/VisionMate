pluginManagement {
    repositories {
        google()
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven{ url = uri("https://jitpack.io") }
    }
}
dependencyResolutionManagement {
    repositories {
        maven { url =uri("https://kommunicate.jfrog.io/artifactory/kommunicate-android-sdk") }
    }
}

rootProject.name = "VisionMate"
include(":app",":models")
 