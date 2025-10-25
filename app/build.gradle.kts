// build.gradle.kts (Nivel de módulo: app)

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.plugin.compose") // ✅ ya correcto
    id("com.google.gms.google-services")
    id("kotlin-kapt")
}

android {
    namespace = "com.caycedo.registroganado"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.caycedo.registroganado"
        minSdk = 26 // 🔄 lo bajamos a 24 para mayor compatibilidad (Apache POI y Firebase funcionan bien)
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

    // ✅ PASO 1: Opciones de compilación modernas
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true // requerido para Apache POI
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += "-Xjvm-default=all"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.13" // ✅ versión estable actual
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // ✅ Librerías base
    implementation(libs.foundation)
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.androidx.foundation)

    // ✅ Soporte de funciones Java 8+ (para Apache POI, LocalDate, etc.)
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    // ✅ Apache POI (Excel)
    implementation("org.apache.poi:poi-ooxml:5.2.5")

    // Librería principal de Vico para Compose con Material 3
    implementation("com.patrykandpatrick.vico:compose-m3:1.14.0")




    // (Opcional) ByteBeats — si aún usas su ejemplo base en algún archivo:
    implementation("io.github.bytebeats:compose-charts:0.2.1")

    // ✅ CSV Reader
    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.9.3")

    // ✅ Firebase (BOM = sincroniza versiones automáticamente)
    implementation(platform(libs.firebase.bom))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")

    // ✅ Corrutinas y Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.2")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // ✅ Imágenes (Coil)
    implementation("io.coil-kt:coil-compose:2.6.0")

    // ✅ Material 3 y Jetpack Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui.preview)
    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.activity.compose)
    implementation(libs.navigation.compose)
    implementation("androidx.compose.material:material-icons-extended")
}







