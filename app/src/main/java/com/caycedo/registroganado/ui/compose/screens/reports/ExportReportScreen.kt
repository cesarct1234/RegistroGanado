package com.caycedo.registroganado.ui.compose.screens.reports

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
//import com.caycedo.registroganado.ui.compose.screens.utils.generarPDFReporteGanado

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportReportScreen(navController: NavController) {

    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Exportar Reporte General en PDF") }
            )
        }
    ) { pad ->

        Column(
            Modifier
                .padding(pad)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            Text("Generar un PDF con todos los animales registrados")

            Button(
                onClick = {
                    generarPDFReporteGanado(context)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Generar PDF")
            }
        }
    }
}

private fun ColumnScope.generarPDFReporteGanado(context: Context) {
        TODO("Not yet implemented")
}


