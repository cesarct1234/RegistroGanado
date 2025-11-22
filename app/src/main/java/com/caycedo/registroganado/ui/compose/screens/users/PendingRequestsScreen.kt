package com.caycedo.registroganado.ui_compose.screens.users

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.database.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingRequestsScreen(navController: NavController) {

    val dbRef = FirebaseDatabase.getInstance().getReference("usuarios")
    var users by remember { mutableStateOf(listOf<UserRequest>()) }
    var isLoading by remember { mutableStateOf(true) }

    // Cargar usuarios pendientes
    LaunchedEffect(Unit) {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val temp = mutableListOf<UserRequest>()

                for (child in snapshot.children) {
                    val uid = child.key ?: continue
                    val nombre = child.child("nombre").value?.toString().orEmpty()
                    val email = child.child("email").value?.toString().orEmpty()
                    val rol = child.child("rol").value?.toString().orEmpty()
                    val activo = child.child("activo").value as? Boolean ?: false

                    if (!activo || rol.lowercase() == "pendiente") {
                        temp.add(
                            UserRequest(
                                uid = uid,
                                nombre = nombre,
                                email = email,
                                rol = rol,
                                activo = activo
                            )
                        )
                    }
                }
                users = temp
                isLoading = false
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Solicitudes Pendientes") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF1F8E9))
                .padding(16.dp)
        ) {

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (users.isEmpty()) {
                Text(
                    "No hay solicitudes pendientes",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(20.dp)

                )

                // linea donde se muestra cada usuario pendiente
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(users) { user ->
                        PendingUserCard(user, dbRef)
                    }
                }
            }
        }
    }
}

data class UserRequest(
    val uid: String,
    val nombre: String,
    val email: String,
    val rol: String,
    val activo: Boolean
)

@Composable
fun PendingUserCard(
    user: UserRequest,
    dbRef: DatabaseReference
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8EAF6))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Text(user.nombre, style = MaterialTheme.typography.titleMedium)
            Text(user.email, style = MaterialTheme.typography.bodyMedium)
            Text("Rol solicitado: ${user.rol}")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                // Botón Aprobar solicitudes nuevas

                ElevatedButton(
                    onClick = {
                        dbRef.child(user.uid).updateChildren(
                            mapOf(
                                "activo" to true
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(Color(0xFF2E7D32))
                ) {
                    Icon(Icons.Default.Check, null)
                    Spacer(Modifier.width(6.dp))
                    Text("Aprobar")
                }

                // Botón Asignar rol
                ElevatedButton(
                    onClick = {
                        dbRef.child(user.uid).updateChildren(
                            mapOf("rol" to "propietario") // ← Puedes cambiarlo luego
                        )
                    },
                    colors = ButtonDefaults.buttonColors(Color(0xFF0277BD))
                ) {
                    Icon(Icons.Default.Edit, null)
                    Spacer(Modifier.width(6.dp))
                    Text("Asignar rol")
                }

                // Botón Rechazar
                ElevatedButton(
                    onClick = {
                        dbRef.child(user.uid).removeValue()
                    },
                    colors = ButtonDefaults.buttonColors(Color(0xFFD32F2F))
                ) {
                    Icon(Icons.Default.Delete, null)
                }
            }
        }
    }
}
