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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.caycedo.registroganado.ui.compose.nav.NavRoutes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VetHomeScreen(navController: NavController) {

    val auth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid ?: ""
    val dbUser = FirebaseDatabase.getInstance().getReference("usuarios")

    var nombre by remember { mutableStateOf("Veterinario") }
    var email by remember { mutableStateOf("") }
    var showLogout by remember { mutableStateOf(false) }

    // CARGAR DATOS DEL VET
    LaunchedEffect(uid) {
        if (uid.isNotEmpty()) {
            dbUser.child(uid).get().addOnSuccessListener { snap ->
                nombre = snap.child("nombre").value?.toString() ?: "Veterinario"
                email = snap.child("email").value?.toString() ?: ""
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Panel Veterinario", fontSize = 20.sp)
                        Text(nombre, fontSize = 13.sp, color = Color.Gray)
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
                .background(Color(0xFFE8F5E9))
                .padding(pad)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // 🔷 Sección animales
            Text("Gestión Clínica", fontSize = 18.sp, fontWeight = FontWeight.Bold)

            VetItem(
                title = "Animales Asignados",
                description = "Ver animales activos en la finca",
                icon = Icons.Default.Pets
            ) {
                navController.navigate(NavRoutes.LIST_ANIMALS)
            }

            VetItem(
                title = "Historial Clínico",
                description = "Vacunas, tratamientos, diagnósticos",
                icon = Icons.Default.Healing
            ) {
                navController.navigate(NavRoutes.LIST_ANIMALS)
            }

            VetItem(
                title = "Registrar Diagnóstico",
                description = "Reportar enfermedades o hallazgos",
                icon = Icons.Default.MedicalServices
            ) {
                navController.navigate(NavRoutes.LIST_ANIMALS)
            }

            VetItem(
                title = "Registrar Vacunas",
                description = "Aplicación y control sanitario",
                icon = Icons.Default.Vaccines
            ) {
                navController.navigate(NavRoutes.LIST_ANIMALS)
            }

            VetItem(
                title = "Registrar Peso",
                description = "Control de crecimiento",
                icon = Icons.Default.MonitorWeight
            ) {
                navController.navigate(NavRoutes.LIST_ANIMALS)
            }

            VetItem(
                title = "Alertas Sanitarias",
                description = "Emergencias, brotes, síntomas",
                icon = Icons.Default.Warning
            ) {
                navController.navigate(NavRoutes.LIST_ANIMALS)
            }

        }
    }

    // 🔴 Diálogo cerrar sesión
    if (showLogout) {
        AlertDialog(
            onDismissRequest = { showLogout = false },
            title = { Text("Cerrar Sesión") },
            text = { Text("¿Deseas cerrar sesión?") },
            confirmButton = {
                Button(onClick = {
                    auth.signOut()
                    navController.navigate(NavRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }) {
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
fun VetItem(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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

            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )

            Column {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(description, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}



