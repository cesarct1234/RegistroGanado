package com.caycedo.registroganado.utils

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Environment
import androidx.core.content.FileProvider
import com.caycedo.registroganado.R
import com.caycedo.registroganado.ui.compose.screens.animals.Animal
import java.io.File
import java.io.FileOutputStream

object PDFUtils {


    fun generarReporteGeneral(context: Context, animales: List<Animal>): File {


        val pdf = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdf.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 14f
        }

        // 🔥 LOGO
        val logo = BitmapFactory.decodeResource(context.resources, R.drawable.logo_ganaderia)
        val logoScaled = Bitmap.createScaledBitmap(logo, 120, 120, true)
        canvas.drawBitmap(logoScaled, 30f, 20f, null)

        // 🔥 TÍTULO
        paint.textSize = 22f
        paint.isFakeBoldText = true
        canvas.drawText("Ganadería La Esperanza", 170f, 70f, paint)

        paint.textSize = 16f
        paint.isFakeBoldText = false
        canvas.drawText("Reporte General de Animales", 170f, 100f, paint)

        val fecha = "Fecha: " + java.time.LocalDate.now().toString()
        canvas.drawText(fecha, 170f, 130f, paint)

        // 🔥 ENCABEZADO DE TABLA
        paint.textSize = 14f
        paint.isFakeBoldText = true

        var y = 180f

        canvas.drawText("ID", 30f, y, paint)
        canvas.drawText("Nombre", 100f, y, paint)
        canvas.drawText("Raza", 230f, y, paint)
        canvas.drawText("Sexo", 330f, y, paint)
        canvas.drawText("Peso", 400f, y, paint)
        canvas.drawText("Estado", 470f, y, paint)

        paint.isFakeBoldText = false
        y += 30f

        // 🔥 CONTENIDO
        animales.forEach { a ->
            canvas.drawText(a.id.take(6), 30f, y, paint)
            canvas.drawText(a.nombre.take(12), 100f, y, paint)
            canvas.drawText(a.raza.take(10), 230f, y, paint)
            canvas.drawText(a.sexo, 330f, y, paint)
            canvas.drawText(a.peso + "kg", 400f, y, paint)
            canvas.drawText(if (a.activo) "Activo" else "Inactivo", 470f, y, paint)

            y += 25f
        }

        pdf.finishPage(page)

        // 🔥 GUARDAR ARCHIVO
        val file = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            "reporte_animales.pdf"
        )

        val fos = FileOutputStream(file)
        pdf.writeTo(fos)
        fos.close()
        pdf.close()

        return file
    }

    fun compartirPDF(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            context.packageName + ".provider",
            file
        )

        val intent = android.content.Intent(android.content.Intent.ACTION_SEND)
        intent.type = "application/pdf"
        intent.putExtra(android.content.Intent.EXTRA_STREAM, uri)
        intent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)

        context.startActivity(android.content.Intent.createChooser(intent, "Compartir PDF"))
    }
}

