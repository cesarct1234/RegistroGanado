package com.caycedo.registroganado.ui.compose.screens.users

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.caycedo.registroganado.ui.compose.nav.NavRoutes
import com.google.firebase.database.FirebaseDatabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUserScreen(
    navController: NavController,
    userId: String
) {

    val context = LocalContext.current
    val db = FirebaseDatabase.getInstance().getReference("usuarios")

    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var rol by remember { mutableStateOf("") }
    var activo by remember { mutableStateOf(true) }
    var expanded by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }

    // Cargar datos del usuario

    LaunchedEffect(userId) {
        db.child(userId).get().addOnSuccessListener { snap ->
            nombre = snap.child("nombre").value?.toString() ?: ""
            correo = snap.child("email").value?.toString() ?: ""    // CORRECTO
            rol = snap.child("rol").value?.toString() ?: ""
            activo = snap.child("activo").value as? Boolean ?: true
            loading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Usuario") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { pad ->

        if (loading) {
            Box(
                Modifier
                    .padding(pad)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            Modifier
                .padding(pad)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = correo,
                onValueChange = {},
                label = { Text("Correo (no editable)") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true
            )

            // Selector de rol
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = rol,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Rol") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    listOf("administrador", "propietario", "veterinario", "cuidador")
                        .forEach { r ->
                            DropdownMenuItem(
                                text = { Text(r) },
                                onClick = {
                                    rol = r
                                    expanded = false
                                }
                            )
                        }
                }
            }

            // Activar / desactivar usuario
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Activo")
                Spacer(Modifier.weight(1f))
                Switch(
                    checked = activo,
                    onCheckedChange = { activo = it }
                )
            }

            // Guardar cambios
            Button(
                onClick = {

                    val newUser = mapOf(
                        "id" to userId,
                        "nombre" to nombre,
                        "email" to correo,    // CORRECTO
                        "rol" to rol,
                        "activo" to activo
                    )

                    db.child(userId).setValue(newUser)   // LIMPIA DATOS VIEJOS
                        .addOnSuccessListener {
                            Toast.makeText(context, "Usuario actualizado", Toast.LENGTH_SHORT).show()
                            navController.navigate(NavRoutes.USERS_MANAGEMENT) {
                                popUpTo(NavRoutes.USERS_MANAGEMENT) { inclusive = true }
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Error al actualizar", Toast.LENGTH_SHORT).show()
                        }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar cambios")
            }
        }
    }
}
