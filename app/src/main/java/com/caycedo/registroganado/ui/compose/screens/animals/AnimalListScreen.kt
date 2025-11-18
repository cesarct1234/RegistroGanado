package com.caycedo.registroganado.ui.compose.screens.animals

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import com.caycedo.registroganado.ui.compose.nav.NavRoutes
import com.caycedo.registroganado.ui.compose.screens.utils.ExcelUtility
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimalListScreen(navController: NavController) {

    val auth = FirebaseAuth.getInstance()
    val myUid = auth.currentUser?.uid ?: return
    val context = LocalContext.current

    val dbUsers = FirebaseDatabase.getInstance().getReference("usuarios")
    val dbAnimals = FirebaseDatabase.getInstance().getReference("animales_global")


    var myRole by remember { mutableStateOf("") }
    var lista by remember { mutableStateOf(listOf<Animal>()) }
    var filtro by remember { mutableStateOf("Activos") }

    // Obtener rol
    LaunchedEffect(Unit) {
        dbUsers.child(myUid).get().addOnSuccessListener { snap ->
            myRole = snap.child("rol").value.toString()
        }
    }

    // Cargar animales
    LaunchedEffect(myRole, filtro) {
        if (myRole.isEmpty()) return@LaunchedEffect

        dbAnimals.child(myUid).get().addOnSuccessListener { snap ->

            val tmp = mutableListOf<Animal>()

            for (node in snap.children) {
                val a = node.getValue(Animal::class.java) ?: continue

                // 🚀 Mostrar TODOS los animales al administrador, sin filtro por propietario
                when (myRole) {

                    "administrador" -> {
                        when (filtro) {
                            "Activos" -> if (a.activo) tmp.add(a)
                            "Inactivos" -> if (!a.activo) tmp.add(a)
                            "Todos" -> tmp.add(a)
                        }
                    }

                    "propietario" -> {
                        if (a.propietarioId == myUid || a.propietarioId.isBlank()) {
                            when (filtro) {
                                "Activos" -> if (a.activo) tmp.add(a)
                                "Inactivos" -> if (!a.activo) tmp.add(a)
                                "Todos" -> tmp.add(a)
                            }
                        }
                    }

                    "veterinario", "cuidador" -> {
                        if (a.activo) tmp.add(a)
                    }
                }

            }

            lista = tmp
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Animales", fontWeight = FontWeight.Bold) },
                actions = {
                    if (myRole == "administrador" || myRole == "propietario") {
                        IconButton(onClick = {
                            navController.navigate(NavRoutes.ADD_ANIMAL)
                        }) {
                            Icon(Icons.Default.Add, contentDescription = null)
                        }
                    }
                    IconButton(onClick = {
                        val excel = ExcelUtility.exportarAnimales(context, lista)
                        compartirArchivo(context, excel)
                    }) {
                        Icon(Icons.Default.Download, contentDescription = "Exportar Excel")
                    }

                }
            )
        }
    ) { pad ->

        Column(
            Modifier.padding(pad).padding(12.dp)
        ) {

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                listOf("Activos", "Inactivos", "Todos").forEach {
                    FilterChip(
                        selected = filtro == it,
                        onClick = { filtro = it },
                        label = { Text(it) }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            if (lista.isEmpty()) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No hay animales")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(lista) {
                        AnimalCard(it, myRole, navController)
                    }
                }
            }
        }
    }
}

private fun RowScope.compartirArchivo(context: android.content.Context, excel: java.io.File) {}

@Composable
fun AnimalCard(
    animal: Animal,
    role: String,
    navController: NavController
) {

    val context = LocalContext.current
    val db = FirebaseDatabase.getInstance().getReference("animales")

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            if (animal.activo)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {

        Column(Modifier.padding(12.dp)) {

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {

                Column {
                    Text("🐮 ${animal.nombre}", fontWeight = FontWeight.Bold)
                    Text("Raza: ${animal.raza}")
                    Text("Sexo: ${animal.sexo}")
                    Text("Peso: ${animal.peso} kg")
                    if (!animal.activo) Text("(INACTIVO)", color = MaterialTheme.colorScheme.error)
                }

                Row {

                    // Editar
                    if ((role == "administrador" || role == "propietario") && animal.activo) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier
                                .size(32.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    navController.navigate("${NavRoutes.ADD_ANIMAL}/${animal.propietarioId}/${animal.id}")
                                }
                        )
                    }

                    // Activar / desactivar
                    if (role == "administrador" || role == "propietario") {
                        Icon(
                            if (animal.activo) Icons.Default.Block else Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier
                                .size(32.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    val nuevo = !animal.activo
                                    db.child(animal.id).child("activo").setValue(nuevo)
                                    Toast.makeText(
                                        context,
                                        if (nuevo) "Animal activado" else "Animal desactivado",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        )
                    }
                }
            }
        }
    }
}



