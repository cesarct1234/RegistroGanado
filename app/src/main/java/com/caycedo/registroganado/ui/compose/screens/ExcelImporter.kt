package com.caycedo.registroganado.ui.compose.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import org.apache.poi.xssf.usermodel.XSSFWorkbook

fun leerExcel(context: Context, uri: Uri) {
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: return
    val database = FirebaseDatabase.getInstance().getReference("animales").child(userId)

    try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val workbook = XSSFWorkbook(inputStream)
        val sheet = workbook.getSheetAt(0)

        // Saltar la fila de encabezado
        for (i in 1..sheet.lastRowNum) {
            val row = sheet.getRow(i)

            val id = row.getCell(0)?.toString()?.trim() ?: ""
            val nombre = row.getCell(1)?.toString()?.trim() ?: ""
            val raza = row.getCell(2)?.toString()?.trim() ?: ""
            val sexo = row.getCell(3)?.toString()?.trim() ?: ""
            val nacimiento = row.getCell(4)?.toString()?.trim() ?: ""
            val edad = row.getCell(5)?.toString()?.trim() ?: ""
            val peso = row.getCell(6)?.toString()?.trim() ?: ""
            val estadoReproductivo = row.getCell(7)?.toString()?.trim() ?: ""
            val ultimoParto = row.getCell(8)?.toString()?.trim() ?: ""
            val produccionLeche = row.getCell(9)?.toString()?.trim() ?: ""
            val vacunas = row.getCell(10)?.toString()?.trim() ?: ""
            val tratamientos = row.getCell(11)?.toString()?.trim() ?: ""
            val observaciones = row.getCell(12)?.toString()?.trim() ?: ""

            if (nombre.isNotEmpty()) {
                val animalId = database.push().key ?: continue
                val animal = Animal(
                    id = id.ifEmpty { animalId },
                    nombre = nombre,
                    raza = raza,
                    sexo = sexo,
                    nacimiento = nacimiento,
                    edad = edad,
                    peso = peso,
                    estadoReproductivo = estadoReproductivo,
                    ultimoParto = ultimoParto,
                    produccionLeche = produccionLeche,
                    vacunas = vacunas,
                    tratamientos = tratamientos,
                    observaciones = observaciones
                )
                database.child(animalId).setValue(animal)
            }
        }

        workbook.close()
        inputStream?.close()
        Toast.makeText(context, "Importación desde Excel completada ✅", Toast.LENGTH_LONG).show()

    } catch (e: Exception) {
        Toast.makeText(context, "Error al leer Excel: ${e.message}", Toast.LENGTH_LONG).show()
        e.printStackTrace()
    }
}

