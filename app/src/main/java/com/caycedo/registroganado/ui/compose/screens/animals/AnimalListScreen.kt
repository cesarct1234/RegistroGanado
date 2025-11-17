package com.caycedo.registroganado.ui.compose.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.caycedo.registroganado.ui.compose.nav.NavRoutes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimalListScreen(navController: NavController) {

    val auth = FirebaseAuth.getInstance()
    val myUid = auth.currentUser?.uid ?: return
    val context = LocalContext.current

    val dbUsers = FirebaseDatabase.getInstance().getReference("usuarios")
    val dbAnimals = FirebaseDatabase.getInstance().getReference("animales")

    var myRole by remember { mutableStateOf("") }
    var listaAnimales by remember { mutableStateOf(listOf<Animal>()) }
    var filtro by remember { mutableStateOf("Activos") }

    // Obtener rol del usuario
    LaunchedEffect(Unit) {
        dbUsers.child(myUid).get().addOnSuccessListener { snap ->
            myRole = snap.child("rol").value.toString()
        }
    }

    // Cargar animales
    LaunchedEffect(myRole, filtro) {

        if (myRole.isEmpty()) return@LaunchedEffect

        val temp = mutableListOf<Animal>()

        dbAnimals.get().addOnSuccessListener { snap ->
            for (animalNode in snap.children) {
                val a = animalNode.getValue(Animal::class.java) ?: continue

                // Filtrado por rol
                when (myRole) {
                    "administrador" -> {
                        when (filtro) {
                            "Activos" -> if (a.activo) temp.add(a)
                            "Inactivos" -> if (!a.activo) temp.add(a)
                            else -> temp.add(a)
                        }
                    }
                    "propietario" -> {
                        if (a.propietarioId == myUid) {
                            when (filtro) {
                                "Activos" -> if (a.activo) temp.add(a)
                                "Inactivos" -> if (!a.activo) temp.add(a)
                                else -> temp.add(a)
                            }
                        }
                    }
                    "veterinario", "cuidador" -> {
                        if (a.activo) temp.add(a)
                    }
                }
            }

            listaAnimales = temp
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Animales", fontWeight = FontWeight.Bold) },
                actions = {
                    if (myRole != "cuidador" && myRole != "veterinario") {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { navController.navigate(NavRoutes.ADD_ANIMAL) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Agregar")
                        }
                    }
                }
            )
        }
    ) { pad ->

        Column(
            modifier = Modifier
                .padding(pad)
                .padding(12.dp)
                .fillMaxSize()
        ) {

            // Chips de filtro
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf("Activos", "Inactivos", "Todos").forEach { item ->
                    FilterChip(
                        selected = filtro == item,
                        onClick = { filtro = item },
                        label = { Text(item) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (listaAnimales.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay animales registrados")
                }
            } else {

                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(listaAnimales) { a ->
                        AnimalCard(a, myRole, navController)
                    }
                }
            }
        }
    }
}

@Composable
fun AnimalCard(
    animal: Animal,
    role: String,
    navController: NavController
) {
    val context = LocalContext.current
    val dbAnimals = FirebaseDatabase.getInstance().getReference("animales")

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor =
                if (animal.activo) MaterialTheme.colorScheme.surfaceVariant
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(Modifier.padding(14.dp)) {

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Column {
                    Text("🐮 ${animal.nombre}", fontWeight = FontWeight.Bold)
                    if (!animal.activo) {
                        Text("(INACTIVO)", color = MaterialTheme.colorScheme.error)
                    }
                }

                Row {

                    // EDITAR
                    if (animal.activo && (role == "administrador" || role == "propietario")) {

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    navController.navigate("${NavRoutes.ADD_ANIMAL}/${animal.propietarioId}/${animal.id}")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar")
                        }
                    }

                    // ACTIVAR / DESACTIVAR
                    if (role == "administrador" || role == "propietario") {

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    val nuevoValor = !animal.activo
                                    dbAnimals.child(animal.id).child("activo").setValue(nuevoValor)

                                    Toast.makeText(
                                        context,
                                        if (nuevoValor) "Animal activado" else "Animal desactivado",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (animal.activo) Icons.Default.Block else Icons.Default.Check,
                                contentDescription = "Cambiar estado"
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text("Raza: ${animal.raza}")
            Text("Sexo: ${animal.sexo}")
            Text("Producción: ${animal.tipoProduccion}")
            Text("Peso: ${animal.peso} kg")
        }
    }
}


