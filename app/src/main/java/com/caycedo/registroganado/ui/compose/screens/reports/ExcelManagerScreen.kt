package com.caycedo.registroganado.ui.compose.screens.reports

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FileDownload
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
import com.caycedo.registroganado.ui.compose.screens.utils.ShareUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExcelManagerScreen(navController: NavController) {

    val context = LocalContext.current
    var statusMessage by remember { mutableStateOf("") }

    // 👉 Selector de archivo para IMPORTAR
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data

            if (uri != null) {
                val success = ExcelUtility.importExcelFromUri(context, uri)

                statusMessage = if (success)
                    "✅ Archivo importado correctamente"
                else
                    "❌ Error al importar el archivo"
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Excel", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFA8D89F)
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2E7D32)
                )
            ) {
                Icon(Icons.Default.FileUpload, null)
                Spacer(Modifier.width(10.dp))
                Text("Importar Excel")
            }

            // EXPORTAR
            Button(
                onClick = {
                    ExcelUtility.exportExcel(context) { filePath ->
                        if (filePath != null) {
                            ShareUtils.shareFile(context, filePath)
                            statusMessage = "📤 Archivo exportado y listo para compartir"
                        } else {
                            statusMessage = "❌ Error al generar el archivo"
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0288D1)
                )
            ) {
                Icon(Icons.Default.FileDownload, null)
                Spacer(Modifier.width(10.dp))
                Text("Exportar Excel")
            }

            // MENSAJE
            if (statusMessage.isNotEmpty()) {
                Text(
                    text = statusMessage,
                    color = Color(0xFF1B5E20),
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    }
}

private fun ShareUtils.shareFile(context: android.content.Context, filePath: String) {}
