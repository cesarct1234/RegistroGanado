package com.caycedo.registroganado.ui_compose.screens.users

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.caycedo.registroganado.ui.compose.nav.NavRoutes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(navController: NavController) {

    val dbRef = FirebaseDatabase.getInstance().getReference("usuarios")
    val auth = FirebaseAuth.getInstance()

    var usuarios by remember { mutableStateOf(listOf<UserData>()) }
    var showApprovalDialog by remember { mutableStateOf<UserData?>(null) }
    var showEditDialog by remember { mutableStateOf<UserData?>(null) }
    var showDisableDialog by remember { mutableStateOf<UserData?>(null) }
    var showDeleteDialog by remember { mutableStateOf<UserData?>(null) }

    LaunchedEffect(true) {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<UserData>()
                for (child in snapshot.children) {
                    val id = child.child("id").value?.toString() ?: ""
                    val nombre = child.child("nombre").value?.toString() ?: ""
                    val email = child.child("email").value?.toString() ?: ""
                    val rol = child.child("rol").value?.toString() ?: "pendiente"
                    val activo = child.child("activo").getValue(Boolean::class.java) ?: false
                    if (id.isNotEmpty()) {
                        list.add(UserData(id, nombre, email, rol, activo))
                    }
                }
                usuarios = list
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Gestión de Usuarios", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFA8D89F)
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFE8F5E9))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            SectionHeader("Pendientes por aprobación")

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(usuarios.filter { it.rol == "pendiente" }) { user ->
                    UserCard(
                        user = user,
                        color = Color(0xFFFFEB3B),
                        showApprovalDialog = { showApprovalDialog = it },
                        showEditDialog = { showEditDialog = it },
                        showDisableDialog = { showDisableDialog = it },
                        showDeleteDialog = { showDeleteDialog = it }
                    )
                }
            }

            Divider()

            SectionHeader("Usuarios activos")

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(usuarios.filter { it.rol != "pendiente" }) { user ->
                    UserCard(
                        user = user,
                        color = Color(0xFFB2FFC3),
                        showApprovalDialog = { showApprovalDialog = it },
                        showEditDialog = { showEditDialog = it },
                        showDisableDialog = { showDisableDialog = it },
                        showDeleteDialog = { showDeleteDialog = it }
                    )
                }
            }
        }
    }

    if (showApprovalDialog != null) {
        ApproveUserDialog(showApprovalDialog!!) { showApprovalDialog = null }
    }

    if (showEditDialog != null) {
        EditUserDialog(showEditDialog!!) { showEditDialog = null }
    }

    if (showDisableDialog != null) {
        DisableUserDialog(showDisableDialog!!) { showDisableDialog = null }
    }

    if (showDeleteDialog != null) {
        DeleteUserDialog(showDeleteDialog!!) { showDeleteDialog = null }
    }
}

data class UserData(
    val id: String,
    val nombre: String,
    val email: String,
    val rol: String,
    val activo: Boolean
)

@Composable
fun UserCard(
    user: UserData,
    color: Color,
    showApprovalDialog: (UserData) -> Unit,
    showEditDialog: (UserData) -> Unit,
    showDisableDialog: (UserData) -> Unit,
    showDeleteDialog: (UserData) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp),
        onClick = {}
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(user.nombre, fontSize = 18.sp, fontWeight = FontWeight.Bold)

            Text(
                user.email,
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AssistChip(
                    onClick = {},
                    label = { Text(user.rol.uppercase()) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = color
                    )
                )

                AssistChip(
                    onClick = {},
                    label = { Text(if (user.activo) "ACTIVO" else "INACTIVO") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (user.activo) Color(0xFFB2FFC3) else Color(0xFFFFCDD2)
                    )
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                if (user.rol == "pendiente") {
                    Button(
                        onClick = { showApprovalDialog(user) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) { Text("Aprobar") }
                }

                OutlinedButton(onClick = { showEditDialog(user) }) {
                    Text("Editar")
                }

                OutlinedButton(
                    onClick = { showDisableDialog(user) },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (user.activo) Color.Red else Color(0xFF4CAF50)
                    )
                ) {
                    Text(if (user.activo) "Desactivar" else "Activar")
                }

                OutlinedButton(
                    onClick = { showDeleteDialog(user) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                ) { Text("Eliminar") }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF2E7D32)
    )
}

@Composable
fun ApproveUserDialog(user: UserData, onDismiss: () -> Unit) {
    val dbRef = FirebaseDatabase.getInstance().getReference("usuarios")
    var rol by remember { mutableStateOf("propietario") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Aprobar Usuario") },
        text = {
            Column {
                Text("Selecciona el rol del usuario")
                Spacer(Modifier.height(10.dp))

                val roles = listOf("administrador", "veterinario", "cuidador", "propietario")

                roles.forEach {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = rol == it,
                            onClick = { rol = it }
                        )
                        Text(it.uppercase())
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    dbRef.child(user.id).child("rol").setValue(rol)
                    dbRef.child(user.id).child("activo").setValue(true)
                    onDismiss()
                }
            ) { Text("Aprobar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun EditUserDialog(user: UserData, onDismiss: () -> Unit) {
    val dbRef = FirebaseDatabase.getInstance().getReference("usuarios")
    var nombre by remember { mutableStateOf(user.nombre) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Usuario") },
        text = {
            Column {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    dbRef.child(user.id).child("nombre").setValue(nombre)
                    onDismiss()
                }
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun DisableUserDialog(user: UserData, onDismiss: () -> Unit) {
    val dbRef = FirebaseDatabase.getInstance().getReference("usuarios")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (user.activo) "Desactivar Usuario" else "Activar Usuario") },
        text = {
            Text(
                if (user.activo)
                    "El usuario no podrá iniciar sesión hasta ser activado."
                else
                    "El usuario podrá ingresar nuevamente."
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    dbRef.child(user.id).child("activo").setValue(!user.activo)
                    onDismiss()
                }
            ) { Text(if (user.activo) "Desactivar" else "Activar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun DeleteUserDialog(user: UserData, onDismiss: () -> Unit) {
    val dbRef = FirebaseDatabase.getInstance().getReference("usuarios")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Eliminar Usuario") },
        text = { Text("Esta acción no se puede deshacer.") },
        confirmButton = {
            Button(
                onClick = {
                    dbRef.child(user.id).removeValue()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) { Text("Eliminar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}


