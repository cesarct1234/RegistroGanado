package com.caycedo.registroganado.ui.compose.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.caycedo.registroganado.ui.compose.screens.AnimalListScreen
import com.caycedo.registroganado.ui.compose.screens.ProductionScreen
import com.caycedo.registroganado.ui.compose.screens.reports.GenerateReportScreen
import com.caycedo.registroganado.ui.compose.screens.reports.ReportsProductionScreen
import com.caycedo.registroganado.ui.compose.screens.reports.ReportsScreen
import com.caycedo.registroganado.ui.compose.screens.supplies.AddSupplyScreen
import com.caycedo.registroganado.ui.compose.screens.supplies.EditSupplyScreen
import com.caycedo.registroganado.ui.compose.screens.supplies.SuppliesScreen

import com.caycedo.registroganado.ui_compose.screens.*
import com.caycedo.registroganado.ui.compose.screens.users.*
import com.caycedo.registroganado.ui_compose.screens.roles.*

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {

    NavHost(
        navController = navController,
        startDestination = NavRoutes.WELCOME
    ) {

        //------------------------------------
        // AUTH
        //------------------------------------
        composable(NavRoutes.WELCOME) { WelcomeScreen(navController) }
        composable(NavRoutes.LOGIN) { LoginScreen(navController) }
        composable(NavRoutes.REGISTER) { RegisterScreen(navController) }

        //------------------------------------
        // HOMES POR ROL
        //------------------------------------
        composable(NavRoutes.ADMIN_HOME) { AdminHomeScreen(navController) }
        composable(NavRoutes.VET_HOME) { VetHomeScreen(navController) }
        composable(NavRoutes.CUIDADOR_HOME) { CuidadorHomeScreen(navController) }
        composable(NavRoutes.PROP_HOME) { PropHomeScreen(navController) }

        //------------------------------------
        // GESTIÓN DE USUARIOS
        //------------------------------------
        composable(NavRoutes.USERS_MANAGEMENT) { UserManagementScreen(navController) }
        composable(NavRoutes.CREATE_USER) { CreateUserScreen(navController) }
        composable("${NavRoutes.EDIT_USER}/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            EditUserScreen(navController, userId)
        }

        //------------------------------------
        // ANIMALES
        //------------------------------------
        composable(NavRoutes.LIST_ANIMALS) { AnimalListScreen(navController) }

        composable(NavRoutes.ADD_ANIMAL) { AddAnimalScreen(navController) }

        composable("${NavRoutes.ADD_ANIMAL}/{propId}/{animalId}") { backStackEntry ->
            val propId = backStackEntry.arguments?.getString("propId") ?: ""
            val animalId = backStackEntry.arguments?.getString("animalId") ?: ""
            AddAnimalScreen(navController, propId, animalId)
        }

        //------------------------------------
        // INSUMOS
        //------------------------------------
        composable(NavRoutes.SUPPLIES) { SuppliesScreen(navController) }
        composable(NavRoutes.ADD_SUPPLY) { AddSupplyScreen(navController) }
        composable("${NavRoutes.EDIT_SUPPLY}/{insumoId}") { backStackEntry ->
            val insumoId = backStackEntry.arguments?.getString("insumoId") ?: ""
            EditSupplyScreen(navController, insumoId)
        }

        //------------------------------------
        // REPORTES / PRODUCCIÓN
        //------------------------------------
        composable(NavRoutes.PRODUCTIONS) { ProductionScreen(navController) }
        composable(NavRoutes.REPORTS) { ReportsScreen(navController) }
        composable(NavRoutes.REPORTS_PRODUCTION) { ReportsProductionScreen(navController) }
        composable(NavRoutes.REPORTS_EXPORT) { GenerateReportScreen(navController) }
    }
}


