package com.caycedo.registroganado.ui.compose.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.google.firebase.database.*

data class ReporteResumen(
    val totalAnimales: Int = 0,
    val totalProducciones: Int = 0,
    val promedioLeche: Double = 0.0,
    val razasUnicas: Int = 0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: return
    val databaseAnimales = FirebaseDatabase.getInstance().getReference("animales").child(userId)
    val databaseProduccion = FirebaseDatabase.getInstance().getReference("produccion").child(userId)

    var resumen by remember { mutableStateOf(ReporteResumen()) }

    // 🔄 Cargar datos de Firebase
    LaunchedEffect(Unit) {
        var totalAnimales = 0
        val razas = mutableSetOf<String>()
        var totalProducciones = 0
        var litrosTotales = 0.0

        // Animales
        databaseAnimales.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                totalAnimales = snapshot.childrenCount.toInt()
                for (animalSnapshot in snapshot.children) {
                    val raza = animalSnapshot.child("raza").value?.toString()
                    if (!raza.isNullOrEmpty()) razas.add(raza)
                }
                resumen = resumen.copy(totalAnimales = totalAnimales, razasUnicas = razas.size)
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        // Producción
        databaseProduccion.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                totalProducciones = snapshot.childrenCount.toInt()
                for (prodSnapshot in snapshot.children) {
                    val litros = prodSnapshot.child("litrosLeche").value?.toString()?.toDoubleOrNull() ?: 0.0
                    litrosTotales += litros
                }
                val promedio = if (totalProducciones > 0) litrosTotales / totalProducciones else 0.0
                resumen = resumen.copy(totalProducciones = totalProducciones, promedioLeche = promedio)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Reportes y Estadísticas", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (resumen.totalAnimales == 0 && resumen.totalProducciones == 0) {
                Text(
                    text = "Aún no hay datos suficientes para generar reportes 📊",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        ReportCard("🐮 Total de animales registrados", resumen.totalAnimales.toString())
                        ReportCard("🧬 Razas distintas", resumen.razasUnicas.toString())
                        ReportCard("🥛 Registros de producción", resumen.totalProducciones.toString())
                        ReportCard("📈 Promedio de leche (L/día)", "%.2f".format(resumen.promedioLeche))
                    }
                }
            }
        }
    }
}

@Composable
fun ReportCard(titulo: String, valor: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = titulo, fontWeight = FontWeight.Bold)
            Text(text = valor, style = MaterialTheme.typography.bodyLarge)
        }
    }
}



