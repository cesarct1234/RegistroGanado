package com.caycedo.registroganado.ui_compose.screens.users

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.caycedo.registroganado.R
import com.caycedo.registroganado.ui.compose.nav.NavRoutes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateUserScreen(navController: NavController) {

    val auth = FirebaseAuth.getInstance()
    val dbRef = FirebaseDatabase.getInstance().getReference("usuarios")

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    val backgroundColor = Color(0xFFE8F5E9)
    val rolesPermitidos = listOf("Propietario", "Veterinario", "Cuidador")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Registrar Usuario", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFA8D89F)
                )
            )
        },
        containerColor = backgroundColor
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(backgroundColor)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Imagen superior estilo Login
            Card(
                modifier = Modifier
                    .size(180.dp)
                    .padding(bottom = 20.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Image(
                    painter = painterResource(id = R.drawable.imglogin),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Text(
                "Crear nuevo usuario",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Color(0xFF2E7D32)
            )

            Spacer(Modifier.height(4.dp))

            Text(
                "Completa la información para registrar un usuario",
                fontSize = 14.sp,
                color = Color(0xFF558B2F)
            )

            Spacer(Modifier.height(24.dp))

            // NOMBRE
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre completo") },
                leadingIcon = { Icon(Icons.Default.Person, null, tint = Color(0xFF2E7D32)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2E7D32),
                    focusedLabelColor = Color(0xFF2E7D32)
                )
            )

            Spacer(Modifier.height(16.dp))

            // EMAIL
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                leadingIcon = { Icon(Icons.Default.Email, null, tint = Color(0xFF2E7D32)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2E7D32),
                    focusedLabelColor = Color(0xFF2E7D32)
                )
            )

            Spacer(Modifier.height(16.dp))

            // ROL
            var expanded by remember { mutableStateOf(false) }

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = selectedRole,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Rol del usuario") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2E7D32),
                        focusedLabelColor = Color(0xFF2E7D32)
                    )
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    rolesPermitidos.forEach { rol ->
                        DropdownMenuItem(
                            text = { Text(rol) },
                            onClick = {
                                selectedRole = rol
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // MENSAJE
            if (message.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (message.startsWith("❌"))
                            MaterialTheme.colorScheme.errorContainer
                        else Color(0xFFD0F2CD)
                    )
                ) {
                    Text(
                        message,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 14.sp,
                        color = if (message.startsWith("❌"))
                            MaterialTheme.colorScheme.onErrorContainer
                        else Color(0xFF2E7D32)
                    )
                }
                Spacer(Modifier.height(12.dp))
            }

            // BOTÓN REGISTRAR
            Button(
                onClick = {

                    if (name.isEmpty() || email.isEmpty() || selectedRole.isEmpty()) {
                        message = "❌ Completa todos los campos"
                        return@Button
                    }

                    isLoading = true
                    message = ""

                    auth.createUserWithEmailAndPassword(email, "temporal123")
                        .addOnSuccessListener { result ->

                            val uid = result.user?.uid ?: return@addOnSuccessListener

                            val userData = mapOf(
                                "uid" to uid,
                                "nombre" to name,
                                "email" to email,
                                "rol" to selectedRole.lowercase(),
                                "activo" to true
                            )

                            dbRef.child(uid).setValue(userData)

                            auth.sendPasswordResetEmail(email)

                            message = "✔️ Usuario creado correctamente"
                            isLoading = false
                        }
                        .addOnFailureListener {
                            message = "❌ Error: ${it.message}"
                            isLoading = false
                        }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2E7D32)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Registrar usuario")
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

