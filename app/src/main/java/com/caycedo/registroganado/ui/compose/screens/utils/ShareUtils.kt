package com.caycedo.registroganado.ui.compose.screens.utils

import android.content.Context
import androidx.core.content.FileProvider
import java.io.File

object ShareUtils {

    fun compartirArchivo(context: Context, file: File) {

        val uri = FileProvider.getUriForFile(
            context,
            context.packageName + ".provider",
            file
        )

        val sendIntent = android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(
            android.content.Intent.createChooser(sendIntent, "Compartir Excel")
        )
    }
}
