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
import com.google.firebase.database.FirebaseDatabase
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
//import com.patrykandpatrick.vico.compose.chart.pie.pieChart
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import kotlinx.coroutines.tasks.await

// --- Data class resumen general ---
data class ReporteResumen(
    val totalAnimales: Int = 0,
    val totalProducciones: Int = 0,
    val promedioLeche: Double = 0.0,
    val razasUnicas: Int = 0,
    val totalInsumos: Int = 0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: return
    val database = FirebaseDatabase.getInstance().reference

    var resumen by remember { mutableStateOf(ReporteResumen()) }
    var isLoading by remember { mutableStateOf(true) }

    // Datos para gráficos
    val insumoBarChartProducer = remember { ChartEntryModelProducer() }
    val insumoPieChartProducer = remember { ChartEntryModelProducer() }
    var unidadesLabels by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val animalesSnap = database.child("animales").child(userId).get().await()
            val produccionSnap = database.child("produccion").child(userId).get().await()
            val insumosSnap = database.child("insumos").child(userId).get().await()

            val totalAnimales = animalesSnap.childrenCount.toInt()
            val razas = animalesSnap.children.mapNotNull { it.child("raza").value?.toString() }.toSet()

            // 🥛 Producción
            var litrosTotales = 0.0
            var produccionesValidas = 0
            produccionSnap.children.forEach { snap ->
                snap.child("litrosLeche").value?.toString()?.toDoubleOrNull()?.let { litros ->
                    if (litros > 0) {
                        litrosTotales += litros
                        produccionesValidas++
                    }
                }
            }

            // 🌾 Insumos
            val totalInsumos = insumosSnap.childrenCount.toInt()
            val unidadesMap = mutableMapOf<String, Double>()
            insumosSnap.children.forEach { snap ->
                val unidad = snap.child("unidad").value?.toString()?.takeIf { it.isNotBlank() } ?: "Sin unidad"
                val cantidad = snap.child("cantidad").value?.toString()?.toDoubleOrNull() ?: 0.0
                unidadesMap[unidad] = (unidadesMap[unidad] ?: 0.0) + cantidad
            }

            val unidadesSorted = unidadesMap.entries.sortedByDescending { it.value }
            unidadesLabels = unidadesSorted.map { it.key }

            // Datos para gráficos
            insumoBarChartProducer.setEntries(
                unidadesSorted.mapIndexed { index, entry -> entryOf(index.toFloat(), entry.value.toFloat()) }
            )
            insumoPieChartProducer.setEntries(
                unidadesSorted.mapIndexed { index, entry -> entryOf(index.toFloat(), entry.value.toFloat()) }
            )

            resumen = ReporteResumen(
                totalAnimales = totalAnimales,
                razasUnicas = razas.size,
                totalProducciones = produccionSnap.childrenCount.toInt(),
                promedioLeche = if (produccionesValidas > 0) litrosTotales / produccionesValidas else 0.0,
                totalInsumos = totalInsumos
            )
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
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
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    Text("📊 Resumen general", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    ReportCard("🐮 Total animales", resumen.totalAnimales.toString())
                    ReportCard("🌾 Total insumos", resumen.totalInsumos.toString())
                    ReportCard("🥛 Registros producción", resumen.totalProducciones.toString())
                    ReportCard("📈 Promedio leche (L)", "%.2f".format(resumen.promedioLeche))
                }

                if (unidadesLabels.isNotEmpty()) {
                    // 🌾 Gráfico de barras
                    item {
                        Text("📦 Cantidades por unidad de insumo", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        val bottomAxisValueFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
                            unidadesLabels.getOrNull(value.toInt()) ?: ""
                        }
                        Chart(
                            chart = columnChart(),
                            chartModelProducer = insumoBarChartProducer,
                            startAxis = rememberStartAxis(),
                            bottomAxis = rememberBottomAxis(valueFormatter = bottomAxisValueFormatter, labelRotationDegrees = -45f),
                            modifier = Modifier.height(250.dp)
                        )
                    }

                    /* 🥧 Gráfico circular (PieChart de Vico)
                    item {
                        Text("🥧 Distribución de insumos por unidad", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Chart(
                            chart = pieChart(),
                            chartModelProducer = insumoPieChartProducer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                        )
                    } */
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
            Text(text = titulo, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
            Text(text = valor, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
}








