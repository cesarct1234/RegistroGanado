package com.caycedo.registroganado.ui.compose.screens.reports.widgets

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.yml.charts.axis.AxisData
import co.yml.charts.axis.Gravity
import co.yml.charts.common.model.Point
import co.yml.charts.ui.linechart.LineChart
import co.yml.charts.ui.linechart.model.GridLines
import co.yml.charts.ui.linechart.model.IntersectionPoint
import co.yml.charts.ui.linechart.model.Line
import co.yml.charts.ui.linechart.model.LineChartData
import co.yml.charts.ui.linechart.model.LinePlotData
import co.yml.charts.ui.linechart.model.LineStyle
import co.yml.charts.ui.linechart.model.SelectionHighlightPoint
import co.yml.charts.ui.linechart.model.SelectionHighlightPopUp
import co.yml.charts.ui.linechart.model.ShadowUnderLine
import co.yml.charts.ui.piechart.charts.DonutPieChart
import co.yml.charts.ui.piechart.models.PieChartConfig
import co.yml.charts.ui.piechart.models.PieChartData
import co.yml.charts.common.model.PlotType
import com.caycedo.registroganado.ui.compose.screens.animals.Animal
import com.google.firebase.database.FirebaseDatabase

val colors = listOf(Color.Black, Color.Red, Color.Green, Color.Blue, Color.Yellow, Color.Cyan, Color.Magenta)

@Composable
fun AnimalGeneralCharts() {

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(40.dp)
    ) {
        ChartRazas()
        ChartSexo()
        ChartVacunas()
        ChartPartos()
        ChartProduccionLeche()
        ChartAptoConsumo()
    }
}

//──────────────────────────────────────────────
// 1. RAZAS
//──────────────────────────────────────────────
@Composable
fun ChartRazas() {

    val db = FirebaseDatabase.getInstance().getReference("animales_global")
    var data by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }

    LaunchedEffect(Unit) {
        db.get().addOnSuccessListener { snap ->
            val map = mutableMapOf<String, Int>()
            for (child in snap.children) {
                val a = child.getValue(Animal::class.java)
                if (a != null && a.raza.isNotBlank())
                    map[a.raza] = (map[a.raza] ?: 0) + 1
            }
            data = map
        }
    }

    if (data.isEmpty()) return

    ChartTitle("Distribución por Raza")

    val pieChartData = PieChartData(
        slices = data.entries.mapIndexed { index, entry ->
            PieChartData.Slice(entry.key, entry.value.toFloat(), color = colors[index % colors.size])
        },
        plotType = PlotType.Pie
    )

    val pieChartConfig = PieChartConfig(
        sliceLabelTextSize = 14.sp,
        isAnimationEnable = true,
        showSliceLabels = true
    )

    DonutPieChart(
        modifier = Modifier.height(250.dp),
        pieChartData = pieChartData,
        pieChartConfig = pieChartConfig
    )
}

//──────────────────────────────────────────────
// 2. SEXO
//──────────────────────────────────────────────
@Composable
fun ChartSexo() {

    val db = FirebaseDatabase.getInstance().getReference("animales_global")
    var machos by remember { mutableIntStateOf(0) }
    var hembras by remember { mutableIntStateOf(0) }
    var loaded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        db.get().addOnSuccessListener { snap ->
            for (c in snap.children) {
                val a = c.getValue(Animal::class.java)
                if (a != null) {
                    if (a.sexo == "Macho") machos++ else hembras++
                }
            }
            loaded = true
        }
    }

    if (!loaded || (machos + hembras == 0)) return

    ChartTitle("Distribución por Sexo")

    val slices = listOf(
        PieChartData.Slice("Macho", machos.toFloat(), Color.Blue),
        PieChartData.Slice("Hembra", hembras.toFloat(), Color.Red)
    )

    val pieChartData = PieChartData(slices = slices, plotType = PlotType.Pie)

    val pieChartConfig = PieChartConfig(
        sliceLabelTextSize = 14.sp,
        isAnimationEnable = true,
        showSliceLabels = true
    )

    DonutPieChart(
        modifier = Modifier.height(250.dp),
        pieChartData = pieChartData,
        pieChartConfig = pieChartConfig
    )
}

//──────────────────────────────────────────────
// 3. VACUNAS APLICADAS
//──────────────────────────────────────────────
@Composable
fun ChartVacunas() {

    val db = FirebaseDatabase.getInstance().getReference("animales_global")
    var conteo by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }

    LaunchedEffect(Unit) {
        db.get().addOnSuccessListener { snap ->
            val map = mutableMapOf<String, Int>()
            for (child in snap.children) {
                val a = child.getValue(Animal::class.java)
                if (a != null) {
                    a.vacunaciones.split(",").map { it.trim() }.forEach { vacuna ->
                        if (vacuna.isNotBlank()) {
                            map[vacuna] = (map[vacuna] ?: 0) + 1
                        }
                    }
                }
            }
            conteo = map
        }
    }

    if (conteo.isEmpty()) return

    ChartTitle("Vacunas Aplicadas")

    val pieChartData = PieChartData(
        slices = conteo.entries.mapIndexed { index, entry ->
            PieChartData.Slice(entry.key, entry.value.toFloat(), color = colors[index % colors.size])
        },
        plotType = PlotType.Pie
    )

    val pieChartConfig = PieChartConfig(
        sliceLabelTextSize = 14.sp,
        isAnimationEnable = true,
        showSliceLabels = true
    )

    DonutPieChart(
        modifier = Modifier.height(250.dp),
        pieChartData = pieChartData,
        pieChartConfig = pieChartConfig
    )
}

