package com.caycedo.registroganado.ui_compose.screens

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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.caycedo.registroganado.R
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

    var isLoading by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    var message by remember { mutableStateOf("") }

    val bg = Color(0xFFC7E6B5)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Iniciar Sesión", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFA8D89F)
                )
            )
        },
        containerColor = bg
    ) { pad ->

        Column(
            modifier = Modifier
                .padding(pad)
                .padding(24.dp)
                .background(bg)
                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Card(
                modifier = Modifier
                    .size(200.dp)
                    .padding(bottom = 16.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Image(
                    painter = painterResource(R.drawable.imginiciarsesion),
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )
            }

            Text(
                "Bienvenido de nuevo",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )
            Text(
                "Ingresa tus credenciales para continuar",
                color = Color(0xFF558B2F),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // EMAIL
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    message = ""
                },
                label = { Text("Correo electrónico") },
                leadingIcon = { Icon(Icons.Default.MailOutline, null, tint = Color(0xFF2E7D32)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(10.dp))

            // PASSWORD
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    message = ""
                },
                label = { Text("Contraseña") },
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = Color(0xFF2E7D32)) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32)
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(14.dp))

            // MENSAJE DE ERROR
            if (message.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // LOGIN BUTTON
            Button(
                onClick = {

                    if (email.isBlank() || password.isBlank()) {
                        message = "⚠️ Completa todos los campos"
                        return@Button
                    }

                    isLoading = true
                    message = ""

                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (!task.isSuccessful) {
                                message = "❌ Credenciales incorrectas"
                                isLoading = false
                                return@addOnCompleteListener
                            }

                            val uid = auth.currentUser?.uid ?: return@addOnCompleteListener

                            dbRef.child(uid).get()
                                .addOnSuccessListener { snap ->
                                    val rol = snap.child("rol").value?.toString() ?: "pendiente"
                                    val activo = snap.child("activo").value as? Boolean ?: false

                                    if (rol == "pendiente" || !activo) {
                                        message = "❌ Tu cuenta está pendiente de aprobación por el administrador."
                                        auth.signOut()
                                        isLoading = false
                                        return@addOnSuccessListener
                                    }

                                    isLoading = false

                                    when (rol.lowercase()) {
                                        "administrador" ->
                                            navController.navigate(NavRoutes.ADMIN_HOME) {
                                                popUpTo(0) { inclusive = true }
                                            }

                                        "veterinario" ->
                                            navController.navigate(NavRoutes.VET_HOME) {
                                                popUpTo(0) { inclusive = true }
                                            }

                                        "cuidador" ->
                                            navController.navigate("${NavRoutes.CUIDADOR_HOME}/$uid") {
                                                popUpTo(0) { inclusive = true }
                                            }

                                        "propietario" ->
                                            navController.navigate("${NavRoutes.PROP_HOME}/$uid") {
                                                popUpTo(0) { inclusive = true }
                                            }

                                        else -> {
                                            message = "❌ Rol desconocido: $rol"
                                            auth.signOut()
                                        }
                                    }
                                }
                        }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
            ) {

                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Validando…")
                } else {
                    Icon(Icons.Default.Login, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Iniciar Sesión")
                }
            }

            Spacer(Modifier.height(10.dp))

            Divider()

            TextButton(
                onClick = { navController.navigate(NavRoutes.REGISTER) }
            ) {
                Icon(Icons.Default.PersonAdd, null)
                Spacer(Modifier.width(6.dp))
                Text("¿No tienes cuenta? Regístrate")
            }

        }
    }
}
