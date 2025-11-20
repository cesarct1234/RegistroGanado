@file:OptIn(ExperimentalMaterial3Api::class)

package com.caycedo.registroganado.ui_compose.screens.roles

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.caycedo.registroganado.ui.compose.nav.NavRoutes
import com.google.firebase.auth.FirebaseAuth

@Composable
fun PropHomeScreen(
    navController: NavController,
    propietarioId: String
) {

    val auth = FirebaseAuth.getInstance()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Panel Propietario") },
                actions = {
                    IconButton(onClick = {
                        auth.signOut()
                        navController.navigate(NavRoutes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar sesión")
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFE7F6E7))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {

            HomeButton(
                title = "Registrar Animal",
                description = "Añade un nuevo animal a tu finca",
                icon = Icons.Default.AddCircle
            ) {
                navController.navigate("${NavRoutes.ADD_ANIMAL}/$propietarioId")
            }

            HomeButton(
                title = "Ver Mis Animales",
                description = "Consulta todos los animales registrados",
                icon = Icons.Default.Pets
            ) {
                navController.navigate(NavRoutes.LIST_ANIMALS)
            }

            HomeButton(
                title = "Insumos",
                description = "Gestiona el inventario de insumos",
                icon = Icons.Default.Inventory
            ) {
                navController.navigate(NavRoutes.SUPPLIES)
            }

            HomeButton(
                title = "Reportes",
                description = "Producción, estadísticas y PDF",
                icon = Icons.Default.BarChart
            ) {
                navController.navigate(NavRoutes.REPORTS)
            }
        }
    }
}

@Composable
fun HomeButton(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {

    // ⛔ SOLUCIÓN: usar interacción nueva + ripple moderno
    val interaction = remember { MutableInteractionSource() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clickable(
                interactionSource = interaction,
                indication = LocalIndication.current
            ) { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
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
