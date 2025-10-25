package com.caycedo.registroganado.ui.compose.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSupplyScreen(navController: NavController, insumoId: String) {
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: return
    val database = FirebaseDatabase.getInstance().getReference("insumos").child(userId).child(insumoId)

    var nombre by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }
    var unidad by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    // 🔄 Cargar los datos del insumo
    LaunchedEffect(insumoId) {
        database.get().addOnSuccessListener { snapshot ->
            nombre = snapshot.child("nombre").value?.toString() ?: ""
            cantidad = snapshot.child("cantidad").value?.toString() ?: ""
            unidad = snapshot.child("unidad").value?.toString() ?: ""
            descripcion = snapshot.child("descripcion").value?.toString() ?: ""
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Editar Insumo", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = cantidad, onValueChange = { cantidad = it }, label = { Text("Cantidad") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = unidad, onValueChange = { unidad = it }, label = { Text("Unidad") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = descripcion, onValueChange = { descripcion = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth())

            Button(
                onClick = {
                    isSaving = true
                    val updates = mapOf(
                        "nombre" to nombre,
                        "cantidad" to cantidad,
                        "unidad" to unidad,
                        "descripcion" to descripcion
                    )
                    database.updateChildren(updates).addOnCompleteListener {
                        isSaving = false
                        if (it.isSuccessful) navController.popBackStack()
                    }
                },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isSaving) "Guardando cambios..." else "Guardar cambios")
            }
        }
    }
}
