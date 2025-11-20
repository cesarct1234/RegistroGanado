package com.caycedo.registroganado.ui.compose.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.caycedo.registroganado.ui_compose.screens.*
import com.caycedo.registroganado.ui.compose.screens.users.*
import com.caycedo.registroganado.ui.compose.screens.animals.*
import com.caycedo.registroganado.ui.compose.screens.supplies.*
import com.caycedo.registroganado.ui.compose.screens.reports.*
import com.caycedo.registroganado.ui_compose.screens.roles.*
import com.caycedo.registroganado.ui.compose.screens.ProductionScreen
import com.caycedo.registroganado.ui.compose.screens.RegisterScreen
import com.caycedo.registroganado.ui_compose.screens.users.CreateUserScreen

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {

    NavHost(
        navController = navController,
        startDestination = NavRoutes.WELCOME
    ) {

        // 🔵 AUTENTICACIÓN
        composable(NavRoutes.WELCOME) { WelcomeScreen(navController) }
        composable(NavRoutes.LOGIN) { LoginScreen(navController) }
        composable(NavRoutes.REGISTER) { RegisterScreen(navController) }

        // 🔵 HOMES POR ROL
        composable(NavRoutes.ADMIN_HOME) { AdminHomeScreen(navController) }
        composable(NavRoutes.VET_HOME) { VetHomeScreen(navController) }

        composable("${NavRoutes.PROP_HOME}/{propietarioId}") { back ->
            val propietarioId = back.arguments?.getString("propietarioId") ?: ""
            PropHomeScreen(navController, propietarioId)
        }

        composable("${NavRoutes.CUIDADOR_HOME}/{cuidadorId}") { back ->
            val cuidadorId = back.arguments?.getString("cuidadorId") ?: ""
            CuidadorHomeScreen(navController, cuidadorId)
        }

        // 🔵 GESTIÓN DE USUARIOS
        composable(NavRoutes.USERS_MANAGEMENT) { UserManagementScreen(navController) }
        composable(NavRoutes.CREATE_USER) { CreateUserScreen(navController) }
        composable("${NavRoutes.EDIT_USER}/{userId}") { back ->
            EditUserScreen(navController, back.arguments?.getString("userId") ?: "")
        }

        // 🔵 ANIMALES
        composable(NavRoutes.LIST_ANIMALS) {
            AnimalListScreen(navController)
        }

        composable("${NavRoutes.ADD_ANIMAL}/{propietarioId}") { back ->
            AddAnimalScreen(
                navController,
                animalIdParam = null,
                propietarioIdParam = back.arguments?.getString("propietarioId")
            )
        }

        composable("${NavRoutes.EDIT_ANIMAL}/{propietarioId}/{animalId}") { back ->
            AddAnimalScreen(
                navController,
                animalIdParam = back.arguments?.getString("animalId"),
                propietarioIdParam = back.arguments?.getString("propietarioId")
            )
        }

        // 🔵 INSUMOS
        composable(NavRoutes.SUPPLIES) { SuppliesScreen(navController) }
        composable(NavRoutes.ADD_SUPPLY) { AddSupplyScreen(navController) }
        composable("${NavRoutes.EDIT_SUPPLY}/{insumoId}") { back ->
            EditSupplyScreen(navController, back.arguments?.getString("insumoId") ?: "")
        }

        // 🔵 REPORTES / PRODUCCIÓN
        composable(NavRoutes.PRODUCTIONS) { ProductionScreen(navController) }
        composable(NavRoutes.REPORTS) { ReportsScreen(navController) }
        composable(NavRoutes.REPORTS_PRODUCTION) { ReportsProductionScreen(navController) }
        composable(NavRoutes.REPORTS_EXPORT) { GenerateReportScreen(navController) }

        // 🔵 IMPORTAR EXCEL (CORRECTO)
        composable(NavRoutes.EXCEL_IMPORT) { ExcelImportScreen(navController) }
    }
}
