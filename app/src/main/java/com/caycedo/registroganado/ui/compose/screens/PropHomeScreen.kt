package com.caycedo.registroganado.ui_compose.screens.roles

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.caycedo.registroganado.ui.compose.nav.NavRoutes
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropHomeScreen(navController: NavController) {

    val auth = FirebaseAuth.getInstance()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Panel del Propietario") },
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
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            PropCard(
                title = "Mis Animales",
                description = "Ver, consultar y gestionar mis registros",
                icon = Icons.Default.Pets
            ) {
                navController.navigate(NavRoutes.LIST_ANIMALS)
            }

            PropCard(
                title = "Historial Médico",
                description = "Consultar tratamientos, vacunas y evolución",
                icon = Icons.Default.MedicalServices
            ) {
                // navController.navigate("historialMedico")
            }

            PropCard(
                title = "Información General",
                description = "Datos, documentos, recomendaciones",
                icon = Icons.Default.Info
            ) {
                // navController.navigate("infoPropietario")
            }

            PropCard(
                title = "Reportes Propietario",
                description = "Ver producción y datos resumidos",
                icon = Icons.Default.BarChart
            ) {
                navController.navigate(NavRoutes.REPORTS)
            }
        }
    }
}

@Composable
fun PropCard(
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
                indication = null
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
                Text(
                    description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

