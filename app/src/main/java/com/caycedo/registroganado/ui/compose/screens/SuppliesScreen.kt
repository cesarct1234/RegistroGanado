package com.caycedo.registroganado.ui.compose.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.caycedo.registroganado.ui.compose.nav.NavRoutes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

data class Insumo(
    val id: String = "",
    val nombre: String = "",
    val cantidad: String = "",
    val unidad: String = "",
    val descripcion: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuppliesScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: return
    val database = FirebaseDatabase.getInstance().getReference("insumos").child(userId)

    var insumos by remember { mutableStateOf(listOf<Insumo>()) }

    // 🔄 Escucha de datos en tiempo real
    LaunchedEffect(Unit) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lista = mutableListOf<Insumo>()
                for (insumoSnapshot in snapshot.children) {
                    val insumo = insumoSnapshot.getValue(Insumo::class.java)
                    if (insumo != null) lista.add(insumo)
                }
                insumos = lista
            }

            override fun onCancelled(error: DatabaseError) {
                println("Error Firebase: ${error.message}")
            }
        })
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Registro de Insumos", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(NavRoutes.ADD_SUPPLY) }) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar insumo")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (insumos.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay insumos registrados aún 🌾")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(insumos) { insumo ->
                        InsumoCard(insumo)
                    }
                }
            }
        }
    }
}

@Composable
fun InsumoCard(insumo: Insumo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "🌾 ${insumo.nombre}", fontWeight = FontWeight.Bold)
            Text(text = "Cantidad: ${insumo.cantidad} ${insumo.unidad}")
        }
    }
}


