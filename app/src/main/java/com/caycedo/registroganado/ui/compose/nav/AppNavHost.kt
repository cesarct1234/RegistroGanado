package com.caycedo.registroganado.ui.compose.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

// ✅ Importaciones correctas y actualizadas
import com.caycedo.registroganado.ui_compose.screens.WelcomeScreen
import com.caycedo.registroganado.ui_compose.screens.LoginScreen
import com.caycedo.registroganado.ui_compose.screens.RegisterScreen
import com.caycedo.registroganado.ui.compose.screens.DashboardScreen
import com.caycedo.registroganado.ui.compose.screens.AnimalListScreen
import com.caycedo.registroganado.ui_compose.screens.AddAnimalScreen
import com.caycedo.registroganado.ui.compose.screens.SuppliesScreen
import com.caycedo.registroganado.ui.compose.screens.AddSupplyScreen
import com.caycedo.registroganado.ui.compose.screens.EditSupplyScreen
import com.caycedo.registroganado.ui.compose.screens.GenerateReportScreen
import com.caycedo.registroganado.ui.compose.screens.ProductionScreen
import com.caycedo.registroganado.ui.compose.screens.ReportsProductionScreen
import com.caycedo.registroganado.ui.compose.screens.ReportsScreen


@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = NavRoutes.WELCOME) {

        // 🏠 Pantallas iniciales
        composable(NavRoutes.WELCOME) { WelcomeScreen(navController) }
        composable(NavRoutes.LOGIN) { LoginScreen(navController) }
        composable(NavRoutes.REGISTER) { RegisterScreen(navController) }

        // 📋 Dashboard
        composable(NavRoutes.DASHBOARD) { DashboardScreen(navController) }

        // 🐮 Gestión de animales
        composable(NavRoutes.LIST_ANIMALS) { AnimalListScreen(navController) }
        composable(NavRoutes.ADD_ANIMAL) { AddAnimalScreen(navController) }

        // 🌾 Registro de insumos
        composable(NavRoutes.SUPPLIES) { SuppliesScreen(navController) }
        composable(NavRoutes.ADD_SUPPLY) { AddSupplyScreen(navController) }
        composable("${NavRoutes.EDIT_SUPPLY}/{insumoId}") { backStackEntry ->
            val insumoId = backStackEntry.arguments?.getString("insumoId") ?: ""
            EditSupplyScreen(navController, insumoId)
        }

        // 🧀 Producción
        composable(NavRoutes.PRODUCTIONS) { ProductionScreen(navController) }

        // 📊 Reportes
        composable(NavRoutes.REPORTS) { ReportsScreen(navController) }
        composable(NavRoutes.REPORTS_PRODUCTION) { ReportsProductionScreen(navController) }
        composable(NavRoutes.REPORTS_EXPORT) { GenerateReportScreen(navController) }

    }
}



