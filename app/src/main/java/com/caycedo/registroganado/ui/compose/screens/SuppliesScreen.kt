package com.caycedo.registroganado.ui.compose.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
    var filteredList by remember { mutableStateOf(listOf<Insumo>()) }
    var searchQuery by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var insumoToDelete by remember { mutableStateOf<Insumo?>(null) }

    // 🔄 Cargar datos en tiempo real desde Firebase
    LaunchedEffect(Unit) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lista = mutableListOf<Insumo>()
                for (insumoSnapshot in snapshot.children) {
                    val insumo = insumoSnapshot.getValue(Insumo::class.java)
                    if (insumo != null) lista.add(insumo)
                }
                insumos = lista
                filteredList = lista
            }

            override fun onCancelled(error: DatabaseError) {
                println("❌ Error Firebase: ${error.message}")
            }
        })
    }

    // 🔍 Filtro de búsqueda en tiempo real
    LaunchedEffect(searchQuery, insumos) {
        filteredList = if (searchQuery.isBlank()) {
            insumos
        } else {
            insumos.filter {
                it.nombre.contains(searchQuery, ignoreCase = true) ||
                        it.unidad.contains(searchQuery, ignoreCase = true)
            }
        }
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
            // 🔍 Barra de búsqueda
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar insumo por nombre o unidad") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                singleLine = true
            )

            if (filteredList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No se encontraron insumos 🔎")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filteredList) { insumo ->
                        InsumoCard(
                            insumo = insumo,
                            onEdit = { navController.navigate("${NavRoutes.EDIT_SUPPLY}/${insumo.id}") },
                            onDelete = {
                                insumoToDelete = insumo
                                showDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    // ⚠️ Diálogo de confirmación de eliminación
    if (showDialog && insumoToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Eliminar insumo") },
            text = { Text("¿Seguro que deseas eliminar '${insumoToDelete?.nombre}'?") },
            confirmButton = {
                TextButton(onClick = {
                    insumoToDelete?.let { database.child(it.id).removeValue() }
                    showDialog = false
                }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun InsumoCard(insumo: Insumo, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "🌾 ${insumo.nombre}", fontWeight = FontWeight.Bold)
                Text(text = "Cantidad: ${insumo.cantidad} ${insumo.unidad}")
                if (insumo.descripcion.isNotEmpty()) {
                    Text(text = "📝 ${insumo.descripcion}")
                }
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
