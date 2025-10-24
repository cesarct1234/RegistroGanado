package com.caycedo.registroganado

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.caycedo.registroganado.ui.compose.nav.AppNavHost
import com.caycedo.registroganado.ui.theme.RegistroGanadoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RegistroGanadoTheme {
                AppNavHost() // ✅ Aquí se carga el grafo de navegación completo
            }
        }
    }
}



