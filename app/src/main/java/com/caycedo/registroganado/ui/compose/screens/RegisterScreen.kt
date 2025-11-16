package com.caycedo.registroganado.ui_compose.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController) {

    val auth = FirebaseAuth.getInstance()
    val dbRef = FirebaseDatabase.getInstance().getReference("usuarios")

    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var rol by remember { mutableStateOf("propietario") }
    var expanded by remember { mutableStateOf(false) }

    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // -----------------------------------------------
    // 🔥 DETECTAR SI ES EL PRIMER USUARIO
    // -----------------------------------------------
    var isFirstUser by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        FirebaseDatabase.getInstance()
            .getReference("usuarios")
            .get()
            .addOnSuccessListener { snap ->
                if (!snap.hasChildren()) {
                    isFirstUser = true
                    rol = "administrador" // asignación automática
                }
            }
    }

    // -----------------------------------------------
    // 🔥 DETECTAR EL ROL DEL USUARIO QUE ESTÁ LOGUEADO
    // -----------------------------------------------
    val currentUserId = auth.currentUser?.uid
    var currentUserRole by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        if (!isFirstUser && currentUserId != null) {
            FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(currentUserId)
                .child("rol")
                .get()
                .addOnSuccessListener {
                    currentUserRole = it.value?.toString() ?: ""
                }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Registro de Usuario", fontWeight = FontWeight.Bold) }
            )
        }
    ) { pad ->

        Column(
            modifier = Modifier
                .padding(pad)
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // 🔹 NOMBRE
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre completo") },
                leadingIcon = { Icon(Icons.Default.Person, null) },
                modifier = Modifier.fillMaxWidth()
            )

            // 🔹 CORREO
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                leadingIcon = { Icon(Icons.Default.MailOutline, null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            )

            // 🔹 CONTRASEÑA
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

            Spacer(Modifier.height(20.dp))

            // ---------------------------------------------------------------------
            // 🔥 SELECTOR DEL ROL
            // ---------------------------------------------------------------------

            when {
                // 1️⃣ PRIMER USUARIO → ADMIN AUTOMÁTICO
                isFirstUser -> {
                    OutlinedTextField(
                        value = "administrador",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Rol asignado automáticamente") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    rol = "administrador"
                }

                // 2️⃣ UN ADMIN ESTÁ REGISTRANDO → puede elegir el rol
                currentUserRole == "administrador" -> {
                    Text("Seleccione Rol", fontWeight = FontWeight.Bold)

                    val roles = listOf("administrador", "propietario", "veterinario", "cuidador")

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = rol,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Rol del usuario") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                            },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            roles.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item.replaceFirstChar { it.uppercase() }) },
                                    onClick = {
                                        rol = item
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // 3️⃣ Cualquier otro usuario → siempre propietario
                else -> {
                    OutlinedTextField(
                        value = "propietario",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Rol asignado") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    rol = "propietario"
                }
            }

            Spacer(Modifier.height(24.dp))

            // ------------------------------------------------
            // 🔘 BOTÓN REGISTRAR
            // ------------------------------------------------
            Button(
                onClick = {
                    if (nombre.isBlank() || email.isBlank() || password.isBlank()) {
                        message = "Completa todos los campos"
                        return@Button
                    }

                    isLoading = true

                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->

                            isLoading = false

                            if (task.isSuccessful) {

                                val userId = auth.currentUser?.uid ?: return@addOnCompleteListener

                                val userData = mapOf(
                                    "id" to userId,
                                    "nombre" to nombre,
                                    "email" to email,
                                    "rol" to rol,
                                    "activo" to true
                                )

                                dbRef.child(userId).setValue(userData)

                                navController.navigate("login") {
                                    popUpTo("register") { inclusive = true }
                                }

                            } else {
                                message = "Error: ${task.exception?.message}"
                            }
                        }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isLoading) "Creando..." else "Registrar")
            }

            if (message.isNotEmpty()) {
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    }
}
