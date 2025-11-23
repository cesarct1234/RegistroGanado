@file:OptIn(ExperimentalMaterial3Api::class)

package com.caycedo.registroganado.ui_compose.screens.roles

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.caycedo.registroganado.ui.compose.nav.NavRoutes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

@Composable
fun CuidadorHomeScreen(navController: NavController, cuidadorId: String) {

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseDatabase.getInstance().getReference("usuarios")

    var nombre by remember { mutableStateOf("Cuidador") }
    var email by remember { mutableStateOf("") }
    var showLogout by remember { mutableStateOf(false) }

    // Cargar datos del cuidador
    LaunchedEffect(cuidadorId) {
        db.child(cuidadorId).get().addOnSuccessListener { snap ->
            nombre = snap.child("nombre").value?.toString() ?: "Cuidador"
            email = snap.child("email").value?.toString() ?: ""
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Panel Cuidador", fontSize = 20.sp)
                        Text(nombre, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    IconButton(onClick = { showLogout = true }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar sesión")
                    }
                }
            )
        }
    ) { pad ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFD8F3DC))
                .padding(pad)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // TÍTULO SECCIÓN
            Text(
                "Gestión Diaria del Ganado",
                fontWeight = MaterialTheme.typography.titleLarge.fontWeight,
                fontSize = 18.sp,
                color = Color(0xFF1B5E20)
            )

            // 🔵 Animales activos
            CuidadorItem(
                title = "Animales Activos",
                description = "Ver animales que requieren atención diaria",
                icon = Icons.Default.Pets
            ) {
                navController.navigate(NavRoutes.LIST_ANIMALS)
            }

            // 🟢 Registro Alimentación
            CuidadorItem(
                title = "Registro Alimentación",
                description = "Registrar comida suministrada por animal",
                icon = Icons.Default.Restaurant
            ) {
                navController.navigate(NavRoutes.LIST_ANIMALS)
            }

            // 🟢 Registro de Agua
            CuidadorItem(
                title = "Suministro de Agua",
                description = "Registrar litros y frecuencia",
                icon = Icons.Default.WaterDrop
            ) {
                navController.navigate(NavRoutes.LIST_ANIMALS)
            }

            // 🟡 Actividades y Aseo
            CuidadorItem(
                title = "Aseo e Instalaciones",
                description = "Limpieza, corrales, mantenimiento",
                icon = Icons.Default.CleaningServices
            ) {
                navController.navigate(NavRoutes.LIST_ANIMALS)
            }

            // 🔴 Reporte de anomalías
            CuidadorItem(
                title = "Reporte de Anomalías",
                description = "Notificar signos extraños o problemas",
                icon = Icons.Default.ReportProblem
            ) {
                navController.navigate(NavRoutes.LIST_ANIMALS)
            }

            // 🔥 Alertas Sanitarias
            CuidadorItem(
                title = "Alertas Sanitarias",
                description = "Notificar emergencias al veterinario",
                icon = Icons.Default.Warning
            ) {
                navController.navigate(NavRoutes.LIST_ANIMALS)
            }
        }
    }

    // Diálogo de cerrar sesión
    if (showLogout) {
        AlertDialog(
            onDismissRequest = { showLogout = false },
            title = { Text("Cerrar Sesión") },
            text = { Text("¿Deseas cerrar sesión?") },
            confirmButton = {
                Button(
                    onClick = {
                        auth.signOut()
                        navController.navigate(NavRoutes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                ) {
                    Text("Cerrar Sesión")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogout = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun CuidadorItem(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current
            ) { onClick() },
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))

            Column {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(description, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
