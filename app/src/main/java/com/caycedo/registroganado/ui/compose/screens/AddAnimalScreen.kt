package com.caycedo.registroganado.ui_compose.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.caycedo.registroganado.ui.compose.screens.Animal
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAnimalScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: return
    val database = FirebaseDatabase.getInstance().getReference("animales").child(userId)

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Campos del formulario
    var nombre by remember { mutableStateOf("") }
    var raza by remember { mutableStateOf("") }
    var sexo by remember { mutableStateOf("") }
    var nacimiento by remember { mutableStateOf("") }
    var edad by remember { mutableStateOf("") }
    var peso by remember { mutableStateOf("") }
    var estadoReproductivo by remember { mutableStateOf("") }
    var ultimoParto by remember { mutableStateOf("") }
    var produccionLeche by remember { mutableStateOf("") }
    var vacunas by remember { mutableStateOf("") }
    var tratamientos by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }

    var isSaving by remember { mutableStateOf(false) }

    val opcionesSexo = listOf("Macho", "Hembra")
    var expanded by remember { mutableStateOf(false) }

    // 🗓️ Función para abrir selector de fecha
    fun abrirDatePicker(onDateSelected: (String) -> Unit) {
        val calendario = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            context,
            { _, year, month, day ->
                val fecha = "%02d/%02d/%04d".format(day, month + 1, year)
                onDateSelected(fecha)
            },
            calendario.get(Calendar.YEAR),
            calendario.get(Calendar.MONTH),
            calendario.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Registrar Animal", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->

        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre del animal") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = raza,
                onValueChange = { raza = it },
                label = { Text("Raza") },
                modifier = Modifier.fillMaxWidth()
            )

            // ⚧️ Selección de sexo
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = sexo,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Sexo (M/H)") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    opcionesSexo.forEach { opcion ->
                        DropdownMenuItem(
                            text = { Text(opcion) },
                            onClick = {
                                sexo = opcion
                                expanded = false
                            }
                        )
                    }
                }
            }

            // 📅 Fecha de nacimiento
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { abrirDatePicker { nacimiento = it } }
            ) {
                OutlinedTextField(
                    value = nacimiento,
                    onValueChange = {},
                    label = { Text("Fecha de nacimiento (dd/mm/aaaa)") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            OutlinedTextField(
                value = edad,
                onValueChange = { edad = it },
                label = { Text("Edad (años)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = peso,
                onValueChange = { peso = it },
                label = { Text("Peso (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = estadoReproductivo,
                onValueChange = { estadoReproductivo = it },
                label = { Text("Estado Reproductivo") },
                modifier = Modifier.fillMaxWidth()
            )

            // 🗓️ Último parto
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { abrirDatePicker { ultimoParto = it } }
            ) {
                OutlinedTextField(
                    value = ultimoParto,
                    onValueChange = {},
                    label = { Text("Último parto (dd/mm/aaaa)") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            OutlinedTextField(
                value = produccionLeche,
                onValueChange = { produccionLeche = it },
                label = { Text("Producción de leche (L/día)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = vacunas,
                onValueChange = { vacunas = it },
                label = { Text("Vacunas") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = tratamientos,
                onValueChange = { tratamientos = it },
                label = { Text("Tratamientos") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = observaciones,
                onValueChange = { observaciones = it },
                label = { Text("Observaciones") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (nombre.isNotEmpty() && raza.isNotEmpty() && sexo.isNotEmpty()) {
                        isSaving = true
                        val id = database.push().key ?: return@Button
                        val nuevoAnimal = Animal(
                            id = id,
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
                        database.child(id).setValue(nuevoAnimal).addOnCompleteListener { task ->
                            isSaving = false
                            scope.launch {
                                if (task.isSuccessful) {
                                    snackbarHostState.showSnackbar("✅ Animal guardado correctamente")
                                    navController.popBackStack()
                                } else {
                                    snackbarHostState.showSnackbar("❌ Error: ${task.exception?.message}")
                                }
                            }
                        }
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar("⚠️ Por favor completa los campos obligatorios")
                        }
                    }
                },
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Text(if (isSaving) "Guardando..." else "Guardar Animal")
            }

            Spacer(modifier = Modifier.height(50.dp)) // margen inferior extra
        }
    }
}
