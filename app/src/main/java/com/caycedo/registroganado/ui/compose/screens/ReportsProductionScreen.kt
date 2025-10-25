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
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// 🧮 Modelo de resumen de producción
data class ReporteProduccion(
    val totalProducciones: Int = 0,
    val promedioLeche: Double = 0.0,
    val litrosTotales: Double = 0.0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsProductionScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: return
    val database = FirebaseDatabase.getInstance().reference

    var resumen by remember { mutableStateOf(ReporteProduccion()) }
    var isLoading by remember { mutableStateOf(true) }

    // Modelos para los gráficos
    val barChartProducer = remember { ChartEntryModelProducer() }
    val lineChartProducer = remember { ChartEntryModelProducer() }
    var barLabels by remember { mutableStateOf<List<String>>(emptyList()) }
    var lineLabels by remember { mutableStateOf<List<String>>(emptyList()) }

    // Cargar datos de Firebase
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val produccionSnap = database.child("produccion").child(userId).get().await()

            var litrosTotales = 0.0
            var registrosValidos = 0
            val produccionPorAnimal = mutableMapOf<String, Double>()
            val produccionPorFecha = mutableMapOf<String, Double>()

            for (snap in produccionSnap.children) {
                val litros = snap.child("litrosLeche").value?.toString()?.toDoubleOrNull() ?: 0.0
                if (litros > 0) {
                    val animal = snap.child("nombreAnimal").value?.toString() ?: "Desconocido"
                    val fecha = snap.child("fecha").value?.toString() ?: "N/A"

                    litrosTotales += litros
                    registrosValidos++

                    produccionPorAnimal[animal] = (produccionPorAnimal[animal] ?: 0.0) + litros
                    produccionPorFecha[fecha] = (produccionPorFecha[fecha] ?: 0.0) + litros
                }
            }

            val sortedPorAnimal = produccionPorAnimal.entries.sortedByDescending { it.value }
            val sortedPorFecha = produccionPorFecha.entries.sortedBy {
                try {
                    LocalDate.parse(it.key, DateTimeFormatter.ISO_LOCAL_DATE)
                } catch (e: Exception) {
                    LocalDate.MAX
                }
            }

            // Configurar modelos de gráficos
            barLabels = sortedPorAnimal.map { it.key }
            lineLabels = sortedPorFecha.map { it.key }

            barChartProducer.setEntries(
                sortedPorAnimal.mapIndexed { index, entry -> entryOf(index, entry.value) }
            )
            lineChartProducer.setEntries(
                sortedPorFecha.mapIndexed { index, entry -> entryOf(index, entry.value) }
            )

            resumen = ReporteProduccion(
                totalProducciones = registrosValidos,
                promedioLeche = if (registrosValidos > 0) litrosTotales / registrosValidos else 0.0,
                litrosTotales = litrosTotales
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
                title = { Text("Reporte de Producción", fontWeight = FontWeight.Bold) },
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
                // 📋 Resumen de datos
                item {
                    Text("📊 Resumen de Producción", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    ReportCard("🥛 Total Producciones", resumen.totalProducciones.toString())
                    ReportCard("📈 Promedio Leche (L)", "%.2f".format(resumen.promedioLeche))
                    ReportCard("🧮 Total Litros Producidos", "%.2f".format(resumen.litrosTotales))
                }

                // 📉 Producción total por animal
                if (barLabels.isNotEmpty()) {
                    item {
                        Text("Producción total por animal (L)", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        val formatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
                            barLabels.getOrNull(value.toInt()) ?: ""
                        }
                        Chart(
                            chart = columnChart(),
                            chartModelProducer = barChartProducer,
                            startAxis = rememberStartAxis(),
                            bottomAxis = rememberBottomAxis(valueFormatter = formatter, labelRotationDegrees = -45f),
                            modifier = Modifier.height(250.dp)
                        )
                    }
                }

                // 📆 Producción por fecha
                if (lineLabels.isNotEmpty()) {
                    item {
                        Text("Producción de leche por fecha (L)", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        val formatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
                            lineLabels.getOrNull(value.toInt())?.substring(5) ?: ""
                        }
                        Chart(
                            chart = lineChart(),
                            chartModelProducer = lineChartProducer,
                            startAxis = rememberStartAxis(),
                            bottomAxis = rememberBottomAxis(valueFormatter = formatter, labelRotationDegrees = -45f),
                            modifier = Modifier.height(250.dp)
                        )
                    }
                }
            }
        }
    }
}

// Reutiliza el ReportCard existente en ReportsScreen.kt








