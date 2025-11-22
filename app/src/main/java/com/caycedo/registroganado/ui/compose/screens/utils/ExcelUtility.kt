package com.caycedo.registroganado.ui.compose.screens.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream

object ExcelUtility {

    private val dbRef = FirebaseDatabase.getInstance().getReference("animales")

    // -------------------------------------------------------------------------
    //  EXPORTAR ANIMALES A EXCEL
    // -------------------------------------------------------------------------
    fun exportExcel(context: Context, onResult: (filePath: String?) -> Unit) {
        dbRef.get().addOnSuccessListener { snapshot ->
            try {
                val workbook = XSSFWorkbook()
                val sheet = workbook.createSheet("Animales")

                // Encabezados
                val header = sheet.createRow(0)
                val headers = listOf(
                    "ID", "Nombre", "Raza", "Sexo", "Fecha Nacimiento",
                    "Peso", "Producción Leche", "Estado Reproductivo",
                    "Último Parto", "Vacunas", "Tratamientos",
                    "Observaciones", "Apto Consumo"
                )

                headers.forEachIndexed { index, title ->
                    header.createCell(index).setCellValue(title)
                }

                // Datos
                var rowIndex = 1
                snapshot.children.forEach { snap ->
                    val row = sheet.createRow(rowIndex++)
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

                // Guardar archivo
                val folder = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "ReportesExcel")
                if (!folder.exists()) folder.mkdirs()

                val file = File(folder, "animales_export.xlsx")
                FileOutputStream(file).use { workbook.write(it) }
                onResult(file.absolutePath)
            } catch (e: Exception) {
                Log.e("ExcelUtility", "Error writing Excel file", e)
                onResult(null)
            }
        }.addOnFailureListener {
            Log.e("ExcelUtility", "Error fetching data for export", it)
            onResult(null)
        }
    }

    // -------------------------------------------------------------------------
    //  IMPORTAR ANIMALES DESDE EXCEL
    // -------------------------------------------------------------------------
    fun importExcelFromUri(context: Context, uri: Uri): Boolean {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val workbook = WorkbookFactory.create(inputStream)
                val sheet = workbook.getSheetAt(0)

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

                    dbRef.child(id).setValue(data)
                }
            }
            true
        } catch (e: Exception) {
            Log.e("ExcelUtility", "Error importing Excel", e)
            false
        }
    }
}