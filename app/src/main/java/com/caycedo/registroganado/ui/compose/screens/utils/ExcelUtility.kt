package com.caycedo.registroganado.ui.compose.screens.utils



import android.content.Context
import android.os.Environment
import com.caycedo.registroganado.ui.compose.screens.animals.Animal
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream

object ExcelUtility {

    fun exportarAnimales(context: Context, lista: List<Animal>): File {

        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Animales")

        // Encabezados
        val header = sheet.createRow(0)
        val columns = listOf(
            "ID", "Nombre", "Raza", "Sexo", "Nacimiento", "Edad", "Peso",
            "Estado Reproductivo", "Último Parto", "Producción Leche",
            "Tipo Producción", "Vacunas", "Tratamientos", "Observaciones",
            "Apto Consumo", "Propietario", "Activo"
        )

        for ((i, col) in columns.withIndex()) {
            header.createCell(i).setCellValue(col)
        }

        // Contenido
        lista.forEachIndexed { index, a ->
            val row = sheet.createRow(index + 1)

            row.createCell(0).setCellValue(a.id)
            row.createCell(1).setCellValue(a.nombre)
            row.createCell(2).setCellValue(a.raza)
            row.createCell(3).setCellValue(a.sexo)
            row.createCell(4).setCellValue(a.nacimiento)
            row.createCell(5).setCellValue(a.edad)
            row.createCell(6).setCellValue(a.peso)
            row.createCell(7).setCellValue(a.estadoReproductivo)
            row.createCell(8).setCellValue(a.ultimoParto)
            row.createCell(9).setCellValue(a.produccionLeche)
            row.createCell(10).setCellValue(a.tipoProduccion)
            row.createCell(11).setCellValue(a.vacunas)
            row.createCell(12).setCellValue(a.tratamientos)
            row.createCell(13).setCellValue(a.observaciones)
            row.createCell(14).setCellValue(a.aptoConsumo.toString())
            row.createCell(15).setCellValue(a.propietarioId)
            row.createCell(16).setCellValue(a.activo.toString())
        }

        // Crear archivo
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        if (dir!!.exists().not()) dir.mkdirs()

        val file = File(dir, "animales.xlsx")
        val output = FileOutputStream(file)
        workbook.write(output)
        output.close()
        workbook.close()

        return file
    }
}
