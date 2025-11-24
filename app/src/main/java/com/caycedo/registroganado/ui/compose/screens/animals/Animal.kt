package com.caycedo.registroganado.ui.compose.screens.animals

import com.google.firebase.database.Exclude
import java.util.Calendar

data class Animal(

    // -------------------------
    // CAMPOS BASE
    // -------------------------
    val id: String = "",
    val nombre: String = "",
    val raza: String = "",
    val sexo: String = "",
    val tipoProduccion: String = "",

    // Fechas
    val nacimiento: String = "",
    val ultimoParto: String = "",

    // Producción y peso
    val peso: String = "",
    val produccionLeche: String = "",

    // Reproducción
    val estadoReproductivo: String = "",

    // -------------------------
    // CAMPOS DE SALUD GENERAL
    // -------------------------
    val vacunaciones: String = "",
    val tratamientos: String = "",
    val observaciones: String = "",

    // -------------------------
    // VETERINARIO
    // -------------------------
    val estadoSaludGeneral: String = "",
    val diagnosticoGeneral: String = "",
    val proximaRevision: String = "",

    // -------------------------
    // CUIDADOR
    // -------------------------
    val alimentacion: String = "",
    val actividades: String = "",
    val pesoDia: String = "",
    val reporteCuidador: String = "",

    // -------------------------
    // CONSUMO Y PROPIETARIO
    // -------------------------
    val aptoConsumo: Boolean = false,
    val propietarioId: String = "",

    // Estado del registro
    val activo: Boolean = true
) {

    // --------------------------------------
    // EDAD CALCULADA (NO SE GUARDA EN FIREBASE)
    // --------------------------------------
    @get:Exclude
    val edad: String
        get() {
            if (nacimiento.isBlank() || !nacimiento.contains("/")) return "N/A"

            return try {
                val parts = nacimiento.split("/")
                if (parts.size != 3) return "N/A"

                val day = parts[0].toInt()
                val month = parts[1].toInt()
                val year = parts[2].toInt()

                val birthCal = Calendar.getInstance()
                birthCal.set(year, month - 1, day)

                val todayCal = Calendar.getInstance()

                var age = todayCal.get(Calendar.YEAR) - birthCal.get(Calendar.YEAR)
                if (todayCal.get(Calendar.DAY_OF_YEAR) < birthCal.get(Calendar.DAY_OF_YEAR)) {
                    age--
                }

                "$age años"
            } catch (e: Exception) {
                "N/A"
            }
        }
}

