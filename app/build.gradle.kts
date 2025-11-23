plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += "-Xjvm-default=all"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.ui.text)
    implementation(libs.androidx.ui.text)
    // --- DESUGAR (Java 8+ para POI Excel) ---
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    // --- Compose / Material 3 (USO EXCLUSIVO DEL BOM — NO DUPLICAR) ---
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui.preview)
    implementation(libs.activity.compose)
    implementation(libs.navigation.compose)
    debugImplementation(libs.compose.ui.tooling)


    // --- Foundation moderno (del catalogo) ---
    implementation(libs.foundation)

    // --- Íconos extendidos (opcional / estable) ---
    implementation("androidx.compose.material:material-icons-extended")

    // --- Firebase ---
    implementation(platform(libs.firebase.bom))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")

    // --- Coil imágenes ---
    implementation("io.coil-kt:coil-compose:2.6.0")

    // --- YML charts (gráficos) ---

    implementation("co.yml:ycharts:2.1.0")



    // --- CSV ---
    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.9.3")

    // --- Excel (Apache POI) ---
    implementation("org.apache.poi:poi-ooxml:5.2.5")

    // --- PDF ---
    implementation("com.itextpdf:itextg:5.5.10")

    // --- Corrutinas & Lifecycle ---
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.2")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // --- LiveData + Compose ---
    implementation("androidx.compose.runtime:runtime-livedata")
}



