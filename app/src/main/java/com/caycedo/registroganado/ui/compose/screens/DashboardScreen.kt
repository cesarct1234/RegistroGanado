package com.caycedo.registroganado.ui.compose.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.MonitorWeight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
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
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFF8FBFF), Color(0xFFEAF3FA))
                    )
                )
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
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

            // 🔹 SECCIÓN GESTIÓN
            Text("📋 Gestión", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            DashboardCard(
                title = "Registro de Animales",
                icon = Icons.Default.Pets,
                colors = listOf(Color(0xFFA5D6A7), Color(0xFFC8E6C9))
            ) { navController.navigate(NavRoutes.LIST_ANIMALS) }

            DashboardCard(
                title = "Registro de Insumos",
                icon = Icons.Default.Grass,
                colors = listOf(Color(0xFFCDDC39), Color(0xFFE6EE9C))
            ) { navController.navigate(NavRoutes.SUPPLIES) }

            DashboardCard(
                title = "Registro de Producción",
                icon = Icons.Outlined.MonitorWeight,
                colors = listOf(Color(0xFFFFF176), Color(0xFFFFF59D))
            ) { navController.navigate(NavRoutes.PRODUCTIONS) }

            // 🔹 SECCIÓN REPORTES
            Text("📈 Reportes", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            DashboardCard(
                title = "Reportes Generales",
                icon = Icons.Outlined.PieChart,
                colors = listOf(Color(0xFF81D4FA), Color(0xFFB3E5FC))
            ) { navController.navigate(NavRoutes.REPORTS) }

            DashboardCard(
                title = "Reportes de Producción",
                icon = Icons.Outlined.MonitorWeight,
                colors = listOf(Color(0xFF4DD0E1), Color(0xFFB2EBF2))
            ) { navController.navigate(NavRoutes.REPORTS_PRODUCTION) }

            DashboardCard(
                title = "Exportar Reportes",
                icon = Icons.Outlined.MonitorWeight,
                colors = listOf(Color(0xFF6A1B9A), Color(0xFFB2EBF2))
            ) {  navController.navigate(NavRoutes.REPORTS_EXPORT)}
        }
    }
}

@Composable
fun DashboardCard(
    title: String,
    icon: ImageVector,
    colors: List<Color>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(65.dp)
            .clickable(
                indication = null,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            ) { onClick() },
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Box(
            modifier = Modifier
                .background(Brush.linearGradient(colors))
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = title,
                    tint = Color(0xFF1A237E),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A237E)
                    )
                )
            }
        }
    }
}

