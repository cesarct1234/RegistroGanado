package com.caycedo.registroganado.ui.compose.screens.supplies

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.caycedo.registroganado.ui.compose.nav.NavRoutes
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.foundation.background
import androidx.compose.ui.text.style.TextOverflow
import com.google.firebase.database.*

data class Insumo(
    val id: String = "",
    val nombre: String = "",
    val tipo: String = "",
    val cantidad: String = "",
    val unidad: String = "",
    val descripcion: String = "",
    val animalId: String = "",
    val fecha: String = ""
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
    var selectedFilter by remember { mutableStateOf("Todos") }
    var showDialog by remember { mutableStateOf(false) }
    var insumoToDelete by remember { mutableStateOf<Insumo?>(null) }
    var showFilterMenu by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    val filterOptions = listOf("Todos", "Alimento", "Vacuna", "Medicina", "Suplemento", "Otro")

    // Cargar datos en tiempo real
    LaunchedEffect(Unit) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lista = mutableListOf<Insumo>()
                for (insumoSnapshot in snapshot.children) {
                    val insumo = insumoSnapshot.getValue(Insumo::class.java)
                    if (insumo != null) lista.add(insumo)
                }
                insumos = lista.sortedByDescending { it.fecha }
                isLoading = false
            }

            override fun onCancelled(error: DatabaseError) {
                println("❌ Error Firebase: ${error.message}")
                isLoading = false
            }
        })
    }

    // Filtro de búsqueda y tipo
    LaunchedEffect(searchQuery, insumos, selectedFilter) {
        filteredList = insumos
            .filter { insumo ->
                val matchesSearch = searchQuery.isBlank() ||
                        insumo.nombre.contains(searchQuery, ignoreCase = true) ||
                        insumo.tipo.contains(searchQuery, ignoreCase = true) ||
                        insumo.unidad.contains(searchQuery, ignoreCase = true)

                val matchesFilter = selectedFilter == "Todos" ||
                        insumo.tipo.equals(selectedFilter, ignoreCase = true)

                matchesSearch && matchesFilter
            }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Registro de Insumos", fontWeight = FontWeight.Bold)
                        Text(
                            "${filteredList.size} de ${insumos.size}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    // Botón de filtro
                    IconButton(onClick = { showFilterMenu = true }) {
                        Badge(
                            containerColor = if (selectedFilter != "Todos")
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filtrar")
                        }
                    }

                    DropdownMenu(
                        expanded = showFilterMenu,
                        onDismissRequest = { showFilterMenu = false }
                    ) {
                        filterOptions.forEach { filter ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(
                                            selected = selectedFilter == filter,
                                            onClick = null
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(filter)
                                    }
                                },
                                onClick = {
                                    selectedFilter = filter
                                    showFilterMenu = false
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(NavRoutes.ADD_SUPPLY) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Agregar Insumo") },
                containerColor = MaterialTheme.colorScheme.primary
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Barra de búsqueda mejorada
            Surface(
                shadowElevation = 2.dp,
                tonalElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Buscar insumo") },
                        placeholder = { Text("Nombre, tipo o unidad...") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                                }
                            }
                        }
                    )
                }
            }

            // Chips de filtro rápido
            if (selectedFilter != "Todos") {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Filtrando por: $selectedFilter",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(Modifier.weight(1f))
                        TextButton(onClick = { selectedFilter = "Todos" }) {
                            Text("Limpiar filtro")
                        }
                    }
                }
            }

            // Contenido principal
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(Modifier.height(16.dp))
                            Text("Cargando insumos...")
                        }
                    }
                }

                insumos.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Inventory,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "No hay insumos registrados",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Presiona el botón + para agregar tu primer insumo",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                filteredList.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                Icons.Default.SearchOff,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "No se encontraron resultados",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Intenta con otros términos de búsqueda",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        items(filteredList, key = { it.id }) { insumo ->
                            InsumoCard(
                                insumo = insumo,
                                onEdit = { navController.navigate("${NavRoutes.EDIT_SUPPLY}/${insumo.id}") },
                                onDelete = {
                                    insumoToDelete = insumo
                                    showDialog = true
                                }
                            )
                        }

                        // Espaciado para el FAB
                        item {
                            Spacer(Modifier.height(80.dp))
                        }
                    }
                }
            }
        }
    }

    // Diálogo de confirmación mejorado
    if (showDialog && insumoToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("¿Eliminar insumo?") },
            text = {
                Column {
                    Text("Esta acción no se puede deshacer.")
                    Spacer(Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "Insumo: ${insumoToDelete?.nombre}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                "Tipo: ${insumoToDelete?.tipo}",
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        insumoToDelete?.let { database.child(it.id).removeValue() }
                        showDialog = false
                        insumoToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                    insumoToDelete = null
                }) {
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
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header con tipo y acciones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Badge de tipo
                Surface(
                    color = when (insumo.tipo.lowercase()) {
                        "alimento" -> MaterialTheme.colorScheme.primaryContainer
                        "vacuna" -> MaterialTheme.colorScheme.secondaryContainer
                        "medicina" -> MaterialTheme.colorScheme.tertiaryContainer
                        "suplemento" -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = insumo.tipo.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = MaterialTheme.colorScheme.primary
                        )
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

            Spacer(Modifier.height(8.dp))

            // Nombre del insumo
            Text(
                text = insumo.nombre,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(12.dp))

            // Información en chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Cantidad
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Inventory2,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "${insumo.cantidad} ${insumo.unidad}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Fecha
                if (insumo.fecha.isNotEmpty()) {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                insumo.fecha,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Descripción
            if (insumo.descripcion.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Divider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = insumo.descripcion,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}