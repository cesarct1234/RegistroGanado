package com.caycedo.registroganado.ui.compose.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import org.apache.poi.ss.usermodel.WorkbookFactory

// 🐮 Modelo de datos
data class Animal(
    val id: String = "",
    val nombre: String = "",
    val raza: String = "",
    val sexo: String = "",
    val nacimiento: String = "",
    val edad: String = "",
    val peso: String = "",
    val estadoReproductivo: String = "",
    val ultimoParto: String = "",
    val produccionLeche: String = "",
    val vacunas: String = "",
    val tratamientos: String = "",
    val observaciones: String = ""
)


// 📥 Función para leer archivo Excel (.xlsx)
fun leerCsv(context: Context, uri: Uri) {
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: return
    val database = FirebaseDatabase.getInstance().getReference("animales").child(userId)

    try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val workbook = WorkbookFactory.create(inputStream)
            val sheet = workbook.getSheetAt(0)

            // 🧾 Recorremos las filas (saltando encabezado)
            for (i in 1..sheet.lastRowNum) {
                val row = sheet.getRow(i) ?: continue
                val id = row.getCell(0)?.toString()?.trim() ?: ""
                val nombre = row.getCell(1)?.toString()?.trim() ?: ""
                val raza = row.getCell(2)?.toString()?.trim() ?: ""
                val sexo = row.getCell(3)?.toString()?.trim() ?: ""
                val nacimiento = row.getCell(4)?.toString()?.trim() ?: ""
                val edad = row.getCell(5)?.toString()?.trim() ?: ""
                val peso = row.getCell(6)?.toString()?.trim() ?: ""
                val estadoReproductivo = row.getCell(7)?.toString()?.trim() ?: ""
                val ultimoParto = row.getCell(8)?.toString()?.trim() ?: ""
                val produccionLeche = row.getCell(9)?.toString()?.trim() ?: ""
                val vacunas = row.getCell(10)?.toString()?.trim() ?: ""
                val tratamientos = row.getCell(11)?.toString()?.trim() ?: ""
                val observaciones = row.getCell(12)?.toString()?.trim() ?: ""

                if (nombre.isNotEmpty()) {
                    val animalId = database.push().key ?: continue
                    val animal = Animal(
                        id = if (id.isEmpty()) animalId else id,
                        nombre = nombre,
                        raza = raza,
                        sexo = sexo,
                        nacimiento = nacimiento,
                        edad = edad,
                        peso = peso,
                        estadoReproductivo = estadoReproductivo,
                        ultimoParto = ultimoParto,
                        produccionLeche = produccionLeche,
                        vacunas = vacunas,
                        tratamientos = tratamientos,
                        observaciones = observaciones
                    )
                    database.child(animalId).setValue(animal)
                }
            }

            workbook.close()
            Toast.makeText(context, "✅ Importación desde Excel completada", Toast.LENGTH_LONG).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "❌ Error al leer Excel: ${e.message}", Toast.LENGTH_LONG).show()
        e.printStackTrace()
    }
}


// 📋 Pantalla principal de lista de animales
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimalListScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: return
    val database = FirebaseDatabase.getInstance().getReference("animales").child(userId)

    var listaAnimales by remember { mutableStateOf(listOf<Animal>()) }

    // 🔄 Escucha cambios en Firebase en tiempo real
    LaunchedEffect(Unit) {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val animales = mutableListOf<Animal>()
                for (animalSnapshot in snapshot.children) {
                    val animal = animalSnapshot.getValue(Animal::class.java)
                    if (animal != null) {
                        animales.add(animal)
                    }
                }
                listaAnimales = animales
            }

            override fun onCancelled(error: DatabaseError) {
                println("Error al cargar animales: ${error.message}")
            }
        }
        database.addValueEventListener(listener)
    }

    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri -> uri?.let { leerCsv(context, it) } }
    )

    // 🧱 UI general
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Animales") },
                actions = {
                    // 📂 Importar Excel
                    IconButton(onClick = {
                        launcher.launch(arrayOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    }) {
                        Icon(Icons.Default.FileUpload, contentDescription = "Importar Excel")
                    }

                    // ➕ Agregar manualmente
                    IconButton(onClick = { navController.navigate("addAnimal") }) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar Animal")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            if (listaAnimales.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay animales registrados aún 🐄", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listaAnimales) { animal ->
                        AnimalCard(animal)
                    }
                }
            }
        }
    }
}


// 🎴 Tarjeta individual de animal
@Composable
fun AnimalCard(animal: Animal) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "🐮 ${animal.nombre}", fontWeight = FontWeight.Bold)
            Text(text = "Raza: ${animal.raza}")
            Text(text = "Sexo: ${animal.sexo}")
            Text(text = "Peso: ${animal.peso} kg")
            Text(text = "Producción leche: ${animal.produccionLeche} L/día")
        }
    }
}



