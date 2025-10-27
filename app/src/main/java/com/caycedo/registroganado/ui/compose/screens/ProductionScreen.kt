package com.caycedo.registroganado.ui.compose.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import java.util.*

data class Produccion(
    val id: String = "",
    val fecha: String = "",
    val idAnimal: String = "",
    val nombreAnimal: String = "",
    val litrosLeche: String = "",
    val observaciones: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductionScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: return
    val database = FirebaseDatabase.getInstance().getReference("produccion").child(userId)

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var fecha by remember { mutableStateOf("") }
    var idAnimal by remember { mutableStateOf("") }
    var nombreAnimal by remember { mutableStateOf("") }
    var litrosLeche by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    // 🗓️ Selector de fecha
    fun abrirDatePicker(onDateSelected: (String) -> Unit) {
        val calendario = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, day ->
                val fechaSeleccionada = "%02d/%02d/%04d".format(day, month + 1, year)
                onDateSelected(fechaSeleccionada)
            },
            calendario.get(Calendar.YEAR),
            calendario.get(Calendar.MONTH),
            calendario.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Registro de Producción", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // 📅 Fecha (editable o seleccionable)
            OutlinedTextField(
                value = fecha,
                onValueChange = { fecha = it },
                label = { Text("Fecha (dd/mm/aaaa)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .noRippleClickable {
                        abrirDatePicker { fecha = it }
                    },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            OutlinedTextField(
                value = idAnimal,
                onValueChange = { idAnimal = it },
                label = { Text("ID del animal") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = nombreAnimal,
                onValueChange = { nombreAnimal = it },
                label = { Text("Nombre del animal") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = litrosLeche,
                onValueChange = { litrosLeche = it },
                label = { Text("Producción de leche (L/día)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = observaciones,
                onValueChange = { observaciones = it },
                label = { Text("Observaciones") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (fecha.isNotEmpty() && nombreAnimal.isNotEmpty() && litrosLeche.isNotEmpty()) {
                        isSaving = true
                        val id = database.push().key ?: return@Button
                        val nuevaProduccion = Produccion(
                            id = id,
                            fecha = fecha,
                            idAnimal = idAnimal,
                            nombreAnimal = nombreAnimal,
                            litrosLeche = litrosLeche,
                            observaciones = observaciones
                        )
                        database.child(id).setValue(nuevaProduccion).addOnCompleteListener { task ->
                            isSaving = false
                            scope.launch {
                                if (task.isSuccessful) {
                                    snackbarHostState.showSnackbar("✅ Registro guardado correctamente")
                                    navController.popBackStack()
                                } else {
                                    snackbarHostState.showSnackbar("❌ Error: ${task.exception?.message}")
                                }
                            }
                        }
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar("⚠️ Por favor completa los campos requeridos")
                        }
                    }
                },
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text(if (isSaving) "Guardando..." else "Guardar Producción")
            }
        }
    }
}

/**
 * 🧩 Extensión auxiliar: clickable sin ripple (corrige crash en Compose 1.7+)
 */
fun Modifier.noRippleClickable2(onClick: () -> Unit): Modifier = composed {
    clickable(
        indication = null,
        interactionSource = remember { MutableInteractionSource() }
    ) {
        onClick()
    }
}

