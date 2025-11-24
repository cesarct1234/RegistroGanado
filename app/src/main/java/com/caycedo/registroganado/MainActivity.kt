package com.caycedo.registroganado

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import com.caycedo.registroganado.ui.compose.nav.AppNavHost
import com.caycedo.registroganado.ui.theme.RegistroGanadoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // -------------------------------------------------
        // SOLICITAR PERMISOS PARA GUARDAR EN DESCARGAS
        // -------------------------------------------------
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            // Solo Android 9 o menor requiere estos permisos
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                100
            )
        }
        // Android 10+ no requiere permisos si usas getExternalFilesDir,
        // pero como quieres guardar en DESCARGAS, te pongo la versión correcta
        else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                101
            )
        }

        setContent {
            RegistroGanadoTheme {
                AppNavHost() // Navegación completa
            }
        }
    }
}


