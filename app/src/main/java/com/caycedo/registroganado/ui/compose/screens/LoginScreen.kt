package com.caycedo.registroganado.ui_compose.screens

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.caycedo.registroganado.ui.compose.nav.NavRoutes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {

    val auth = FirebaseAuth.getInstance()
    val dbRef = FirebaseDatabase.getInstance().getReference("usuarios")

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Iniciar Sesión", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                leadingIcon = { Icon(Icons.Default.MailOutline, null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                leadingIcon = { Icon(Icons.Default.Lock, null) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 🔵 BOTÓN SIN RIPPLE (IMPORTANTE)
            Button(
                onClick = {

                    if (email.isEmpty() || password.isEmpty()) {
                        message = "Completa todos los campos"
                        return@Button
                    }

                    isLoading = true

                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->

                            if (task.isSuccessful) {

                                val uid = auth.currentUser?.uid ?: return@addOnCompleteListener

                                dbRef.child(uid).get()
                                    .addOnSuccessListener { snap ->

                                        val rol = snap.child("rol").value?.toString() ?: "pendiente"
                                        val activo = snap.child("activo").value as? Boolean ?: false

                                        if (!activo) {
                                            message = "Tu usuario está deshabilitado. Contacte al administrador."
                                            isLoading = false
                                            return@addOnSuccessListener
                                        }

                                        when (rol) {
                                            "administrador" -> navController.navigate(NavRoutes.ADMIN_HOME)
                                            "veterinario" -> navController.navigate(NavRoutes.VET_HOME)
                                            "cuidador" -> navController.navigate(NavRoutes.CUIDADOR_HOME)
                                            "propietario" -> navController.navigate(NavRoutes.PROP_HOME)
                                            else -> message = "Rol desconocido: $rol"
                                        }

                                    }.addOnFailureListener {
                                        message = "Error al obtener datos del usuario"
                                    }

                            } else {
                                message = "Error: ${task.exception?.message}"
                            }

                            isLoading = false
                        }

                },
                enabled = !isLoading,
                interactionSource = remember { MutableInteractionSource() }, // <- evita ripple
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isLoading) "Cargando..." else "Entrar")
            }

            // 🔵 TextButton también sin ripple
            TextButton(
                onClick = { navController.navigate(NavRoutes.REGISTER) },
                interactionSource = remember { MutableInteractionSource() }
            ) {
                Text("¿No tienes cuenta? Regístrate")
            }

            if (message.isNotEmpty()) {
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
        }
    }
}


