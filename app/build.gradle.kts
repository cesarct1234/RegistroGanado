// build.gradle.kts (Nivel de Módulo: app)

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // El plugin de compose ya no necesita 'alias' si usas la versión del catalogo
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.gms.google-services")
    id("kotlin-kapt")
}

android {
    namespace = "com.caycedo.registroganado"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.caycedo.registroganado"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // ✅ PASO 1: Opciones de compilación corregidas
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8 // Usar 1.8 es más seguro para compatibilidad
        targetCompatibility = JavaVersion.VERSION_1_8
        // Habilitar la "desugarización" para que Apache POI funcione
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "1.8" // Debe coincidir con `compileOptions`
    }

    buildFeatures {
        compose = true
    }

    // Es importante que el bloque packagingOptions esté aquí para evitar conflictos
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.foundation)
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)
    // ✅ PASO 2: Dependencia para la "desugarización"
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    // ✅ Apache POI para leer archivos Excel (.xlsx)
    kotlin
    implementation("org.apache.poi:poi-ooxml:5.2.5")


    // ✅ Librería para leer archivos CSV
    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.9.3")

    // ✅ Firebase (usando el BOM para gestionar versiones)
    implementation(platform(libs.firebase.bom))
    implementation("com.google.firebase:firebase-auth") // quitamos -ktx, es implícito
    implementation("com.google.firebase:firebase-database") // quitamos -ktx, es implícito

    // ✅ Corrutinas / Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.2") // Versión estable
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.2") // Versión estable
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // ✅ Imágenes (Coil)
    implementation("io.coil-kt:coil-compose:2.6.0") // Usar `coil-compose` para Jetpack Compose

    // ✅ Material 3 y Jetpack Compose (usando el BOM)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui.preview)
    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.activity.compose)
    implementation(libs.navigation.compose)
    implementation("androidx.compose.material:material-icons-extended") // No necesita versión con el BOM






}






