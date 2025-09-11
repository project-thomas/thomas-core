rootProject.name = "thomas-core"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
        maven {
            url = uri("https://repo.repsy.io/mvn/${System.getenv("REPSY_USERNAME")}/thomas-release")
            credentials {
                username = System.getenv("REPSY_USERNAME")
                password = System.getenv("REPSY_PASSWORD")
            }
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        mavenLocal()
        maven {
            url = uri("https://repo.repsy.io/mvn/${System.getenv("REPSY_USERNAME")}/thomas-release")
            credentials {
                username = System.getenv("REPSY_USERNAME")
                password = System.getenv("REPSY_PASSWORD")
            }
        }
    }
}

buildCache {
    local {
        isEnabled = true
        directory = File(rootDir, "build-cache")
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
