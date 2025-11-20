package com.caycedo.registroganado.ui.compose.screens.animals

import java.util.Calendar

data class Animal(
    val id: String = "",
    val nombre: String = "",
    val raza: String = "",
    val sexo: String = "",
    val tipoProduccion: String = "",
    val nacimiento: String = "",
    val peso: String = "",
    val produccionLeche: String = "",
    val estadoReproductivo: String = "",
    val ultimoParto: String = "",
    val aptoConsumo: Boolean = false,
    val vacunaciones: String = "",
    val tratamientos: String = "",
    val observaciones: String = "",
    val propietarioId: String = "",
    val activo: Boolean = true,
){
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
            } catch (e: NumberFormatException) {
                "N/A"
            }
        }
}
