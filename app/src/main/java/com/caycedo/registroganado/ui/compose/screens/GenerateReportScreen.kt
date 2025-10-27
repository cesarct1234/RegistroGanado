package com.caycedo.registroganado.ui.compose.screens

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateReportScreen(navController: NavController) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    var reportType by remember { mutableStateOf(ReportType.ALL) }
    var dateFrom by remember { mutableStateOf<Date?>(null) }
    var dateTo by remember { mutableStateOf<Date?>(null) }
    var isWorking by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Generar informe (PDF)", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Tipo de reporte", style = MaterialTheme.typography.titleMedium)
            ReportTypeSelector(reportType) { reportType = it }

            Text("Rango de fechas (opcional)", style = MaterialTheme.typography.titleMedium)
            DateRangeRow(
                dateFrom = dateFrom,
                dateTo = dateTo,
                onPickFrom = { dateFrom = it },
                onPickTo = { dateTo = it }
            )

            Button(
                onClick = {
                    isWorking = true
                    scope.launch {
                        try {
                            val file = generatePdf(ctx, reportType, dateFrom, dateTo)
                            snackbar.showSnackbar("PDF guardado: ${file.name}")
                            sharePdf(ctx, file)
                        } catch (e: Exception) {
                            snackbar.showSnackbar("Error: ${e.message ?: "desconocido"}")
                        } finally {
                            isWorking = false
                        }
                    }
                },
                enabled = !isWorking,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isWorking) "Generando..." else "Generar PDF")
            }
        }
    }
}

// --------- UI helpers ---------

enum class ReportType { ANIMALS, PRODUCTION, SUPPLIES, ALL }

@Composable
private fun ReportTypeSelector(selected: ReportType, onChange: (ReportType) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ReportType.values().forEach { type ->
            val title = when (type) {
                ReportType.ANIMALS -> "Animales"
                ReportType.PRODUCTION -> "Producción"
                ReportType.SUPPLIES -> "Insumos"
                ReportType.ALL -> "Todo (general)"
            }
            FilterChip(
                selected = selected == type,
                onClick = { onChange(type) },
                label = { Text(title) }
            )
        }
    }
}

@Composable
private fun DateRangeRow(
    dateFrom: Date?,
    dateTo: Date?,
    onPickFrom: (Date?) -> Unit,
    onPickTo: (Date?) -> Unit
) {
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val context = LocalContext.current // ✅ se define aquí el contexto

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = dateFrom?.let(sdf::format) ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Desde") },
            modifier = Modifier
                .weight(1f)
                .noRippleClickable {
                    showDatePicker(context) { onPickFrom(it) }
                }
        )
        OutlinedTextField(
            value = dateTo?.let(sdf::format) ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Hasta") },
            modifier = Modifier
                .weight(1f)
                .noRippleClickable {
                    showDatePicker(context) { onPickTo(it) }
                }
        )
    }
}

private fun showDatePicker(context: Context, onDate: (Date?) -> Unit) {
    val cal = Calendar.getInstance()
    DatePickerDialog(
        context,
        { _, y, m, d ->
            cal.set(y, m, d, 0, 0, 0)
            onDate(cal.time)
        },
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH),
        cal.get(Calendar.DAY_OF_MONTH)
    ).show()
}

// --------- Generación del PDF ---------

