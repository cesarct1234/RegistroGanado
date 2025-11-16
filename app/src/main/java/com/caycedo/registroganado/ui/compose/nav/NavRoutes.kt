package com.caycedo.registroganado.ui.compose.nav

object NavRoutes {

    // ===============================
    // 🧩  AUTH / INICIO
    // ===============================
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val REGISTER = "register"


    // ===============================
    // 👤  HOME SEGÚN ROL
    // ===============================
    const val ADMIN_HOME = "adminHome"
    const val VET_HOME = "vetHome"
    const val CUIDADOR_HOME = "cuidadorHome"
    const val PROP_HOME = "propHome"


    // ===============================
    // 🐮  ANIMALES
    // ===============================
    const val LIST_ANIMALS = "listAnimals"

    // Crear o editar animal (para editar se usa: addAnimal/{id})
    const val ADD_ANIMAL = "addAnimal"


    // ===============================
    // 🌾  INSUMOS
    // ===============================
    const val SUPPLIES = "supplies"
    const val ADD_SUPPLY = "addSupply"

    // Editar insumo → editSupply/{id}
    const val EDIT_SUPPLY = "editSupply"


    // ===============================
    // 🧀  PRODUCCIÓN / REPORTES
    // ===============================
    const val PRODUCTIONS = "productions"

    const val REPORTS = "reports"
    const val REPORTS_PRODUCTION = "reportsProduction"
    const val REPORTS_EXPORT = "reportsExport"
}
