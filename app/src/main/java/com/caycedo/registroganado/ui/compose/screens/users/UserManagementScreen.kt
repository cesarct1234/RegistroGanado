package com.caycedo.registroganado.ui.compose.screens.users

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.caycedo.registroganado.ui.compose.nav.NavRoutes
import com.google.firebase.database.*

data class Usuario(
    val id: String = "",
    val nombre: String = "",
    val email: String = "",
    val rol: String = "",
    val activo: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(navController: NavController) {

    val db = FirebaseDatabase.getInstance().getReference("usuarios")
    val context = LocalContext.current

    var usuarios by remember { mutableStateOf(listOf<Usuario>()) }

    fun cargarUsuarios() {
        db.get().addOnSuccessListener { snap ->

            val lista = mutableListOf<Usuario>()

            for (u in snap.children) {

                val map = u.value

                // 🔥 FILTRO REAL: solo aceptar si es un MAPA y contiene "email"
                if (map is Map<*, *> && map.containsKey("email")) {

                    val user = u.getValue(Usuario::class.java)

                    if (user != null && user.id.isNotBlank()) {
                        lista.add(user)
                    }
                }
            }

            usuarios = lista
        }
    }





    LaunchedEffect(Unit) { cargarUsuarios() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Gestión de Usuarios") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { pad ->

        Column(
            Modifier
                .padding(pad)
                .padding(16.dp)
                .fillMaxSize()
        ) {

            Button(
                onClick = { navController.navigate(NavRoutes.CREATE_USER) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50),
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Crear usuario")
            }

            Spacer(Modifier.height(20.dp))

            if (usuarios.isEmpty()) {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay usuarios registrados")
                }
            } else {

                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(usuarios) { user ->

                        UserCard(
                            user = user,
                            navController = navController,
                            onToggleActivo = {
                                val nuevo = !user.activo
                                db.child(user.id).child("activo").setValue(nuevo)
                                cargarUsuarios()

                                Toast.makeText(
                                    context,
                                    if (nuevo) "Usuario activado" else "Usuario desactivado",
                                    Toast.LENGTH_SHORT
                                ).show()

                            },
                            onChangeRol = { nuevoRol ->
                                db.child(user.id).child("rol").setValue(nuevoRol)
                                cargarUsuarios()
                                Toast.makeText(context, "Rol actualizado", Toast.LENGTH_SHORT).show()
                            },
                            onDelete = {
                                db.child(user.id).removeValue()
                                cargarUsuarios()
                                Toast.makeText(context, "Usuario eliminado", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun UserCard(
    user: Usuario,
    navController: NavController,
    onToggleActivo: () -> Unit,
    onChangeRol: (String) -> Unit,
    onDelete: () -> Unit
) {

    var showRoleDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor =
                if (user.activo) MaterialTheme.colorScheme.surfaceVariant
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {

        Column(Modifier.padding(16.dp)) {

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Column {
                    Text(user.nombre, fontWeight = FontWeight.Bold)
                    Text(user.email, color = Color.DarkGray)
                    Text("Rol: ${user.rol}", fontWeight = FontWeight.SemiBold)
                    if (!user.activo) {
                        Text("(INACTIVO)", color = Color.Red)
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    //------------------------------------------------------
                    //  EDITAR
                    //------------------------------------------------------
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Editar usuario",
                        modifier = Modifier
                            .size(32.dp)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                navController.navigate("${NavRoutes.EDIT_USER}/${user.id}")
                            }
                    )

                    //------------------------------------------------------
                    //  CAMBIAR ROL
                    //------------------------------------------------------
                    Icon(
                        Icons.Default.ManageAccounts,
                        contentDescription = "Cambiar rol",
                        modifier = Modifier
                            .size(32.dp)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                showRoleDialog = true
                            }
                    )

                    //------------------------------------------------------
                    //  ACTIVAR / DESACTIVAR
                    //------------------------------------------------------
                    Icon(
                        if (user.activo) Icons.Default.Block else Icons.Default.Check,
                        contentDescription = "Act/Desactivar",
                        modifier = Modifier
                            .size(32.dp)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                onToggleActivo()
                            }
                    )

                    //------------------------------------------------------
                    //  ELIMINAR
                    //------------------------------------------------------
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = Color.Red,
                        modifier = Modifier
                            .size(32.dp)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                onDelete()
                            }
                    )
                }
            }
        }
    }

    //-------------------------------------------------------------------
    //  DIALOG PARA CAMBIAR EL ROL
    //-------------------------------------------------------------------
    if (showRoleDialog) {
        AlertDialog(
            onDismissRequest = { showRoleDialog = false },
            title = { Text("Cambiar rol de usuario") },

            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                    val roles = listOf(
                        "administrador",
                        "propietario",
                        "veterinario",
                        "cuidador"
                    )

                    roles.forEach { rol ->

                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
                                    onChangeRol(rol)
                                    showRoleDialog = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(rol.uppercase())
                        }
                    }
                }
            },

            confirmButton = {
                TextButton(
                    onClick = { showRoleDialog = false }
                ) {
                    Text("Cerrar")
                }
            }
        )
    }
}

