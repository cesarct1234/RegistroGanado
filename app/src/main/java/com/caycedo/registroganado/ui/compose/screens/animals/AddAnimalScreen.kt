package com.caycedo.registroganado.ui.compose.screens.animals

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.getValue
import java.util.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAnimalScreen(
    navController: NavController,
    animalIdParam: String? = null,
    propietarioIdParam: String? = null
) {

    val context = LocalContext.current

    val dbAnimales = FirebaseDatabase.getInstance().getReference("animales_global")
    val dbUsers = FirebaseDatabase.getInstance().getReference("usuarios")

    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

    var rolUsuario by remember { mutableStateOf<String?>(null) }

    // 🔰 Obtener ROL DEL USUARIO
    LaunchedEffect(Unit) {
        dbUsers.child(uid).get().addOnSuccessListener { snap ->
            rolUsuario = snap.child("rol").value?.toString()?.lowercase()?.trim()
        }
    }

    if (rolUsuario == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val userId = propietarioIdParam ?: uid

    // -------------------- CAMPOS ---------------------
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

    // Veterinario
    var diagnosticoGeneral by remember { mutableStateOf("") }
    var estadoSaludGeneral by remember { mutableStateOf("") }
    var proximaRevision by remember { mutableStateOf("") }

    // Cuidador
    var alimentacion by remember { mutableStateOf("") }
    var actividades by remember { mutableStateOf("") }
    var pesoDia by remember { mutableStateOf("") }
    var reporteCuidador by remember { mutableStateOf("") }

    // DropDown
    var expandedRaza by remember { mutableStateOf(false) }
    var expandedSexo by remember { mutableStateOf(false) }
    var expandedTipo by remember { mutableStateOf(false) }
    var expandedEstadoRep by remember { mutableStateOf(false) }
    var expandedVacunas by remember { mutableStateOf(false) }

    val razas = listOf("Holstein", "Jersey", "Brahman", "Normando", "Gyr", "Mestizo", "Otra")
    val sexos = listOf("Macho", "Hembra")
    val tipos = listOf("Leche", "Carne", "Mixto", "Cría")
    val estados = listOf("Vacía", "Preñada", "Lactando", "Seca", "No aplica")
    val vacunasDisponibles = listOf("Fiebre Aftosa", "Brucelosis", "Rabia", "Clostridiosis", "Otra")

    // ---------------------------------------------------
    // 🔰 CARGA MODO EDICIÓN
    // ---------------------------------------------------
    LaunchedEffect(animalIdParam) {
        if (animalIdParam != null) {
            dbAnimales.orderByChild("id").equalTo(animalIdParam)
                .get().addOnSuccessListener { snap ->
                    if (!snap.exists()) return@addOnSuccessListener

                    val node = snap.children.first()
                    firebaseKey = node.key
                    val a = node.getValue<Animal>() ?: return@addOnSuccessListener

                    id = a.id
                    nombre = a.nombre
                    raza = a.raza
                    sexo = a.sexo
                    tipo = a.tipoProduccion
                    nacimiento = a.nacimiento
                    peso = a.peso
                    produccion = a.produccionLeche
                    estadoReproductivo = a.estadoReproductivo
                    ultimoParto = a.ultimoParto
                    vacunasSeleccionadas =
                        a.vacunaciones.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()

                    tratamientos = a.tratamientos
                    observaciones = a.observaciones

                    diagnosticoGeneral = a.diagnosticoGeneral
                    estadoSaludGeneral = a.estadoSaludGeneral
                    proximaRevision = a.proximaRevision

                    alimentacion = a.alimentacion
                    actividades = a.actividades
                    pesoDia = a.pesoDia
                    reporteCuidador = a.reporteCuidador
                }
        } else {
            id = UUID.randomUUID().toString().take(8).uppercase()
        }
    }

    // ------------------ DATE PICKERS --------------------
    val nacimientoPicker = remember {
        val cal = Calendar.getInstance()
        DatePickerDialog(context,
            { _, y, m, d -> nacimiento = "%02d/%02d/%04d".format(d, m + 1, y) },
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    val partoPicker = remember {
        val cal = Calendar.getInstance()
        DatePickerDialog(context,
            { _, y, m, d -> ultimoParto = "%02d/%02d/%04d".format(d, m + 1, y) },
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    // ------------------ UI ------------------------------

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (animalIdParam == null) "Registrar Animal" else "Editar Animal") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { pad ->

        Column(
            Modifier.padding(pad).verticalScroll(rememberScrollState()).padding(16.dp)
        ) {

            SectionHeader("Identificación")

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre *") },
                modifier = Modifier.fillMaxWidth()
            )

            // ----------- RAZA DROPDOWN
            ExposedDropdownMenuBox(expanded = expandedRaza, onExpandedChange = { expandedRaza = it }) {
                OutlinedTextField(
                    value = raza,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Raza *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRaza) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )

                ExposedDropdownMenu(expanded = expandedRaza, onDismissRequest = { expandedRaza = false }) {
                    razas.forEach {
                        DropdownMenuItem(
                            text = { Text(it) },
                            onClick = {
                                raza = it
                                expandedRaza = false
                            }
                        )
                    }
                }
            }

            // ------------ SEXO DROPDOWN
            ExposedDropdownMenuBox(expanded = expandedSexo, onExpandedChange = { expandedSexo = it }) {
                OutlinedTextField(
                    value = sexo,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Sexo *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSexo) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )

                ExposedDropdownMenu(expanded = expandedSexo, onDismissRequest = { expandedSexo = false }) {
                    sexos.forEach {
                        DropdownMenuItem(
                            text = { Text(it) },
                            onClick = {
                                sexo = it
                                expandedSexo = false
                            }
                        )
                    }
                }
            }

            // ------------ NACIMIENTO
            OutlinedTextField(
                value = nacimiento,
                onValueChange = {},
                readOnly = true,
                label = { Text("Nacimiento *") },
                trailingIcon = {
                    IconButton(onClick = { nacimientoPicker.show() }) {
                        Icon(Icons.Default.CalendarToday, null)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // ======================================================================
            // PRODUCCIÓN (SOLO propietario y admin)
            // ======================================================================
            if (rolUsuario == "propietario" || rolUsuario == "administrador") {

                SectionHeader("Producción")

                ExposedDropdownMenuBox(expanded = expandedTipo, onExpandedChange = { expandedTipo = it }) {
                    OutlinedTextField(
                        value = tipo,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo producción") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTipo) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )

                    ExposedDropdownMenu(expanded = expandedTipo, onDismissRequest = { expandedTipo = false }) {
                        tipos.forEach {
                            DropdownMenuItem(
                                text = { Text(it) },
                                onClick = {
                                    tipo = it
                                    expandedTipo = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = peso,
                    onValueChange = { peso = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Peso (kg)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = produccion,
                    onValueChange = { produccion = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Producción leche (L/día)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ======================================================================
            // VETERINARIO + ADMIN
            // ======================================================================
            if (rolUsuario == "veterinario" || rolUsuario == "administrador") {

                SectionHeader("Evaluación Veterinaria")

                OutlinedTextField(
                    value = estadoSaludGeneral,
                    onValueChange = { estadoSaludGeneral = it },
                    label = { Text("Estado general") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = diagnosticoGeneral,
                    onValueChange = { diagnosticoGeneral = it },
                    label = { Text("Diagnóstico") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = proximaRevision,
                    onValueChange = { proximaRevision = it },
                    label = { Text("Próxima revisión") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ======================================================================
            // CUIDADOR + ADMIN
            // ======================================================================
            if (rolUsuario == "cuidador" || rolUsuario == "administrador") {

                SectionHeader("Registro del cuidador")

                OutlinedTextField(
                    value = alimentacion,
                    onValueChange = { alimentacion = it },
                    label = { Text("Alimentación") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = actividades,
                    onValueChange = { actividades = it },
                    label = { Text("Actividades") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = pesoDia,
                    onValueChange = { pesoDia = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Peso del día") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = reporteCuidador,
                    onValueChange = { reporteCuidador = it },
                    label = { Text("Reporte") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(20.dp))

            // ============================================================
            // GUARDAR
            // ============================================================

            Button(
                onClick = {

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
                        vacunaciones = vacunasSeleccionadas.joinToString(),
                        tratamientos = tratamientos,
                        observaciones = observaciones,
                        propietarioId = userId,
                        diagnosticoGeneral = diagnosticoGeneral,
                        estadoSaludGeneral = estadoSaludGeneral,
                        proximaRevision = proximaRevision,
                        alimentacion = alimentacion,
                        actividades = actividades,
                        pesoDia = pesoDia,
                        reporteCuidador = reporteCuidador,
                        activo = true
                    )

                    if (firebaseKey != null) {
                        dbAnimales.child(firebaseKey!!).setValue(animal)
                    } else {
                        dbAnimales.push().setValue(animal)
                    }

                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar")
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
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

