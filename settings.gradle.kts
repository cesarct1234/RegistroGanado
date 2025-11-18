pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()   // ✔ necesario para co.yml:charts
        // JitPack solo si usas librerías de GitHub
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "RegistroGanado"
include(":app")