private suspend fun generatePdf(
    ctx: Context,
    type: ReportType,
    from: Date?,
    to: Date?
): File {
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: error("Sesión expirada")
    val db = FirebaseDatabase.getInstance().reference

    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val nowStr = sdf.format(Date())

    val animals = if (type == ReportType.ANIMALS || type == ReportType.ALL)
        db.child("animales").child(userId).get().await().children.toList()
    else emptyList()

    val supplies = if (type == ReportType.SUPPLIES || type == ReportType.ALL)
        db.child("insumos").child(userId).get().await().children.toList()
    else emptyList()

    val production = if (type == ReportType.PRODUCTION || type == ReportType.ALL)
        db.child("produccion").child(userId).get().await().children
            .filter { snap ->
                val f = snap.child("fecha").value?.toString()
                val d = runCatching { sdf.parse(f ?: "") }.getOrNull()
                if (from == null && to == null) true
                else {
                    val afterFrom = from?.let { d?.after(it)!! || d?.equals(it) == true } ?: true
                    val beforeTo = to?.let { d?.before(it)!! || d?.equals(it) == true } ?: true
                    afterFrom && beforeTo
                }
            }.toList()
    else emptyList()

    val pdf = PdfDocument()
    val paint = Paint().apply { color = Color.BLACK; textSize = 12f }
    val titlePaint = Paint().apply { color = Color.BLACK; textSize = 18f; isFakeBoldText = true }
    val headerPaint = Paint().apply { color = Color.DKGRAY; textSize = 14f; isFakeBoldText = true }

    var pageNumber = 1
    fun newPage(): PdfDocument.Page {
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber++).create()
        return pdf.startPage(pageInfo)
    }

    var page = newPage()
    var y = 60

    fun ensureSpace(linesNeeded: Int = 1) {
        if (y + linesNeeded * 18 > 780) {
            pdf.finishPage(page)
            page = newPage()
            y = 60
        }
    }

    fun drawLineY() {
        page.canvas.drawLine(40f, y.toFloat(), 560f, y.toFloat(), Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        })
    }

    page.canvas.apply {
        drawText("Registro Ganado - Informe", 40f, y.toFloat(), titlePaint); y += 24
        drawText("Fecha: $nowStr   Tipo: $type", 40f, y.toFloat(), paint); y += 20
        from?.let { drawText("Desde: ${sdf.format(it)}", 40f, y.toFloat(), paint); }
        to?.let { drawText("   Hasta: ${sdf.format(it)}", 180f, y.toFloat(), paint) }
        y += 16
        drawLineY(); y += 20
    }

    if (animals.isNotEmpty()) {
        page.canvas.drawText("🐮 Animales", 40f, y.toFloat(), headerPaint); y += 20
        animals.forEach { snap ->
            ensureSpace()
            val nombre = snap.child("nombre").value?.toString() ?: "-"
            val raza = snap.child("raza").value?.toString() ?: "-"
            val sexo = snap.child("sexo").value?.toString() ?: "-"
            page.canvas.drawText("- $nombre  |  Raza: $raza  |  Sexo: $sexo", 48f, y.toFloat(), paint)
            y += 16
        }
        y += 10; drawLineY(); y += 20
    }

    if (production.isNotEmpty()) {
        page.canvas.drawText("🥛 Producción", 40f, y.toFloat(), headerPaint); y += 20
        var total = 0.0
        production.forEach { snap ->
            ensureSpace()
            val fecha = snap.child("fecha").value?.toString() ?: "-"
            val animal = snap.child("nombreAnimal").value?.toString() ?: "-"
            val litros = snap.child("litrosLeche").value?.toString()?.toDoubleOrNull() ?: 0.0
            total += litros
            page.canvas.drawText("- $fecha | $animal | ${"%.2f".format(litros)} L", 48f, y.toFloat(), paint)
            y += 16
        }
        ensureSpace(2)
        page.canvas.drawText("Promedio: ${"%.2f".format(total / production.size)} L", 48f, y.toFloat(), paint); y += 16
        y += 10; drawLineY(); y += 20
    }

    if (supplies.isNotEmpty()) {
        page.canvas.drawText("🌾 Insumos", 40f, y.toFloat(), headerPaint); y += 20
        supplies.forEach { snap ->
            ensureSpace()
            val nombre = snap.child("nombre").value?.toString() ?: "-"
            val cantidad = snap.child("cantidad").value?.toString() ?: "-"
            val unidad = snap.child("unidad").value?.toString() ?: "-"
            page.canvas.drawText("- $nombre  |  Cant: $cantidad $unidad", 48f, y.toFloat(), paint)
            y += 16
        }
        y += 10; drawLineY(); y += 20
    }

    if (animals.isEmpty() && supplies.isEmpty() && production.isEmpty()) {
        page.canvas.drawText("Sin datos para el filtro seleccionado.", 40f, y.toFloat(), paint)
    }

    pdf.finishPage(page)

    val dir = File(ctx.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "reportes")
    if (!dir.exists()) dir.mkdirs()
    val file = File(dir, "informe_${type.name.lowercase()}_${nowStr}.pdf")
    FileOutputStream(file).use { out -> pdf.writeTo(out) }
    pdf.close()

    return file
}

private fun sharePdf(ctx: Context, file: File) {
    val uri: Uri = androidx.core.content.FileProvider.getUriForFile(
        ctx,
        ctx.packageName + ".provider",
        file
    )
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    ctx.startActivity(Intent.createChooser(intent, "Compartir informe"))
}

/**
 * 🔇 Extensión sin efecto ripple (segura para Compose 1.7+)
 */
fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = composed {
    clickable(
        indication = null,
        interactionSource = remember { MutableInteractionSource() }
    ) {
        onClick()
    }
}


