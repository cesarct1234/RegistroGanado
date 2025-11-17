package com.caycedo.registroganado.ui.compose.nav

object NavRoutes {

    // AUTH
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val REGISTER = "register"

    // Homes según rol
    const val ADMIN_HOME = "adminHome"
    const val VET_HOME = "vetHome"
    const val CUIDADOR_HOME = "cuidadorHome"
    const val PROP_HOME = "propHome"

    // Gestión de usuarios
    const val USERS_MANAGEMENT = "gestionUsuarios"
    const val CREATE_USER = "createUser"
    const val EDIT_USER = "editUser"

    // Animales
    const val LIST_ANIMALS = "listAnimals"
    const val ADD_ANIMAL = "addAnimal"

    // Insumos
    const val SUPPLIES = "supplies"
    const val ADD_SUPPLY = "addSupply"
    const val EDIT_SUPPLY = "editSupply"

    // Producción & Reportes
    const val PRODUCTIONS = "productions"
    const val REPORTS = "reports"
    const val REPORTS_PRODUCTION = "reportsProduction"
    const val REPORTS_EXPORT = "reportsExport"
}
