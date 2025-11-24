package com.caycedo.registroganado.ui_compose.screens.animals

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.caycedo.registroganado.ui.compose.nav.NavRoutes
import com.caycedo.registroganado.ui.compose.screens.animals.Animal
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimalListScreen(navController: NavController) {

    val dbAnimals = FirebaseDatabase.getInstance().getReference("animales_global")
    val dbUsers = FirebaseDatabase.getInstance().getReference("usuarios")

    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

    var animales by remember { mutableStateOf(listOf<Animal>()) }
    var rol by remember { mutableStateOf<String?>(null) }

    // 1️⃣ Primero obtener el rol del usuario
    LaunchedEffect(uid) {
        dbUsers.child(uid).get()
            .addOnSuccessListener { snap ->
                rol = snap.child("rol").value?.toString()?.lowercase()?.trim()
            }
    }

    // 2️⃣ Mientras rol es null → loader
    if (rol == null) {
        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // 3️⃣ Cargar animales SOLO después de que rol ya está disponible
    LaunchedEffect(rol) {
        dbAnimals.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val lista = mutableListOf<Animal>()

                for (child in snapshot.children) {
                    val animal = child.getValue(Animal::class.java) ?: continue

                    val permitir = when (rol) {
                        "administrador" -> true
                        "veterinario" -> true
                        "cuidador" -> animal.activo == true
                        "propietario" -> animal.propietarioId == uid
                        else -> false
                    }

                    if (permitir) lista.add(animal)
                }

                animales = lista
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Animales Registrados", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                actions = {
                    if (rol == "administrador" || rol == "propietario") {
                        IconButton(onClick = {
                            navController.navigate("${NavRoutes.ADD_ANIMAL}/$uid")
                        }) {
                            Icon(Icons.Default.Add, null)
                        }
                    }
                }
            )
        }
    ) { pad ->

        LazyColumn(
            modifier = Modifier
                .padding(pad)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            items(animales) { animal ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            navController.navigate(
                                "${NavRoutes.EDIT_ANIMAL}/${animal.propietarioId}/${animal.id}"
                            )
                        },
                    elevation = CardDefaults.cardElevation(3.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("🐄 ${animal.nombre}", fontWeight = FontWeight.Bold)
                        Text("Raza: ${animal.raza}")
                        Text("Producción: ${animal.produccionLeche} L/día")
                        Text("ID: ${animal.id}")
                    }
                }
            }
        }
    }
}
