package com.caycedo.registroganado.ui.compose.screens.users

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.caycedo.registroganado.ui.compose.nav.NavRoutes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateUserScreen(navController: NavController) {

    val context = LocalContext.current
    val db = FirebaseDatabase.getInstance().reference.child("usuarios")
    val auth = FirebaseAuth.getInstance()

    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var rol by remember { mutableStateOf("propietario") }
    var expanded by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Usuario") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre completo") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = correo,
                onValueChange = { correo = it },
                label = { Text("Correo electrónico") },
                modifier = Modifier.fillMaxWidth()
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
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    label = { Text("Rol del usuario") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                    }
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

            Button(
                enabled = !saving,
                modifier = Modifier.fillMaxWidth(),
                onClick = {

                    if (nombre.isBlank() || correo.isBlank()) {
                        Toast.makeText(context, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    saving = true

                    val tempPass = "Temporal123*"

                    auth.createUserWithEmailAndPassword(correo, tempPass)
                        .addOnSuccessListener { result ->

                            val uid = result.user!!.uid

                            // Guardar CORRECTAMENTE en Firebase
                            val userMap = mapOf(
                                "uid" to uid,
                                "nombre" to nombre,
                                "email" to correo,   // ← CORREGIDO
                                "rol" to rol,
                                "activo" to true
                            )

                            db.child(uid).setValue(userMap)
                                .addOnSuccessListener {

                                    auth.sendPasswordResetEmail(correo)

                                    Toast.makeText(
                                        context,
                                        "Usuario creado. Se envió correo de cambio de contraseña.",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    navController.navigate(NavRoutes.USERS_MANAGEMENT) {
                                        popUpTo(NavRoutes.USERS_MANAGEMENT) { inclusive = true }
                                    }
                                }
                                .addOnFailureListener {
                                    saving = false
                                    Toast.makeText(context, "Error guardando usuario: ${it.message}", Toast.LENGTH_LONG).show()
                                }

                        }
                        .addOnFailureListener {
                            saving = false
                            Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_LONG).show()
                        }
                }
            ) {
                Text("Crear usuario")
            }
        }
    }
}
