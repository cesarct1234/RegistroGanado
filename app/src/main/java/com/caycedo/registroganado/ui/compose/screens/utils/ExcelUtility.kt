package com.caycedo.registroganado.ui.compose.screens.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import com.google.firebase.database.FirebaseDatabase
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream

object ExcelUtility {

    private val dbRef = FirebaseDatabase.getInstance().getReference("animales_global")

    fun importExcelFromUri(context: Context, uri: Uri): Boolean {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->

                val workbook = org.apache.poi.ss.usermodel.WorkbookFactory.create(inputStream)
                val sheet = workbook.getSheetAt(0)

                // Empieza desde fila 1 (fila 0 es encabezado)
                for (i in 1..sheet.lastRowNum) {
                    val row = sheet.getRow(i) ?: continue

                    val id = row.getCell(0)?.toString() ?: continue

                    val data = mapOf(
                        "nombre" to row.getCell(1)?.toString(),
                        "raza" to row.getCell(2)?.toString(),
                        "sexo" to row.getCell(3)?.toString(),
                        "nacimiento" to row.getCell(4)?.toString(),
                        "peso" to row.getCell(5)?.toString(),
                        "produccionLeche" to row.getCell(6)?.toString(),
                        "estadoReproductivo" to row.getCell(7)?.toString(),
                        "ultimoParto" to row.getCell(8)?.toString(),
                        "vacunaciones" to row.getCell(9)?.toString(),
                        "tratamientos" to row.getCell(10)?.toString(),
                        "observaciones" to row.getCell(11)?.toString(),
                        "aptoConsumo" to (row.getCell(12)?.toString() == "Sí")
                    )

                    // Guardar en Firebase
                    FirebaseDatabase.getInstance()
                        .getReference("animales_global")
                        .child(id)
                        .setValue(data)
                }
            }
            true
        } catch (e: Exception) {
            Log.e("ExcelUtility", "Error importing Excel", e)
            false
        }
    }


    // -------------------------------------------------------------------------
    // EXPORTAR EXCEL → carpeta válida para FileProvider
    // -------------------------------------------------------------------------
    fun exportExcel(
        context: Context,
        onSaved: (File?) -> Unit
    ) {
        dbRef.get().addOnSuccessListener { snapshot ->
            try {

                val workbook = XSSFWorkbook()
                val sheet = workbook.createSheet("Animales")

                // ENCABEZADOS
                val header = sheet.createRow(0)
                val headers = listOf(
                    "ID", "Nombre", "Raza", "Sexo", "Nacimiento",
                    "Peso", "Producción", "Estado Reproductivo",
                    "Último Parto", "Vacunas", "Tratamientos",
                    "Observaciones", "Apto Consumo"
                )

                headers.forEachIndexed { i, h ->
                    header.createCell(i).setCellValue(h)
                }

                // DATOS
                var idx = 1
                snapshot.children.forEach { snap ->
                    val row = sheet.createRow(idx++)

                    row.createCell(0).setCellValue(snap.key ?: "")
                    row.createCell(1).setCellValue(snap.child("nombre").value?.toString() ?: "")
                    row.createCell(2).setCellValue(snap.child("raza").value?.toString() ?: "")
                    row.createCell(3).setCellValue(snap.child("sexo").value?.toString() ?: "")
                    row.createCell(4).setCellValue(snap.child("nacimiento").value?.toString() ?: "")
                    row.createCell(5).setCellValue(snap.child("peso").value?.toString() ?: "")
                    row.createCell(6).setCellValue(snap.child("produccionLeche").value?.toString() ?: "")
                    row.createCell(7).setCellValue(snap.child("estadoReproductivo").value?.toString() ?: "")
                    row.createCell(8).setCellValue(snap.child("ultimoParto").value?.toString() ?: "")
                    row.createCell(9).setCellValue(snap.child("vacunaciones").value?.toString() ?: "")
                    row.createCell(10).setCellValue(snap.child("tratamientos").value?.toString() ?: "")
                    row.createCell(11).setCellValue(snap.child("observaciones").value?.toString() ?: "")
                    row.createCell(12).setCellValue(
                        if (snap.child("aptoConsumo").value == true) "Sí" else "No"
                    )
                }

                // ✔ Carpeta válida para FileProvider
                val folder = File(
                    context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                    "Reportes"
                )

                if (!folder.exists()) folder.mkdirs()

                val file = File(folder, "animales_export.xlsx")

                FileOutputStream(file).use {
                    workbook.write(it)
                }

                onSaved(file)

            } catch (e: Exception) {
                Log.e("ExcelUtility", "Error creando Excel", e)
                onSaved(null)
            }
        }
    }

    // -------------------------------------------------------------------------
    // ABRIR ARCHIVO EXCEL
    // -------------------------------------------------------------------------
    fun abrirArchivo(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(intent)
    }

    // -------------------------------------------------------------------------
    // ENVIAR POR WHATSAPP
    // -------------------------------------------------------------------------
    fun enviarWhatsApp(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            putExtra(Intent.EXTRA_STREAM, uri)
            setPackage("com.whatsapp")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(intent)
    }

    // -------------------------------------------------------------------------
    // ENVIAR POR CORREO
    // -------------------------------------------------------------------------
    fun enviarCorreo(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(""))
            putExtra(Intent.EXTRA_SUBJECT, "Reporte de animales")
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Enviar archivo"))
    }
}