//──────────────────────────────────────────────
// 4. PARTOS (0 o 1)
//──────────────────────────────────────────────
@Composable
fun ChartPartos() {

    val db = FirebaseDatabase.getInstance().getReference("animales_global")
    val puntos = remember { mutableStateListOf<Point>() }

    LaunchedEffect(Unit) {
        var x = 1f
        db.get().addOnSuccessListener { snap ->
            for (child in snap.children) {
                val a = child.getValue(Animal::class.java)
                if (a != null) {
                    val partos = if (a.ultimoParto.isBlank()) 0 else 1
                    puntos.add(Point(x++, partos.toFloat()))
                }
            }
        }
    }

    if (puntos.isEmpty()) return

    ChartTitle("Número de Partos Registrados")

    val xAxisData = AxisData.Builder()
        .axisStepSize(100.dp)
        .backgroundColor(Color.Transparent)
        .steps(puntos.size - 1)
        .labelData { i -> i.toString() }
        .labelAndAxisLinePadding(15.dp)
        .axisPosition(Gravity.BOTTOM)
        .build()

    val yAxisData = AxisData.Builder()
        .steps(2)
        .backgroundColor(Color.Transparent)
        .labelAndAxisLinePadding(20.dp)
        .labelData { i ->
            val y = i.toFloat()
            String.format("%.1f", y)
        }
        .axisPosition(Gravity.LEFT)
        .build()

    val lineChartData = LineChartData(
        linePlotData = LinePlotData(
            lines = listOf(
                Line(
                    dataPoints = puntos,
                    lineStyle = LineStyle(),
                    intersectionPoint = IntersectionPoint(),
                    selectionHighlightPoint = SelectionHighlightPoint(),
                    shadowUnderLine = ShadowUnderLine(),
                    selectionHighlightPopUp = SelectionHighlightPopUp()
                )
            )
        ),
        xAxisData = xAxisData,
        yAxisData = yAxisData,
        gridLines = GridLines()
    )

    LineChart(
        modifier = Modifier.height(230.dp),
        lineChartData = lineChartData
    )
}

//──────────────────────────────────────────────
// 5. PRODUCCIÓN DE LECHE POR ANIMAL
//──────────────────────────────────────────────
@Composable
fun ChartProduccionLeche() {

    val db = FirebaseDatabase.getInstance().getReference("animales_global")
    val puntos = remember { mutableStateListOf<Point>() }

    LaunchedEffect(Unit) {
        var x = 1f
        db.get().addOnSuccessListener { snap ->
            for (child in snap.children) {
                val a = child.getValue(Animal::class.java)
                if (a != null) {
                    val leche = a.produccionLeche.toFloatOrNull() ?: 0f
                    puntos.add(Point(x++, leche))
                }
            }
        }
    }

    if (puntos.isEmpty()) return

    ChartTitle("Producción de Leche por Animal (L/día)")

    val xAxisData = AxisData.Builder()
        .axisStepSize(100.dp)
        .backgroundColor(Color.Transparent)
        .steps(puntos.size - 1)
        .labelData { i -> i.toString() }
        .labelAndAxisLinePadding(15.dp)
        .axisPosition(Gravity.BOTTOM)
        .build()

    val yAxisData = AxisData.Builder()
        .steps(5)
        .backgroundColor(Color.Transparent)
        .labelAndAxisLinePadding(20.dp)
        .labelData { i ->
            val y = i.toFloat()
            String.format("%.1f", y)
        }
        .axisPosition(Gravity.LEFT)
        .build()

    val lineChartData = LineChartData(
        linePlotData = LinePlotData(
            lines = listOf(
                Line(
                    dataPoints = puntos,
                    lineStyle = LineStyle(),
                    intersectionPoint = IntersectionPoint(),
                    selectionHighlightPoint = SelectionHighlightPoint(),
                    shadowUnderLine = ShadowUnderLine(),
                    selectionHighlightPopUp = SelectionHighlightPopUp()
                )
            )
        ),
        xAxisData = xAxisData,
        yAxisData = yAxisData,
        gridLines = GridLines()
    )

    LineChart(
        modifier = Modifier.height(250.dp),
        lineChartData = lineChartData
    )
}

//──────────────────────────────────────────────
// 6. APTO PARA CONSUMO
//──────────────────────────────────────────────
@Composable
fun ChartAptoConsumo() {

    val db = FirebaseDatabase.getInstance().getReference("animales_global")
    var apto by remember { mutableIntStateOf(0) }
    var noApto by remember { mutableIntStateOf(0) }
    var ready by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        db.get().addOnSuccessListener { snap ->
            for (child in snap.children) {
                val a = child.getValue(Animal::class.java)
                if (a != null) {
                    if (a.aptoConsumo) apto++ else noApto++
                }
            }
            ready = true
        }
    }

    if (!ready || (apto + noApto == 0)) return

    ChartTitle("Animales Aptos para Consumo")

    val slices = listOf(
        PieChartData.Slice("Apto", apto.toFloat(), Color.Green),
        PieChartData.Slice("No Apto", noApto.toFloat(), Color.Red)
    )

    val pieChartData = PieChartData(slices = slices, plotType = PlotType.Pie)

    val pieChartConfig = PieChartConfig(
        sliceLabelTextSize = 14.sp,
        isAnimationEnable = true,
        showSliceLabels = true
    )

    DonutPieChart(
        modifier = Modifier.height(250.dp),
        pieChartData = pieChartData,
        pieChartConfig = pieChartConfig
    )
}

//──────────────────────────────────────────────
// TITULO
//──────────────────────────────────────────────
@Composable
fun ChartTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 10.dp)
    )
}
