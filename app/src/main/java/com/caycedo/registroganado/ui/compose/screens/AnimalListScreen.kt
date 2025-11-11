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
import androidx.compose.material.icons.filled.*
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
import androidx.lifecycle.LiveData
import androidx.compose.runtime.livedata.observeAsState
import com.caycedo.registroganado.ui.compose.nav.NavRoutes

// 🐮 Modelo
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
    val observaciones: String = "",
    val tipoProduccion: String = "",
    val aptoConsumo: Boolean = false
)

// 📥 Importar datos desde Excel (.xlsx)
fun leerCsv(context: Context, uri: Uri) {
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: return
    val database = FirebaseDatabase.getInstance().getReference("animales").child(userId)

    try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val workbook = WorkbookFactory.create(inputStream)
            val sheet = workbook.getSheetAt(0)
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
                val tipoProduccion = row.getCell(13)?.toString()?.trim() ?: ""
                val aptoConsumoStr = row.getCell(14)?.toString()?.trim() ?: "false"
                val aptoConsumo =
                    aptoConsumoStr.equals("true", ignoreCase = true) || aptoConsumoStr == "1"

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
                        observaciones = observaciones,
                        tipoProduccion = tipoProduccion,
                        aptoConsumo = aptoConsumo
                    )
                    database.child(animalId).setValue(animal)
                }
            }
            workbook.close()
            Toast.makeText(context, "✅ Importación completada", Toast.LENGTH_LONG).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "❌ Error al leer Excel: ${e.message}", Toast.LENGTH_LONG).show()
        e.printStackTrace()
    }
}

// 📋 Lista de animales
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimalListScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: return
    val database = FirebaseDatabase.getInstance().getReference("animales").child(userId)
    var listaAnimales by remember { mutableStateOf(listOf<Animal>()) }

    // 🔄 Escucha en tiempo real
    LaunchedEffect(Unit) {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val animales = mutableListOf<Animal>()

                for (animalSnapshot in snapshot.children) {
                    // ✅ Verifica que el nodo sea un objeto antes de intentar convertirlo
                    val value = animalSnapshot.value
                    if (value is Map<*, *>) {
                        try {
                            val animal = animalSnapshot.getValue(Animal::class.java)
                            if (animal != null) animales.add(animal)
                        } catch (e: Exception) {
                            println("⚠️ Error al convertir nodo ${animalSnapshot.key}: ${e.message}")
                        }
                    } else {
                        // Solo muestra en consola, no crashea
                        println("⚠️ Nodo inválido (no es un objeto Animal): ${animalSnapshot.key} → ${animalSnapshot.value}")
                    }
                }

                listaAnimales = animales
            }

            override fun onCancelled(error: DatabaseError) {
                println("❌ Error al cargar animales: ${error.message}")
            }
        }

        database.addValueEventListener(listener)
    }


    val context = LocalContext.current

    // 📩 Detectar mensaje de actualización
    val updateMessageLiveData: LiveData<String>? =
        navController.currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData("update_message")

    val updateMessage = updateMessageLiveData?.observeAsState()
    LaunchedEffect(updateMessage?.value) {
        updateMessage?.value?.let { mensaje ->
            Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.remove<String>("update_message")
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri -> uri?.let { leerCsv(context, it) } }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Animales", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = {
                        launcher.launch(arrayOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    }) { Icon(Icons.Default.FileUpload, contentDescription = "Importar Excel") }

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
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay animales registrados aún 🐄")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(listaAnimales) { animal ->
                        AnimalCard(animal, database, navController)
                    }
                }
            }
        }
    }
}

// 🎴 Tarjeta del animal
@Composable
fun AnimalCard(animal: Animal, database: DatabaseReference, navController: NavController) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    // 🗑️ Diálogo de confirmación al eliminar
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Eliminar Animal") },
            text = { Text("¿Seguro que deseas eliminar a ${animal.nombre}? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    database.child(animal.id).removeValue()
                        .addOnSuccessListener {
                            Toast.makeText(context, "✅ ${animal.nombre} eliminado correctamente", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "❌ Error al eliminar", Toast.LENGTH_SHORT).show()
                        }
                    showDialog = false
                }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // 🐮 Tarjeta visual del animal
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // 🔹 Encabezado con nombre y acciones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🐮 ${animal.nombre}", fontWeight = FontWeight.Bold)

                Row {
                    // ✏️ Botón editar → abre AddAnimalScreen en modo edición
                    IconButton(onClick = {
                        navController.navigate("${NavRoutes.ADD_ANIMAL}/${animal.id}")
                    }) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // 🗑️ Botón eliminar → abre diálogo
                    IconButton(onClick = { showDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            // 🔹 Información general del animal
            Text("Raza: ${animal.raza}")
            Text("Sexo: ${animal.sexo}")
            Text("Tipo de Producción: ${animal.tipoProduccion.ifEmpty { "No especificado" }}")
            Text("Peso: ${animal.peso.ifEmpty { "0" }} kg")
            Text("Producción de leche: ${animal.produccionLeche.ifEmpty { "N/A" }} L/día")
            Text("Apto para consumo: ${if (animal.aptoConsumo) "✅ Sí" else "❌ No"}")
        }
    }
}



