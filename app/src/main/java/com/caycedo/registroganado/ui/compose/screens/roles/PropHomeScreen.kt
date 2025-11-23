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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.caycedo.registroganado.ui.compose.nav.NavRoutes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

@Composable
fun PropHomeScreen(
    navController: NavController,
    propietarioId: String
) {

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseDatabase.getInstance().getReference("usuarios")

    var nombre by remember { mutableStateOf("Propietario") }
    var email by remember { mutableStateOf("") }
    var showLogout by remember { mutableStateOf(false) }

    // Cargar datos del propietario
    LaunchedEffect(propietarioId) {
        db.child(propietarioId).get().addOnSuccessListener { snap ->
            nombre = snap.child("nombre").value?.toString() ?: "Propietario"
            email = snap.child("email").value?.toString() ?: ""
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Panel Propietario", fontSize = 20.sp)
                        Text(
                            nombre,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
                .padding(pad)
                .fillMaxSize()
                .background(Color(0xFFE7F6E7))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // -------- SECCIÓN PRINCIPAL -------
            Text(
                "Gestión General de la Finca",
                fontWeight = MaterialTheme.typography.titleLarge.fontWeight,
                fontSize = 18.sp,
                color = Color(0xFF2E7D32)
            )

            // 🔵 Registrar animal
            HomeButton(
                title = "Registrar Animal",
                description = "Añade un nuevo animal a tu finca",
                icon = Icons.Default.AddCircle
            ) {
                navController.navigate("${NavRoutes.ADD_ANIMAL}/$propietarioId")
            }

            // 🔵 Ver animales del propietario
            HomeButton(
                title = "Ver Mis Animales",
                description = "Animales registrados bajo tu propiedad",
                icon = Icons.Default.Pets
            ) {
                navController.navigate("${NavRoutes.LIST_ANIMALS}?owner=$propietarioId")
            }

            // -------- SECCIÓN FINANZAS E INSUMOS -------
            Text(
                "Inventario y Producción",
                fontWeight = MaterialTheme.typography.titleLarge.fontWeight,
                fontSize = 18.sp,
                color = Color(0xFF1B5E20)
            )

            // 🟢 Insumos — inventario del propietario
            HomeButton(
                title = "Insumos",
                description = "Control de inventario y suministros",
                icon = Icons.Default.Inventory
            ) {
                navController.navigate(NavRoutes.SUPPLIES)
            }

            // 🟢 Reportes completos
            HomeButton(
                title = "Reportes",
                description = "Producción, estadísticas, PDF",
                icon = Icons.Default.BarChart
            ) {
                navController.navigate("${NavRoutes.REPORTS}?owner=$propietarioId")
            }

            // -------- SECCIÓN ADICIONAL -------
            Text(
                "Administración General",
                fontWeight = MaterialTheme.typography.titleLarge.fontWeight,
                fontSize = 18.sp,
                color = Color(0xFF33691E)
            )

            HomeButton(
                title = "PDF de la Finca",
                description = "Reporte completo en PDF",
                icon = Icons.Default.PictureAsPdf
            ) {
                navController.navigate(NavRoutes.REPORTS_EXPORT)
            }
        }
    }

    // -------- DIALOGO CERRAR SESIÓN -------
    if (showLogout) {
        AlertDialog(
            onDismissRequest = { showLogout = false },
            title = { Text("Cerrar Sesión") },
            text = {
                Column {
                    Text("¿Seguro que deseas cerrar sesión?")
                    Spacer(Modifier.height(8.dp))
                    Text(email, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        auth.signOut()
                        navController.navigate(NavRoutes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Cerrar Sesión") }
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
fun HomeButton(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {

    val interaction = remember { MutableInteractionSource() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clickable(
                interactionSource = interaction,
                indication = LocalIndication.current
            ) { onClick() },
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {

            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )

            Column {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(
                    description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
