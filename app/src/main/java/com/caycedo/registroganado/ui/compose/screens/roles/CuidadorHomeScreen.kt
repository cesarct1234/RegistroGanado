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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.caycedo.registroganado.ui.compose.nav.NavRoutes
import com.google.firebase.auth.FirebaseAuth

@Composable
fun CuidadorHomeScreen(navController: NavController, cuidadorId: String) {

    val auth = FirebaseAuth.getInstance()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Panel Cuidador") },
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

            CuidadorItem(
                title = "Animales Activos",
                description = "Ver todos los animales en producción",
                icon = Icons.Default.Pets
            ) {
                navController.navigate(NavRoutes.LIST_ANIMALS)
            }

            CuidadorItem(
                title = "Registro Alimentación",
                description = "Registrar comida suministrada",
                icon = Icons.Default.Restaurant
            ) {
                navController.navigate(NavRoutes.LIST_ANIMALS)
            }

            CuidadorItem(
                title = "Actividades y Aseo",
                description = "Tareas de mantenimiento",
                icon = Icons.Default.Checklist
            ) {
                navController.navigate(NavRoutes.LIST_ANIMALS)
            }
        }
    }
}

@Composable
fun CuidadorItem(
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            Icon(icon, contentDescription = null, modifier = Modifier.size(40.dp))

            Column {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(description, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

