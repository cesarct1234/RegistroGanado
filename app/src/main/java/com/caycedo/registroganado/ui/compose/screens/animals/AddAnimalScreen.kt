package com.caycedo.registroganado.ui.compose.screens.animals

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAnimalScreen(
    navController: NavController,
    propietarioIdParam: String? = null,
    animalIdParam: String? = null
) {
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: return

    // 🔥 MUY IMPORTANTE — CADA USUARIO CON SU CARPETA
    val db = FirebaseDatabase.getInstance().getReference("animales").child(userId)

    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val isEditing = animalIdParam != null

    var id by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var raza by remember { mutableStateOf("") }
    var sexo by remember { mutableStateOf("") }
    var nacimiento by remember { mutableStateOf("") }
    var edad by remember { mutableStateOf("") }
    var peso by remember { mutableStateOf("") }
    var estadoReproductivo by remember { mutableStateOf("") }
    var ultimoParto by remember { mutableStateOf("") }
    var produccionLeche by remember { mutableStateOf("") }
    var tipoProduccion by remember { mutableStateOf("Leche") }
    var vacunas by remember { mutableStateOf(setOf<String>()) }
    var tratamientos by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }
    var aptoConsumo by remember { mutableStateOf(false) }
    var propietarioId by remember { mutableStateOf(propietarioIdParam ?: userId) }
    var isSaving by remember { mutableStateOf(false) }

    val razas = listOf("Holstein", "Brahman", "Jersey", "Angus", "Normando", "Gyr")
    val sexos = listOf("Macho", "Hembra")
    val tiposProd = listOf("Leche", "Carne", "Mixta")
    val vacunasCatalogo = listOf("Carbunco", "Aftosa", "Brucelosis", "Rabia", "Leptospirosis")

    // ----------------------------------------------------
    // 🔥 Cargar datos si EDITA animal
    // ----------------------------------------------------
    LaunchedEffect(animalIdParam) {
        if (isEditing) {
            db.child(animalIdParam!!).get().addOnSuccessListener { snap ->
                snap.getValue(Animal::class.java)?.let { a ->
                    id = a.id
                    nombre = a.nombre
                    raza = a.raza
                    sexo = a.sexo
                    nacimiento = a.nacimiento
                    edad = a.edad
                    peso = a.peso
                    estadoReproductivo = a.estadoReproductivo
                    ultimoParto = a.ultimoParto
                    produccionLeche = a.produccionLeche
                    tipoProduccion = a.tipoProduccion
                    vacunas = a.vacunas.split(",").filter { it.isNotBlank() }.toSet()
                    tratamientos = a.tratamientos
                    observaciones = a.observaciones
                    aptoConsumo = a.aptoConsumo
                    propietarioId = a.propietarioId
                }
            }
        }
    }

    // ----------------------------------------------------
    // 🔥 Calcular edad automática
    // ----------------------------------------------------
    LaunchedEffect(nacimiento) {
        if (nacimiento.matches(Regex("\\d{2}/\\d{2}/\\d{4}"))) {
            try {
                val (d, m, y) = nacimiento.split("/").map { it.toInt() }
                val birth = Calendar.getInstance().apply { set(y, m - 1, d) }
                val now = Calendar.getInstance()
                var years = now.get(Calendar.YEAR) - birth.get(Calendar.YEAR)
                if (now.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) years--
                edad = max(0, years).toString()
            } catch (_: Exception) {}
        }
    }

    fun sugerirId(): String = "A-" + System.currentTimeMillis().toString().takeLast(5)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (isEditing) "Editar Animal" else "Registrar Animal") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { pad ->

        Column(
            modifier = Modifier
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ID
            Row {
                OutlinedTextField(
                    value = id,
                    onValueChange = { id = it },
                    label = { Text("ID del animal") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = { id = sugerirId() }) {
                    Text("Sugerir")
                }
            }

            // NOMBRE
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )

            // RAZA
            DropdownField("Raza", raza, razas) { raza = it }

            // SEXO
            DropdownField("Sexo", sexo, sexos) { sexo = it }

            // TIPO PRODUCCIÓN
            DropdownField("Tipo producción", tipoProduccion, tiposProd) { tipoProduccion = it }

            // FECHA NACIMIENTO
            DateField("Nacimiento", nacimiento) { nacimiento = it }

            // PESO
            OutlinedTextField(
                value = peso,
                onValueChange = { peso = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Peso (kg)") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // PRODUCCIÓN LECHE
            if (tipoProduccion == "Leche" || tipoProduccion == "Mixta") {
                OutlinedTextField(
                    value = produccionLeche,
                    onValueChange = { produccionLeche = it },
                    label = { Text("Producción de leche (L/día)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // GUARDAR
            Button(
                onClick = {
                    if (isSaving) return@Button
                    if (id.isBlank() || nombre.isBlank() || raza.isBlank() || sexo.isBlank()) {
                        scope.launch { snackbar.showSnackbar("Faltan campos obligatorios") }
                        return@Button
                    }

                    isSaving = true

                    val newId = if (isEditing) animalIdParam!! else db.push().key!!

                    val animal = Animal(
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
                        tipoProduccion = tipoProduccion,
                        vacunas = vacunas.joinToString(),
                        tratamientos = tratamientos,
                        observaciones = observaciones,
                        aptoConsumo = aptoConsumo,
                        propietarioId = propietarioId,
                        activo = true
                    )

                    db.child(newId).setValue(animal).addOnCompleteListener {
                        isSaving = false
                        scope.launch {
                            snackbar.showSnackbar(if (isEditing) "Animal actualizado" else "Animal registrado")
                        }
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isEditing) "Actualizar" else "Guardar")
            }
        }
    }
}

@Composable
fun DateField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    val context = LocalContext.current

    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePicker = DatePickerDialog(
        context,
        { _, y, m, d ->
            onValueChange(String.format("%02d/%02d/%04d", d, m + 1, y))
        },
        year, month, day
    )

    OutlinedTextField(
        value = value,
        onValueChange = {},
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                datePicker.show()
            },
        enabled = false,
        readOnly = true
    )
}


@Composable
fun DropdownField(
    label: String,
    selected: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            label = { Text(label) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            enabled = false,
            readOnly = true
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                        onSelect(item)
                        expanded = false
                    }
                )
            }
        }
    }
}

