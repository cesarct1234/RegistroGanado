package com.caycedo.registroganado.ui_compose.screens.roles

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.caycedo.registroganado.R
import com.caycedo.registroganado.ui.compose.nav.NavRoutes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import androidx.compose.ui.draw.clip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(navController: NavController) {

    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val dbRef = FirebaseDatabase.getInstance().getReference("usuarios")

    var userName by remember { mutableStateOf("Administrador") }
    var userEmail by remember { mutableStateOf(currentUser?.email ?: "") }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Cargar nombre del usuario
    LaunchedEffect(Unit) {
        currentUser?.uid?.let { uid ->
            dbRef.child(uid).get().addOnSuccessListener { snapshot ->
                val nombre = snapshot.child("nombre").value?.toString()
                if (!nombre.isNullOrEmpty()) {
                    userName = nombre
                }
            }
        }
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            "Panel Administrador",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Bienvenido, $userName",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(
                            Icons.Default.ExitToApp,
                            contentDescription = "Cerrar sesión",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = Color(0xFFA8D89F),
                    scrolledContainerColor = Color(0xFF8BC34A)
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFE8F5E9))
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Card de bienvenida con imagen
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFC8E6C9)
                ),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Gestión Ganadera",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            userEmail,
                            fontSize = 13.sp,
                            color = Color(0xFF558B2F)
                        )
                    }
                    Image(
                        painter = painterResource(id = R.drawable.imglogin),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(MaterialTheme.shapes.medium)
                    )
                }
            }

            // Sección: Gestión Principal
            SectionHeader("Gestión Principal")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AdminItemCompact(
                    title = "Usuarios",
                    icon = Icons.Default.Group,
                    color = Color(0xFF1976D2),
                    modifier = Modifier.weight(1f)
                ) {
                    navController.navigate(NavRoutes.USERS_MANAGEMENT)
                }

                AdminItemCompact(
                    title = "Animales",
                    icon = Icons.Default.Pets,
                    color = Color(0xFFFF6F00),
                    modifier = Modifier.weight(1f)
                ) {
                    navController.navigate(NavRoutes.LIST_ANIMALS)
                }
            }

            AdminItem(
                title = "Registro de Insumos",
                description = "Control de inventario y suministros",
                icon = Icons.Default.Inventory,
                color = Color(0xFF7B1FA2)
            ) {
                navController.navigate(NavRoutes.SUPPLIES)
            }

            // Sección: Reportes y Análisis
            SectionHeader("Reportes y Análisis")

            AdminItem(
                title = "Reportes Estadísticos",
                description = "Visualiza producción y estadísticas",
                icon = Icons.Default.BarChart,
                color = Color(0xFF0288D1)
            ) {
                navController.navigate(NavRoutes.REPORTS)
            }

            AdminItem(
                title = "Exportar a PDF",
                description = "Genera reportes completos en PDF",
                icon = Icons.Default.PictureAsPdf,
                color = Color(0xFFD32F2F)
            ) {
                navController.navigate(NavRoutes.REPORTS_EXPORT)
            }

            // Sección: Herramientas
            SectionHeader("Herramientas")

            AdminItem(
                title = "Importar desde Excel",
                description = "Carga masiva de animales",
                icon = Icons.Default.Upload,
                color = Color(0xFF388E3C)
            ) {
                navController.navigate(NavRoutes.EXCEL_IMPORT)
            }

            // Espaciado final
            Spacer(Modifier.height(16.dp))
        }
    }

    // Diálogo de confirmación de cierre de sesión
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = {
                Icon(
                    Icons.Default.Logout,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    "Cerrar Sesión",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text("¿Estás seguro de que deseas cerrar sesión?")
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Usuario: $userName",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Cerrar Sesión")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun SectionHeader(title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E7D32),
            modifier = Modifier.padding(end = 8.dp)
        )
        Divider(
            modifier = Modifier.weight(1f),
            color = Color(0xFF81C784),
            thickness = 2.dp
        )
    }
}

@Composable
fun AdminItem(
    title: String,
    description: String,
    icon: ImageVector,
    color: Color = Color(0xFF2E7D32),
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = {
            isPressed = true
            onClick()
        },
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isPressed) 8.dp else 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                color = color.copy(alpha = 0.15f),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.size(56.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}

@Composable
fun AdminItemCompact(
    title: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(110.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = color.copy(alpha = 0.15f),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.size(48.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Text(
                title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}


