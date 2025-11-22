package com.caycedo.registroganado.ui.compose.screens.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

import com.caycedo.registroganado.ui.compose.screens.reports.widgets.AnimalGeneralCharts

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(navController: NavController, vm: ReportsViewModel = viewModel()) {

    val totalAnimales by vm.totalAnimales.collectAsState()
    val totalInsumos by vm.totalInsumos.collectAsState()
    val totalProduccion by vm.totalProduccion.collectAsState()
    val promedioLeche by vm.promedioLeche.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Reportes Estadísticos", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { pad ->

        LazyColumn(
            modifier = Modifier
                .padding(pad)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            //──────────────────────────────────────────────
            // RESUMEN GENERAL
            //──────────────────────────────────────────────
            item {
                SummaryCard(
                    totalAnimales = totalAnimales,
                    totalInsumos = totalInsumos,
                    totalProduccion = totalProduccion,
                    promedioLeche = promedioLeche
                )
            }

            //──────────────────────────────────────────────
            // GRAFICAS GENERALES (Un solo archivo maneja todas)
            //──────────────────────────────────────────────
            item {
                Text(
                    "Reportes Generales del Hato",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                AnimalGeneralCharts()
            }
        }
    }
}

@Composable
fun SummaryCard(
    totalAnimales: Int,
    totalInsumos: Int,
    totalProduccion: Int,
    promedioLeche: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryRow("Total de Animales", totalAnimales.toString())
            SummaryRow("Total Insumos Registrados", totalInsumos.toString())
            SummaryRow("Registros de Producción", totalProduccion.toString())
            SummaryRow("Promedio de Producción Leche", "${"%.2f".format(promedioLeche)} L/día")
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontWeight = FontWeight.Medium)
        Text(value, fontWeight = FontWeight.Bold)
    }
}

