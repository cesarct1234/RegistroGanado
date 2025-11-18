package com.caycedo.registroganado.utils

import android.content.Context
import android.os.Environment
//import com.caycedo.registroganado.ui.compose.screens.Animal
import com.caycedo.registroganado.ui.compose.screens.animals.Animal
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import java.io.File
import java.io.FileOutputStream

object PdfGenerator {

    fun generarPDF(context: Context, animal: Animal): File {

        val docsFolder = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            "ReportesGanado"
        )
        if (!docsFolder.exists()) docsFolder.mkdirs()

        val file = File(docsFolder, "reporte_${animal.id}.pdf")
        val output = FileOutputStream(file)

        val document = Document()
        PdfWriter.getInstance(document, output)

        document.open()

        // Título
        val titleFont = Font(Font.FontFamily.HELVETICA, 22f, Font.BOLD)
        val normalFont = Font(Font.FontFamily.HELVETICA, 14f, Font.NORMAL)

        document.add(Paragraph("Reporte Individual de Animal", titleFont))
        document.add(Paragraph("ID Interno: ${animal.id}\n\n", normalFont))

        // Tabla
        val table = PdfPTable(2)
        table.widthPercentage = 100f

        fun addRow(label: String, value: String) {
            val cell1 = PdfPCell(Phrase(label, normalFont))
            cell1.backgroundColor = BaseColor(230, 230, 250)
            table.addCell(cell1)

            val cell2 = PdfPCell(Phrase(value, normalFont))
            table.addCell(cell2)
        }

        addRow("Nombre", animal.nombre)
        addRow("Raza", animal.raza)
        addRow("Sexo", animal.sexo)
        addRow("Fecha de nacimiento", animal.nacimiento)
        addRow("Edad", animal.edad)
        addRow("Peso", "${animal.peso} kg")
        addRow("Estado Reproductivo", animal.estadoReproductivo)
        addRow("Último parto", animal.ultimoParto)
        addRow("Producción Leche", animal.produccionLeche)
        addRow("Tipo Producción", animal.tipoProduccion)
        addRow("Vacunas", animal.vacunas)
        addRow("Tratamientos", animal.tratamientos)
        addRow("Observaciones", animal.observaciones)
        addRow("Apto para consumo", if (animal.aptoConsumo) "Sí" else "No")
        addRow("Estado", if (animal.activo) "Activo" else "Inactivo")

        document.add(table)

        document.close()

        return file
    }
}
