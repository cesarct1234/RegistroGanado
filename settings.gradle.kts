pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        // 🟢 Agregado para asegurar compatibilidad con dependencias externas
        maven { url = uri("https://jitpack.io") }
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()  // 👈 Mantén esta línea
        // 🟢 Repositorio adicional para dependencias como charts, Firebase, etc.
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "RegistroGanado"
include(":app")

