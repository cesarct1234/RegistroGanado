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

// 🧩 Firebase & Navigation
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

// 📅 Utilidades
import android.app.DatePickerDialog
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.max

// 🧱 UI extras
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.text.KeyboardOptions


// 📌 MODELO COMPLETO — LISTO PARA ROLES Y DESHABILITAR
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
    val aptoConsumo: Boolean = false,
    val observaciones: String = "",
    val propietarioId: String = "",
    val activo: Boolean = true
)



@Composable
fun AddAnimalScreen(navController: NavController, animalId: String = "") {

    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: return

    // 🚨 IMPORTANTE → animales globales, no por usuario
    val database = FirebaseDatabase.getInstance().getReference("animales")

    val context = LocalContext.current
    val snackbarHost = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scroll = rememberScrollState()
    val isEditing = animalId.isNotEmpty()


    //----------------------------------------------------------
    // 🟦 CAMPOS DEL FORMULARIO
    //----------------------------------------------------------

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

    var propietarioId by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }


    // Catálogos
    val razas = listOf("Holstein", "Brahman", "Jersey", "Angus", "Normando", "Pardo Suizo", "Gyr", "Simmental")
    val sexos = listOf("Macho", "Hembra")
    val tiposProd = listOf("Leche", "Carne", "Mixta")
    val vacunasCatalogo = listOf("Carbunco", "Brucelosis", "Fiebre Aftosa", "Rabia", "Leptospirosis", "IBR/BVD")

    var expRaza by remember { mutableStateOf(false) }
    var expSexo by remember { mutableStateOf(false) }
    var expTipoProd by remember { mutableStateOf(false) }


    //----------------------------------------------------------
    // 🟦 CARGAR DATOS SI ESTAMOS EDITANDO
    //----------------------------------------------------------
    LaunchedEffect(animalId) {
        if (isEditing) {
            database.child(animalId).get().addOnSuccessListener { snapshot ->
                snapshot.getValue(Animal::class.java)?.let { animal ->

                    id = animal.id
                    nombre = animal.nombre
                    raza = animal.raza
                    sexo = animal.sexo
                    nacimiento = animal.nacimiento
                    edad = animal.edad
                    peso = animal.peso
                    estadoReproductivo = animal.estadoReproductivo
                    ultimoParto = animal.ultimoParto
                    produccionLeche = animal.produccionLeche
                    tipoProduccion = animal.tipoProduccion
                    vacunasSel = animal.vacunas.split(",").map { it.trim() }.toSet()
                    tratamientos = animal.tratamientos
                    aptoConsumo = animal.aptoConsumo
                    observaciones = animal.observaciones
                    propietarioId = animal.propietarioId // 🟦 IMPORTANTE
                }
            }
        }
    }


    //----------------------------------------------------------
    // 🟦 ROL DEL USUARIO
    //----------------------------------------------------------
    var userRole by remember { mutableStateOf("") }
    var listaPropietarios by remember { mutableStateOf(listOf<Pair<String, String>>()) }
    var expandedProp by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val userRef = FirebaseDatabase.getInstance().getReference("usuarios").child(userId)
        userRef.get().addOnSuccessListener { snap ->
            userRole = snap.child("rol").value?.toString() ?: ""

            if (userRole == "administrador") {
                // cargar propietarios
                FirebaseDatabase.getInstance().getReference("usuarios")
                    .orderByChild("rol")
                    .equalTo("propietario")
                    .get()
                    .addOnSuccessListener { users ->
                        val tmp = mutableListOf<Pair<String, String>>()
                        for (child in users.children) {
                            val uid = child.key ?: continue
                            val nombre = child.child("nombre").value?.toString() ?: ""
                            tmp.add(uid to nombre)
                        }
                        listaPropietarios = tmp
                    }
            }
        }
    }


    //----------------------------------------------------------
    // 🟦 FUNCIONES AUXILIARES
    //----------------------------------------------------------
    fun abrirDatePicker(onPick: (String) -> Unit) {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, y, m, d -> onPick("%02d/%02d/%04d".format(d, m + 1, y)) },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

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



    //----------------------------------------------------------
    // 🟦 UI COMPLETA
    //----------------------------------------------------------
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (isEditing) "Editar Animal" else "Registrar Animal") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHost) }
    ) { pad ->

        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            //----------------------------------------------------
            // 🟦 CAMPO ID + BOTÓN SUGERIR
            //----------------------------------------------------
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


            //----------------------------------------------------
            // 🟦 NOMBRE
            //----------------------------------------------------
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre del animal") },
                modifier = Modifier.fillMaxWidth()
            )


            //----------------------------------------------------
            // 🟦 PROPIETARIO SOLO SI ES ADMINISTRADOR
            //----------------------------------------------------
            if (userRole == "administrador") {

                Text("Propietario", fontWeight = FontWeight.Bold)

                ExposedDropdownMenuBox(
                    expanded = expandedProp,
                    onExpandedChange = { expandedProp = it }
                ) {
                    OutlinedTextField(
                        value = listaPropietarios.find { it.first == propietarioId }?.second
                            ?: "Sin propietario",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedProp) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expandedProp,
                        onDismissRequest = { expandedProp = false }
                    ) {

                        DropdownMenuItem(
                            text = { Text("Sin propietario") },
                            onClick = {
                                propietarioId = ""
                                expandedProp = false
                            }
                        )

                        listaPropietarios.forEach { (uid, nombreProp) ->
                            DropdownMenuItem(
                                text = { Text(nombreProp) },
                                onClick = {
                                    propietarioId = uid
                                    expandedProp = false
                                }
                            )
                        }
                    }
                }
            }


            //----------------------------------------------------
            // 🟦 CAMPOS ESPECIALES
            //----------------------------------------------------
            DropdownField("Raza", raza, razas, expRaza, { expRaza = it }) { raza = it }
            DropdownField("Sexo", sexo, sexos, expSexo, { expSexo = it }) { sexo = it }
            DropdownField("Tipo de producción", tipoProduccion, tiposProd, expTipoProd, { expTipoProd = it }) { tipoProduccion = it }

            DateField("Fecha de nacimiento (dd/mm/aaaa)", nacimiento) { nacimiento = it }

            OutlinedTextField(
                value = edad,
                onValueChange = { edad = it.filter { c -> c.isDigit() } },
                label = { Text("Edad (años)") },
                keyboardOptions = KeyboardType.Number.let { KeyboardOptions(keyboardType = it) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = peso,
                onValueChange = { peso = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Peso (kg)") },
                keyboardOptions = KeyboardType.Number.let { KeyboardOptions(keyboardType = it) },
                modifier = Modifier.fillMaxWidth()
            )

            DateField("Último parto (dd/mm/aaaa)", ultimoParto) { ultimoParto = it }

            if (tipoProduccion != "Carne") {
                OutlinedTextField(
                    value = produccionLeche,
                    onValueChange = { produccionLeche = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Producción de leche (L/día)") },
                    keyboardOptions = KeyboardType.Number.let { KeyboardOptions(keyboardType = it) },
                    modifier = Modifier.fillMaxWidth()
                )
            }


            //----------------------------------------------------
            // 🟦 VACUNAS
            //----------------------------------------------------
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Vacunas (selecciona)", fontWeight = FontWeight.SemiBold)

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
                                containerColor = if (selected) MaterialTheme.colorScheme.primary.copy(0.15f)
                                else Color.Transparent,
                                labelColor = if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }


            //----------------------------------------------------
            // 🟦 TRATAMIENTOS / OBSERVACIONES
            //----------------------------------------------------
            OutlinedTextField(value = tratamientos, onValueChange = { tratamientos = it }, label = { Text("Tratamientos") })
            OutlinedTextField(value = observaciones, onValueChange = { observaciones = it }, label = { Text("Observaciones") })


            //----------------------------------------------------
            // 🟦 APTO PARA CONSUMO
            //----------------------------------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.medium)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Apto para consumo", fontWeight = FontWeight.SemiBold)
                    Text("Depende del peso y tratamientos.", style = MaterialTheme.typography.bodySmall)
                }
                Switch(checked = aptoConsumo, onCheckedChange = { aptoConsumo = it })
            }


            //----------------------------------------------------
            // 🟦 BOTÓN GUARDAR
            //----------------------------------------------------
            var showSuccessDialog by remember { mutableStateOf(false) }

            Button(
                onClick = {

                    if (isSaving) return@Button

                    if (!isEditing && (id.isBlank() || nombre.isBlank() || raza.isBlank() || sexo.isBlank())) {
                        scope.launch {
                            snackbarHost.showSnackbar("⚠️ Completa ID, Nombre, Raza y Sexo.")
                        }
                        return@Button
                    }

                    isSaving = true

                    // referencia correcta
                    val ref = if (isEditing) {
                        database.child(animalId)
                    } else {
                        val newKey = database.push().key ?: return@Button
                        database.child(newKey)
                    }

                    val animalActualizado = Animal(
                        id = if (isEditing) animalId else ref.key ?: "",
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
                        aptoConsumo = aptoConsumo,
                        observaciones = observaciones,
                        propietarioId = when (userRole) {
                            "administrador" -> propietarioId
                            "propietario" -> userId
                            else -> propietarioId
                        },
                        activo = true
                    )


                    // ⬇ GUARDAR FIREBASE
                    ref.setValue(animalActualizado).addOnCompleteListener { task ->
                        isSaving = false

                        scope.launch {
                            if (task.isSuccessful) {
                                showSuccessDialog = true
                            } else {
                                snackbarHost.showSnackbar("❌ Error: ${task.exception?.message}")
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving
            ) {
                Text(if (isEditing) "Actualizar Animal" else "Guardar Animal")
            }


            //----------------------------------------------------
            // 🟦 DIÁLOGO ÉXITO
            //----------------------------------------------------
            if (showSuccessDialog) {
                AlertDialog(
                    onDismissRequest = { showSuccessDialog = false },
                    confirmButton = {
                        TextButton(onClick = {
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("update_message", "✅ $nombre actualizado correctamente")
                            showSuccessDialog = false
                            navController.popBackStack()
                        }) {
                            Text("Aceptar", color = MaterialTheme.colorScheme.primary)
                        }
                    },
                    icon = {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Éxito", tint = MaterialTheme.colorScheme.primary)
                    },
                    title = { Text(if (isEditing) "Actualización exitosa" else "Registro guardado", fontWeight = FontWeight.Bold) },
                    text = { Text(if (isEditing) "Datos actualizados." else "Animal registrado correctamente.") },
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}



//--------------------------------------------------------------
// 🟦 COMPONENTES REUTILIZABLES
//--------------------------------------------------------------
@Composable
fun DropdownField(
    label: String,
    value: String,
    options: List<String>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSelect: (String) -> Unit
) {
    ExposedDropdownMenuBox(expanded, onExpandedChange) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = {
                    onSelect(option)
                    onExpandedChange(false)
                })
            }
        }
    }
}


@Composable
fun DateField(label: String, value: String, onPick: (String) -> Unit) {
    val context = LocalContext.current

    fun abrirDatePicker() {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, y, m, d -> onPick("%02d/%02d/%04d".format(d, m + 1, y)) },
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
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


