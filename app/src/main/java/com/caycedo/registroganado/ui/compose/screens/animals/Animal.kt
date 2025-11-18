package com.caycedo.registroganado.ui.compose.screens.animals



data class Animal(
    val id: String = "",
    val nombre: String = "",
    val raza: String = "",
    val sexo: String = "",
    val nacimiento: String = "",
    val edad: String = "",
    val peso: String = "",
    val estadoReproductivo: String = "",
    val ultimoParto: String = "",
    val produccionLeche: String = "",
    val tipoProduccion: String = "",
    val vacunas: String = "",
    val tratamientos: String = "",
    val observaciones: String = "",
    val aptoConsumo: Boolean = false,
    val propietarioId: String = "",
    val activo: Boolean = true
)

