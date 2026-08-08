package app.cash.tanvir.info.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.cash.tanvir.info.ui.screen.calculator.CalculatorScreen
import app.cash.tanvir.info.ui.screen.history.HistoryScreen
import app.cash.tanvir.info.ui.screen.report.ReportScreen
import app.cash.tanvir.info.ui.screen.settings.SettingsScreen

sealed class Screen(val route: String) {
    object Calculator : Screen("calculator")
    object History : Screen("history")
    object Report : Screen("report/{sheetId}") {
        fun createRoute(sheetId: Long) = "report/$sheetId"
    }
    object Settings : Screen("settings")
}

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Calculator.route
    ) {
        composable(Screen.Calculator.route) {
            CalculatorScreen(
                onNavigateToHistory = { navController.navigate(Screen.History.route) },
                onNavigateToReport = { sheetId -> navController.navigate(Screen.Report.createRoute(sheetId)) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                onNavigateBack = { navController.popBackStack() },
                onSelectSheet = { sheet ->
                    navController.navigate(Screen.Report.createRoute(sheet.id))
                }
            )
        }

        composable(
            route = Screen.Report.route,
            arguments = listOf(navArgument("sheetId") { type = NavType.LongType })
        ) {
            ReportScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
