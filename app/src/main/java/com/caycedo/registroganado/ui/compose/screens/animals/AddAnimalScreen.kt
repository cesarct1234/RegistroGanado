@file:OptIn(ExperimentalMaterial3Api::class)

package com.caycedo.registroganado.ui_compose.screens

// 🧱 Foundation
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

// 🎨 Material 3
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

// Firebase
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

// Fecha
import android.app.DatePickerDialog
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.max

// FlowRow
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.text.KeyboardOptions


// ============================================================================================
//  🔥 MODELO CORRECTO (MATCH con AnimalListScreen)
// ============================================================================================
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
    val tipoProduccion: String = "",
    val vacunas: String = "",
    val tratamientos: String = "",
    val observaciones: String = "",
    val aptoConsumo: Boolean = false,
    val propietarioId: String = "",
    val activo: Boolean = true
)


// ============================================================================================
//  🔥 PANTALLA PRINCIPAL
// ============================================================================================
@Composable
fun AddAnimalScreen(
    navController: NavController,
    propietarioIdParam: String? = null,
    animalIdParam: String? = null
) {

    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: return

    val database = FirebaseDatabase.getInstance().getReference("animales")

    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scroll = rememberScrollState()

    val isEditing = !animalIdParam.isNullOrEmpty()


    // --------------------------------------------------------------------------------------------
    //  🔥 CAMPOS DEL FORMULARIO
    // --------------------------------------------------------------------------------------------

    var id by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var raza by remember { mutableStateOf("") }
    var sexo by remember { mutableStateOf("") }
    var nacimiento by remember { mutableStateOf("") }
    var edad by remember { mutableStateOf("") }
    var peso by remember { mutableStateOf("") }
    var tipoProduccion by remember { mutableStateOf("Leche") }
    var estadoReproductivo by remember { mutableStateOf("") }
    var ultimoParto by remember { mutableStateOf("") }
    var produccionLeche by remember { mutableStateOf("") }
    var vacunasSel by remember { mutableStateOf(setOf<String>()) }
    var tratamientos by remember { mutableStateOf("") }
    var aptoConsumo by remember { mutableStateOf(false) }
    var observaciones by remember { mutableStateOf("") }

    var propietarioId by remember { mutableStateOf(propietarioIdParam ?: userId) }
    var isSaving by remember { mutableStateOf(false) }


    // Catálogos
    val razas = listOf("Holstein", "Brahman", "Jersey", "Angus", "Normando", "Gyr", "Pardo Suizo")
    val sexos = listOf("Macho", "Hembra")
    val tiposProd = listOf("Leche", "Carne", "Mixta")
    val vacunasCatalogo = listOf("Carbunco", "Brucelosis", "Aftosa", "Rabia", "Leptospirosis")


    // ====================================================================================
    //  🔥 Cargar datos cuando se está editando
    // ====================================================================================
    LaunchedEffect(animalIdParam) {
        if (isEditing) {
            database.child(animalIdParam!!).get().addOnSuccessListener { snap ->
                snap.getValue(Animal::class.java)?.let { a ->
                    id = a.id
                    nombre = a.nombre
                    raza = a.raza
                    sexo = a.sexo
                    nacimiento = a.nacimiento
                    edad = a.edad
                    peso = a.peso
                    tipoProduccion = a.tipoProduccion
                    estadoReproductivo = a.estadoReproductivo
                    ultimoParto = a.ultimoParto
                    produccionLeche = a.produccionLeche
                    vacunasSel = a.vacunas.split(",").map { it.trim() }.filter { it.isNotBlank() }.toSet()
                    tratamientos = a.tratamientos
                    observaciones = a.observaciones
                    aptoConsumo = a.aptoConsumo
                    propietarioId = a.propietarioId
                }
            }
        }
    }


    // ====================================================================================
    //  Fecha de Nacimiento → calcula Edad
    // ====================================================================================
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


    fun sugerirId(): String = "A" + System.currentTimeMillis().toString().takeLast(5)


    // ====================================================================================
    //  UI
    // ====================================================================================
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (isEditing) "Editar Animal" else "Registrar Animal") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { pad ->

        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // -------------------------------------------------------------------------
            // 🔷 ID
            // -------------------------------------------------------------------------
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = id,
                    onValueChange = { id = it.trim() },
                    label = { Text("ID del animal") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = { id = sugerirId() }) { Text("Sugerir") }
            }


            // Nombre
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )


            // -------------------------------------------------------------------------
            // 🔷 Raza
            // -------------------------------------------------------------------------
            DropdownField("Raza", raza, razas) { raza = it }

            // Sexo
            DropdownField("Sexo", sexo, sexos) { sexo = it }

            // Tipo Producción
            DropdownField("Tipo de producción", tipoProduccion, tiposProd) { tipoProduccion = it }


            // -------------------------------------------------------------------------
            // 🔷 Fecha de nacimiento
            // -------------------------------------------------------------------------
            DateField("Nacimiento (dd/mm/aaaa)", nacimiento) { nacimiento = it }


            OutlinedTextField(
                value = peso,
                onValueChange = { peso = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Peso (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            DateField("Último parto (dd/mm/aaaa)", ultimoParto) { ultimoParto = it }


            if (tipoProduccion != "Carne") {
                OutlinedTextField(
                    value = produccionLeche,
                    onValueChange = { produccionLeche = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Producción leche (L/día)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }


            // -------------------------------------------------------------------------
            //  🔷 Vacunas
            // -------------------------------------------------------------------------
            Column {
                Text("Vacunas", fontWeight = FontWeight.SemiBold)

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    vacunasCatalogo.forEach { vacuna ->
                        val selected = vacuna in vacunasSel

                        AssistChip(
                            onClick = {
                                vacunasSel = if (selected) vacunasSel - vacuna else vacunasSel + vacuna
                            },
                            label = { Text(vacuna) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor =
                                    if (selected) MaterialTheme.colorScheme.primary.copy(0.18f)
                                    else Color.Transparent
                            )
                        )
                    }
                }
            }


            OutlinedTextField(
                value = tratamientos,
                onValueChange = { tratamientos = it },
                label = { Text("Tratamientos") }
            )

            OutlinedTextField(
                value = observaciones,
                onValueChange = { observaciones = it },
                label = { Text("Observaciones") }
            )


            // -------------------------------------------------------------------------
            // 🔷 Apto para consumo
            // -------------------------------------------------------------------------
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.medium)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Apto para consumo", fontWeight = FontWeight.Bold)
                    Text("Según peso y tratamientos.", style = MaterialTheme.typography.bodySmall)
                }
                Switch(checked = aptoConsumo, onCheckedChange = { aptoConsumo = it })
            }


            // ====================================================================================
            //  🔥 BOTÓN GUARDAR
            // ====================================================================================
            Button(
                onClick = {

                    if (isSaving) return@Button

                    if (id.isBlank() || nombre.isBlank() || raza.isBlank() || sexo.isBlank()) {
                        scope.launch { snackbar.showSnackbar("⚠️ Campos obligatorios incompletos") }
                        return@Button
                    }

                    isSaving = true

                    val ref = if (isEditing) {
                        database.child(animalIdParam!!)
                    } else {
                        val newKey = database.push().key!!
                        database.child(newKey)
                    }

                    val animal = Animal(
                        id = ref.key!!,
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
                        vacunas = vacunasSel.joinToString(),
                        tratamientos = tratamientos,
                        observaciones = observaciones,
                        aptoConsumo = aptoConsumo,
                        propietarioId = propietarioId,
                        activo = true
                    )

                    ref.setValue(animal).addOnCompleteListener { task ->
                        isSaving = false
                        scope.launch {
                            if (task.isSuccessful) {
                                snackbar.showSnackbar("✔ Guardado correctamente")
                                navController.popBackStack()
                            } else {
                                snackbar.showSnackbar("❌ Error al guardar")
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isEditing) "Actualizar animal" else "Guardar animal")
            }
        }
    }
}


// ============================================================================================
// 🔧 COMPONENTE Dropdown reutilizable
// ============================================================================================
@Composable
fun DropdownField(
    label: String,
    value: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = value,
            readOnly = true,
            onValueChange = {},
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach {
                DropdownMenuItem(
                    text = { Text(it) },
                    onClick = {
                        onSelect(it)
                        expanded = false
                    }
                )
            }
        }
    }
}


// ============================================================================================
// 🔧 COMPONENTE DATE PICKER
// ============================================================================================
@Composable
fun DateField(label: String, value: String, onPick: (String) -> Unit) {

    val context = LocalContext.current

    fun abrirDatePicker() {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, y, m, d -> onPick("%02d/%02d/%04d".format(d, m + 1, y)) },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { abrirDatePicker() }
    )
}


