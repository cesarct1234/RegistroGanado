package com.caycedo.registroganado.ui_compose.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.caycedo.registroganado.R
import com.caycedo.registroganado.ui.compose.nav.NavRoutes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import androidx.lifecycle.viewmodel.compose.viewModel
import com.caycedo.registroganado.ui.compose.session.SessionViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {

    val auth = FirebaseAuth.getInstance()
    val sessionVM: SessionViewModel = viewModel()
    val dbRef = FirebaseDatabase.getInstance().getReference("usuarios")

    // Estados del formulario
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    // Estados para recuperar contraseña
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }
    var resetMessage by remember { mutableStateOf("") }
    var isResetting by remember { mutableStateOf(false) }

    val backgroundColor = Color(0xFFC7E6B5)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Iniciar Sesión", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFA8D89F)
                )
            )
        },
        containerColor = backgroundColor
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Imagen del logo
            Card(
                modifier = Modifier
                    .size(200.dp)
                    .padding(bottom = 24.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Image(
                    painter = painterResource(id = R.drawable.imginiciarsesion),
                    contentDescription = "Logo del Proyecto",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Text(
                text = "Bienvenido de nuevo",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Ingresa tus credenciales para continuar",
                fontSize = 14.sp,
                color = Color(0xFF558B2F),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Campo de Email
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    message = "" // Limpiar mensaje de error al escribir
                },
                label = { Text("Correo electrónico") },
                leadingIcon = {
                    Icon(Icons.Default.MailOutline, null, tint = Color(0xFF2E7D32))
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2E7D32),
                    focusedLabelColor = Color(0xFF2E7D32)
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Campo de Contraseña con ojito
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    message = ""
                },
                label = { Text("Contraseña") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, null, tint = Color(0xFF2E7D32))
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible)
                                Icons.Default.Visibility
                            else
                                Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible)
                                "Ocultar contraseña"
                            else
                                "Mostrar contraseña",
                            tint = Color(0xFF2E7D32)
                        )
                    }
                },
                visualTransformation = if (passwordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2E7D32),
                    focusedLabelColor = Color(0xFF2E7D32)
                )
            )

            // Botón "Olvidaste tu contraseña"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = { showForgotPasswordDialog = true }
                ) {
                    Text(
                        "¿Olvidaste tu contraseña?",
                        color = Color(0xFF2E7D32),
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Mensaje de error
            if (message.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Botón Iniciar Sesión
            Button(
                onClick = {
                    if (email.isEmpty() || password.isEmpty()) {
                        message = "⚠️ Completa todos los campos"
                        return@Button
                    }

                    isLoading = true
                    message = ""

                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val uid = auth.currentUser?.uid ?: return@addOnCompleteListener

                                dbRef.child(uid).get()
                                    .addOnSuccessListener { snap ->
                                        val rol = snap.child("rol").value?.toString()?.lowercase()?.trim() ?: "pendiente"
                                        val activo = snap.child("activo").value as? Boolean ?: false

                                        if (!activo) {
                                            message = "❌ Tu usuario está deshabilitado. Contacta al administrador."
                                            auth.signOut()
                                            isLoading = false
                                            return@addOnSuccessListener
                                        }

                                        isLoading = false
                                        sessionVM.setSession(uid, rol)

                                        when (rol) {
                                            "administrador" -> navController.navigate(NavRoutes.ADMIN_HOME) {
                                                popUpTo(0) { inclusive = true }
                                            }
                                            "veterinario" -> navController.navigate(NavRoutes.VET_HOME) {
                                                popUpTo(0) { inclusive = true }
                                            }
                                            "cuidador" -> navController.navigate(NavRoutes.CUIDADOR_HOME) {
                                                popUpTo(0) { inclusive = true }
                                            }
                                            "propietario" -> navController.navigate(NavRoutes.PROP_HOME) {
                                                popUpTo(0) { inclusive = true }
                                            }
                                            else -> {
                                                message = "❓ Rol desconocido: $rol"
                                                auth.signOut()
                                            }
                                        }

                                    }.addOnFailureListener {
                                        message = "❌ Error al obtener datos del usuario"
                                        isLoading = false
                                    }
                            } else {
                                val errorMsg = when {
                                    task.exception?.message?.contains("password") == true ->
                                        "❌ Contraseña incorrecta"
                                    task.exception?.message?.contains("user") == true ->
                                        "❌ Usuario no encontrado"
                                    task.exception?.message?.contains("network") == true ->
                                        "❌ Error de conexión"
                                    else -> "❌ ${task.exception?.message}"
                                }
                                message = errorMsg
                                isLoading = false
                            }
                        }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2E7D32)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Iniciando sesión...")
                } else {
                    Icon(Icons.Default.Login, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Iniciar Sesión", fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Botón Registrarse
            OutlinedButton(
                onClick = { navController.navigate(NavRoutes.REGISTER) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF2E7D32)
                )
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("¿No tienes cuenta? Regístrate")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // DIÁLOGO DE RECUPERAR CONTRASEÑA
    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = {
                showForgotPasswordDialog = false
                resetEmail = ""
                resetMessage = ""
            },
            icon = {
                Icon(
                    Icons.Default.LockReset,
                    contentDescription = null,
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    "Recuperar Contraseña",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        "Ingresa tu correo electrónico y te enviaremos un enlace para restablecer tu contraseña.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = {
                            resetEmail = it
                            resetMessage = ""
                        },
                        label = { Text("Correo electrónico") },
                        leadingIcon = { Icon(Icons.Default.Email, null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isResetting
                    )

                    if (resetMessage.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        val isSuccess = resetMessage.contains("✅")
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSuccess)
                                    Color(0xFFE8F5E9)
                                else
                                    MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = resetMessage,
                                modifier = Modifier.padding(12.dp),
                                fontSize = 13.sp,
                                color = if (isSuccess)
                                    Color(0xFF2E7D32)
                                else
                                    MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (resetEmail.isEmpty()) {
                            resetMessage = "⚠️ Ingresa tu correo electrónico"
                            return@Button
                        }

                        if (!resetEmail.contains("@")) {
                            resetMessage = "⚠️ Correo electrónico inválido"
                            return@Button
                        }

                        isResetting = true
                        resetMessage = ""

                        auth.sendPasswordResetEmail(resetEmail)
                            .addOnSuccessListener {
                                resetMessage = "✅ Correo enviado. Revisa tu bandeja de entrada."
                                isResetting = false
                            }
                            .addOnFailureListener { e ->
                                resetMessage = when {
                                    e.message?.contains("user") == true ->
                                        "❌ No existe una cuenta con este correo"
                                    e.message?.contains("network") == true ->
                                        "❌ Error de conexión"
                                    else -> "❌ Error: ${e.message}"
                                }
                                isResetting = false
                            }
                    },
                    enabled = !isResetting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2E7D32)
                    )
                ) {
                    if (isResetting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(if (isResetting) "Enviando..." else "Enviar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showForgotPasswordDialog = false
                        resetEmail = ""
                        resetMessage = ""
                    }
                ) {
                    Text("Cancelar", color = Color(0xFF2E7D32))
                }
            }
        )
    }
}