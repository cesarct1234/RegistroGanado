package com.caycedo.registroganado.ui.compose.screens.reports

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.InputStream
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Description



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExcelImportScreen(navController: NavController) {

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var fileName by remember { mutableStateOf("Ningún archivo seleccionado") }
    var isLoading by remember { mutableStateOf(false) }

    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            fileName = uri.lastPathSegment ?: "archivo.xlsx"

            isLoading = true
            scope.launch {
                val result = leerExcel(context, uri)
                isLoading = false

                snackbarHostState.showSnackbar(result)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Importar desde Excel") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { pad ->

        Column(
            modifier = Modifier
                .padding(pad)
                .padding(20.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                "Carga Masiva de Animales",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )

            Text(
                "Seleccione un archivo .xlsx con columnas: nombre, raza, sexo, peso, edad",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            // Caja de archivo seleccionado
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Text("Archivo: $fileName")
            }

            Button(
                onClick = {
                    filePicker.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Elegir archivo Excel")
            }

            Spacer(modifier = Modifier.height(30.dp))

            if (isLoading) {
                CircularProgressIndicator()
            }
        }
    }
}
suspend fun leerExcel(context: Context, uri: Uri): String {

    return try {
        val dbRef = FirebaseDatabase.getInstance().getReference("animales_global")

        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val workbook = XSSFWorkbook(inputStream)
        val sheet = workbook.getSheetAt(0)

        // Saltar encabezado
        for (i in 1..sheet.lastRowNum) {
            val row = sheet.getRow(i)

            if (row == null) continue

            val nombre = row.getCell(0)?.toString() ?: ""
            val raza = row.getCell(1)?.toString() ?: ""
            val sexo = row.getCell(2)?.toString() ?: ""
            val peso = row.getCell(3)?.toString() ?: ""
            val edad = row.getCell(4)?.toString() ?: ""

            if (nombre.isEmpty()) continue

            val id = dbRef.push().key!!

            val animal = mapOf(
                "id" to id,
                "nombre" to nombre,
                "raza" to raza,
                "sexo" to sexo,
                "peso" to peso,
                "edad" to edad
            )

            dbRef.child(id).setValue(animal)
        }

        workbook.close()
        inputStream?.close()

        "Importación completada correctamente"

    } catch (e: Exception) {
        e.printStackTrace()
        "Error leyendo el archivo: ${e.message}"
    }
}
