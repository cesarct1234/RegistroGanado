package com.caycedo.registroganado.ui_compose.screens.roles

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.caycedo.registroganado.R
import com.caycedo.registroganado.ui.compose.nav.NavRoutes
import com.google.firebase.auth.FirebaseAuth






@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(navController: NavController) {


    val auth = FirebaseAuth.getInstance()



    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Panel Administrador") },
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
                .background(Color(0xFFB7E4A5))
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                painter = painterResource(id = R.drawable.imglogin),
                contentDescription = "Imagen decorativa",
                modifier = Modifier
                    .size(180.dp)
                    .padding(top = 10.dp)
            )

            // ======================
            //  MÓDULOS PRINCIPALES
            // ======================

            AdminItem(
                title = "Gestión de Usuarios",
                description = "Crear, editar y administrar roles",
                icon = Icons.Default.Group
            ) {
                navController.navigate(NavRoutes.USERS_MANAGEMENT)
            }

            AdminItem(
                title = "Animales",
                description = "Registrar y gestionar animales",
                icon = Icons.Default.Pets
            ) {
                navController.navigate(NavRoutes.LIST_ANIMALS)
            }

            AdminItem(
                title = "Insumos",
                description = "Control de inventario y suministros",
                icon = Icons.Default.Inventory
            ) {
                navController.navigate(NavRoutes.SUPPLIES)
            }

            AdminItem(
                title = "Reportes",
                description = "Estadísticas, producción y exportación",
                icon = Icons.Default.BarChart
            ) {
                navController.navigate(NavRoutes.REPORTS)
            }

            // ======================
            //  NUEVAS FUNCIONES
            // ======================

            AdminItem(
                title = "Importar desde Excel",
                description = "Cargar animales por lotes",
                icon = Icons.Default.Upload
            ) {
                navController.navigate(NavRoutes.EXCEL_IMPORT)
            }

            AdminItem(
                title = "Exportar PDF",
                description = "Generar reporte completo",
                icon = Icons.Default.PictureAsPdf
            ) {
                navController.navigate(NavRoutes.REPORTS_EXPORT)
            }
        }
    }
}

@Composable
fun AdminItem(
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

