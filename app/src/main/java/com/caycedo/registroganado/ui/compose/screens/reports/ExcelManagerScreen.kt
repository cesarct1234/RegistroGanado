package com.caycedo.registroganado.ui.compose.screens.reports

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.caycedo.registroganado.ui.compose.screens.utils.ExcelUtility

import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExcelManagerScreen(navController: NavController) {

    val context = LocalContext.current

    var statusMessage by remember { mutableStateOf("") }

    var showDialog by remember { mutableStateOf(false) }
    var generatedFile by remember { mutableStateOf<File?>(null) }

    // 👉 launcher para importar archivo
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data

            if (uri != null) {
                val ok = ExcelUtility.importExcelFromUri(context, uri)

                statusMessage = if (ok)
                    "Archivo importado correctamente"
                else
                    "Error al importar el archivo"
            }


        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Excel", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFA8D89F))
            )
        }
    ) { pad ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // IMPORTAR
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_GET_CONTENT)
                    intent.type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    importLauncher.launch(intent)
                },
                modifier = Modifier.fillMaxWidth().height(55.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
            ) {
                Icon(Icons.Default.FileUpload, null)
                Spacer(Modifier.width(10.dp))
                Text("Importar Excel")
            }

            // EXPORTAR
            Button(
                onClick = {
                    ExcelUtility.exportExcel(context) { file ->
                        if (file != null) {
                            generatedFile = file
                            showDialog = true
                            statusMessage = "Archivo exportado correctamente"
                        } else {
                            statusMessage = "Error al generar archivo"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(55.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1))
            ) {
                Icon(Icons.Default.FileDownload, null)
                Spacer(Modifier.width(10.dp))
                Text("Exportar Excel")
            }

            // MENSAJE
            if (statusMessage.isNotEmpty()) {
                Text(
                    text = statusMessage,
                    color = Color(0xFF1B5E20)
                )
            }
        }
    }

    // =====================================================================================
    // DIÁLOGO PARA COMPARTIR / WHATSAPP / CORREO / ABRIR
    // =====================================================================================

    if (showDialog && generatedFile != null) {

        AlertDialog(
            onDismissRequest = { showDialog = false },

            title = { Text("Archivo generado") },

            text = { Text("¿Qué deseas hacer con el archivo?") },

            confirmButton = {
                TextButton(
                    onClick = {
                        ExcelUtility.enviarWhatsApp(context, generatedFile!!)
                    }
                ) { Text("WhatsApp") }
            },

            dismissButton = {

                Column(horizontalAlignment = Alignment.End) {

                    TextButton(
                        onClick = {
                            ExcelUtility.enviarCorreo(context, generatedFile!!)
                        }
                    ) { Text("Correo") }

                    TextButton(
                        onClick = {
                            ExcelUtility.abrirArchivo(context, generatedFile!!)
                        }
                    ) { Text("Abrir") }

                    TextButton(
                        onClick = {
                            showDialog = false
                        }
                    ) { Text("Cerrar") }
                }
            }
        )
    }
}


