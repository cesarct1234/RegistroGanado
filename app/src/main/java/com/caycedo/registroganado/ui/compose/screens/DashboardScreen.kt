package com.caycedo.registroganado.ui.compose.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.caycedo.registroganado.R
import com.caycedo.registroganado.ui.compose.nav.NavRoutes
import com.google.firebase.auth.FirebaseAuth






@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Panel de Control", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = {
                        auth.signOut()
                        navController.navigate(NavRoutes.LOGIN) {
                            popUpTo(NavRoutes.DASHBOARD) { inclusive = true }
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Bienvenido, ${user?.email ?: "Ganadero"} 👋",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Image(
                painter = painterResource(id = R.drawable.imagen),
                contentDescription = "Imagen principal",
                modifier = Modifier
                    .height(160.dp)
                    .fillMaxWidth()
            )

            // Tarjetas de navegación
            DashboardCard(
                title = "Gestión de Animales",
                icon = Icons.Default.Pets,
                color = Color(0xFF4CAF50)

            ) {
                navController.navigate(NavRoutes.LIST_ANIMALS)
            }

            DashboardCard(
                title = "Registro de Insumos",
                icon = Icons.Default.Grass,
                color = Color(0xFF8BC34A)
            ) {
                // 🔜 Próximamente: Pantalla de insumos
                navController.navigate(com.caycedo.registroganado.ui.compose.nav.NavRoutes.SUPPLIES)
            }
            DashboardCard(
                title = "Registro de Producción",
                icon = Icons.Default.BarChart,
                color = Color(0xFFFFC107) // Amarillo dorado para destacar
            ) {
                navController.navigate(NavRoutes.PRODUCTIONS)
            }

            DashboardCard(
                title = "Reportes y Estadísticas",
                icon = Icons.Default.BarChart,
                color = Color(0xFF03A9F4)
            ) {
                navController.navigate(NavRoutes.REPORTS)
            }
        }
    }
}

@Composable
fun DashboardCard(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            // ✅ Solución: usar el nuevo clic sin ripple conflictivo
            .clickable(
                indication = null, // evita PlatformRipple crash
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            ) { onClick() },
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(6.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = color
            )
        }
    }
}

