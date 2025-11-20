package com.caycedo.registroganado.ui.compose.screens.animals

import android.app.DatePickerDialog
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.getValue
import java.util.Calendar
import java.util.UUID
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAnimalScreen(
    navController: NavController,
    animalIdParam: String? = null,
    propietarioIdParam: String? = null
) {

    val context = LocalContext.current
    val db = FirebaseDatabase.getInstance().getReference("animales_global")
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid
    val userId = propietarioIdParam ?: currentUid ?: return

    // Estado de formulario
    var firebaseKey by remember { mutableStateOf<String?>(null) }
    var id by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var raza by remember { mutableStateOf("") }
    var sexo by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("") }
    var nacimiento by remember { mutableStateOf("") }
    var peso by remember { mutableStateOf("") }
    var produccion by remember { mutableStateOf("") }
    var estadoReproductivo by remember { mutableStateOf("") }
    var ultimoParto by remember { mutableStateOf("") }
    var vacunasSeleccionadas by remember { mutableStateOf(setOf<String>()) }
    var tratamientos by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }

    var isSaving by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf("") }

    // Dropdowns
    var expandedRaza by remember { mutableStateOf(false) }
    var expandedSexo by remember { mutableStateOf(false) }
    var expandedTipo by remember { mutableStateOf(false) }
    var expandedEstadoRep by remember { mutableStateOf(false) }
    var expandedVacunas by remember { mutableStateOf(false) }

    // Catálogos
    val razas = listOf(
        "Holstein", "Jersey", "Brahman", "Simmental",
        "Normando", "Pardo Suizo", "Gyr", "Angus",
        "Hereford", "Charolais", "Mestizo", "Otra"
    )
    val sexos = listOf("Macho", "Hembra")
    val tipos = listOf("Leche", "Carne", "Mixto", "Cría")
    val estadosRep = listOf("Vacía", "Preñada", "Lactando", "Seca", "No aplica")

    val vacunasDisponibles = listOf(
        "Fiebre Aftosa",
        "Brucelosis",
        "Rabia",
        "Carbón Sintomático",
        "Clostridiosis",
        "IBR",
        "DVB",
        "Leptospirosis",
        "Otra"
    )

    // Cargar datos en modo edición
    LaunchedEffect(animalIdParam) {
        if (animalIdParam != null) {
            db.orderByChild("id").equalTo(animalIdParam)
                .get()
                .addOnSuccessListener { snap ->
                    if (snap.exists()) {
                        val node = snap.children.first()
                        firebaseKey = node.key
                        val animal = node.getValue<Animal>()
                        if (animal != null) {
                            id = animal.id
                            nombre = animal.nombre
                            raza = animal.raza
                            sexo = animal.sexo
                            tipo = animal.tipoProduccion
                            nacimiento = animal.nacimiento
                            peso = animal.peso
                            produccion = animal.produccionLeche
                            estadoReproductivo = animal.estadoReproductivo
                            ultimoParto = animal.ultimoParto
                            // Convertir string de vacunas a set
                            vacunasSeleccionadas = animal.vacunaciones
                                .split(",")
                                .map { it.trim() }
                                .filter { it.isNotEmpty() }
                                .toSet()
                            tratamientos = animal.tratamientos
                            observaciones = animal.observaciones
                        }
                    }
                }
        } else {
            if (id.isBlank()) {
                id = UUID.randomUUID().toString().take(8).uppercase()
            }
        }
    }

    // Date Pickers
    val nacimientoPicker = remember {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                nacimiento = "%02d/%02d/%04d".format(dayOfMonth, month + 1, year)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    val ultimoPartoPicker = remember {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                ultimoParto = "%02d/%02d/%04d".format(dayOfMonth, month + 1, year)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    // UI
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        if (animalIdParam == null) "Registrar Animal" else "Editar Animal",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { pad ->

        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
        ) {
            // Contenedor scrollable
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                // SECCIÓN: IDENTIFICACIÓN
                SectionHeader("Identificación")

                OutlinedTextField(
                    value = id,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("ID del animal") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        disabledTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    enabled = false
                )

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = nombre.isBlank() && mensajeError.isNotEmpty()
                )

                // SECCIÓN: CARACTERÍSTICAS
                SectionHeader("Características")

                // Raza
                ExposedDropdownMenuBox(
                    expanded = expandedRaza,
                    onExpandedChange = { expandedRaza = it }
                ) {
                    OutlinedTextField(
                        value = raza,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Raza *") },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedRaza) },
                        isError = raza.isBlank() && mensajeError.isNotEmpty()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedRaza,
                        onDismissRequest = { expandedRaza = false }
                    ) {
                        razas.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    raza = item
                                    expandedRaza = false
                                }
                            )
                        }
                    }
                }

                // Sexo
                ExposedDropdownMenuBox(
                    expanded = expandedSexo,
                    onExpandedChange = { expandedSexo = it }
                ) {
                    OutlinedTextField(
                        value = sexo,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Sexo *") },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedSexo) },
                        isError = sexo.isBlank() && mensajeError.isNotEmpty()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedSexo,
                        onDismissRequest = { expandedSexo = false }
                    ) {
                        sexos.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    sexo = item
                                    expandedSexo = false
                                }
                            )
                        }
                    }
                }

                // Tipo producción
                ExposedDropdownMenuBox(
                    expanded = expandedTipo,
                    onExpandedChange = { expandedTipo = it }
                ) {
                    OutlinedTextField(
                        value = tipo,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo de producción") },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedTipo) }
                    )
                    ExposedDropdownMenu(
                        expanded = expandedTipo,
                        onDismissRequest = { expandedTipo = false }
                    ) {
                        tipos.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    tipo = item
                                    expandedTipo = false
                                }
                            )
                        }
                    }
                }

                // Fecha nacimiento - CORREGIDO
                OutlinedTextField(
                    value = nacimiento,
                    onValueChange = {},
                    label = { Text("Fecha de nacimiento *") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { nacimientoPicker.show() }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Seleccionar fecha")
                        }
                    },
                    isError = nacimiento.isBlank() && mensajeError.isNotEmpty(),
                    placeholder = { Text("DD/MM/AAAA") }
                )

                // SECCIÓN: PRODUCCIÓN
                SectionHeader("Datos de Producción")

                OutlinedTextField(
                    value = peso,
                    onValueChange = { peso = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Peso (kg)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    placeholder = { Text("Ej: 450.5") }
                )

                OutlinedTextField(
                    value = produccion,
                    onValueChange = { produccion = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Producción de leche (L/día)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    placeholder = { Text("Ej: 18.5") }
                )

                // SECCIÓN: REPRODUCCIÓN
                SectionHeader("Estado Reproductivo")

                ExposedDropdownMenuBox(
                    expanded = expandedEstadoRep,
                    onExpandedChange = { expandedEstadoRep = it }
                ) {
                    OutlinedTextField(
                        value = estadoReproductivo,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Estado reproductivo") },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedEstadoRep) }
                    )
                    ExposedDropdownMenu(
                        expanded = expandedEstadoRep,
                        onDismissRequest = { expandedEstadoRep = false }
                    ) {
                        estadosRep.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    estadoReproductivo = item
                                    expandedEstadoRep = false
                                }
                            )
                        }
                    }
                }

                // Último parto - CORREGIDO
                OutlinedTextField(
                    value = ultimoParto,
                    onValueChange = {},
                    label = { Text("Fecha último parto") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { ultimoPartoPicker.show() }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Seleccionar fecha")
                        }
                    },
                    placeholder = { Text("DD/MM/AAAA") }
                )

                // SECCIÓN: SALUD
                SectionHeader("Salud y Observaciones")

                // Vacunas - NUEVO CON SELECCIÓN MÚLTIPLE
                ExposedDropdownMenuBox(
                    expanded = expandedVacunas,
                    onExpandedChange = { expandedVacunas = it }
                ) {
                    OutlinedTextField(
                        value = if (vacunasSeleccionadas.isEmpty()) ""
                        else "${vacunasSeleccionadas.size} vacuna(s) seleccionada(s)",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Vacunas aplicadas") },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedVacunas) },
                        placeholder = { Text("Seleccionar vacunas") }
                    )
                    ExposedDropdownMenu(
                        expanded = expandedVacunas,
                        onDismissRequest = { expandedVacunas = false }
                    ) {
                        vacunasDisponibles.forEach { vacuna ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Checkbox(
                                            checked = vacunasSeleccionadas.contains(vacuna),
                                            onCheckedChange = null
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(vacuna)
                                    }
                                },
                                onClick = {
                                    vacunasSeleccionadas = if (vacunasSeleccionadas.contains(vacuna)) {
                                        vacunasSeleccionadas - vacuna
                                    } else {
                                        vacunasSeleccionadas + vacuna
                                    }
                                }
                            )
                        }
                    }
                }

                // Mostrar vacunas seleccionadas
                if (vacunasSeleccionadas.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                "Vacunas seleccionadas:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                vacunasSeleccionadas.joinToString(", "),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = tratamientos,
                    onValueChange = { tratamientos = it },
                    label = { Text("Tratamientos médicos") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3,
                    placeholder = { Text("Descripción de tratamientos recibidos") }
                )

                OutlinedTextField(
                    value = observaciones,
                    onValueChange = { observaciones = it },
                    label = { Text("Observaciones adicionales") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 4,
                    placeholder = { Text("Notas importantes sobre el animal") }
                )

                Spacer(Modifier.height(16.dp))
            }

            // Botón fijo en la parte inferior
            Surface(
                shadowElevation = 8.dp,
                tonalElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    if (mensajeError.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Text(
                                text = mensajeError,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(12.dp),
                                fontSize = 14.sp
                            )
                        }
                    }

                    Button(
                        onClick = {
                            // Validaciones
                            if (nombre.isBlank() || raza.isBlank() || sexo.isBlank() || nacimiento.isBlank()) {
                                mensajeError = "Completa los campos obligatorios (*)"
                                return@Button
                            }

                            isSaving = true
                            mensajeError = ""

                            val animal = Animal(
                                id = id,
                                nombre = nombre,
                                raza = raza,
                                sexo = sexo,
                                tipoProduccion = tipo,
                                nacimiento = nacimiento,
                                peso = peso,
                                produccionLeche = produccion,
                                estadoReproductivo = estadoReproductivo,
                                ultimoParto = ultimoParto,
                                vacunaciones = vacunasSeleccionadas.joinToString(", "),
                                tratamientos = tratamientos,
                                observaciones = observaciones,
                                propietarioId = userId,
                                activo = true
                            )

                            if (firebaseKey != null) {
                                // MODO EDICIÓN: Actualizar el registro existente
                                db.child(firebaseKey!!).setValue(animal)
                                    .addOnSuccessListener {
                                        isSaving = false
                                        navController.popBackStack()
                                    }
                                    .addOnFailureListener { e ->
                                        isSaving = false
                                        mensajeError = "Error al actualizar: ${e.message}"
                                    }
                            } else {
                                // MODO CREAR: Crear nuevo registro
                                val newKey = db.push().key!!
                                db.child(newKey).setValue(animal)
                                    .addOnSuccessListener {
                                        isSaving = false
                                        navController.popBackStack()
                                    }
                                    .addOnFailureListener { e ->
                                        isSaving = false
                                        mensajeError = "Error al guardar: ${e.message}"
                                    }
                            }
                        },
                        enabled = !isSaving,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(
                            text = when {
                                isSaving && firebaseKey != null -> "Actualizando..."
                                isSaving -> "Guardando..."
                                firebaseKey != null -> "Actualizar Animal"
                                else -> "Guardar Animal"
                            },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
    Divider(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
        thickness = 2.dp,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}