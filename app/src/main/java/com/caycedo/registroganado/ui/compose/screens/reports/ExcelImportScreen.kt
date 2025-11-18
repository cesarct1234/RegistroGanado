package com.caycedo.registroganado.ui.compose.screens.reports

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
//import com.caycedo.registroganado.ui.compose.screens.utils.leerExcel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExcelImportScreen(navController: NavController) {

    val context = LocalContext.current

    var fileName by remember { mutableStateOf("Ningún archivo seleccionado") }

    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            fileName = uri.lastPathSegment ?: "archivo.xlsx"
            leerExcel(context, uri)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Importar Animales desde Excel") }
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

            Text("Seleccione un archivo .xlsx con los datos de animales")

            Button(
                onClick = { filePicker.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Elegir archivo Excel")
            }

            Text("Archivo seleccionado: $fileName")
        }
    }
}

fun leerExcel(context: Context, uri: Uri) {}

