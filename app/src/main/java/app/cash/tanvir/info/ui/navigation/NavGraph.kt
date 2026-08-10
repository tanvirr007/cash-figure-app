package app.cash.tanvir.info.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.cash.tanvir.info.ui.screen.calculator.CalculatorScreen
import app.cash.tanvir.info.ui.screen.changelog.ChangelogScreen
import app.cash.tanvir.info.ui.screen.history.HistoryScreen
import app.cash.tanvir.info.ui.screen.report.ReportScreen
import app.cash.tanvir.info.ui.screen.settings.SettingsScreen

sealed class Screen(val route: String) {
    object Calculator : Screen("calculator")
    object History : Screen("history")
    object Report : Screen("report/{sheetId}?fromSave={fromSave}") {
        fun createRoute(sheetId: Long, fromSave: Boolean = false) = "report/$sheetId?fromSave=$fromSave"
    }
    object Changelog : Screen("changelog")
    object Settings : Screen("settings?autoCheck={autoCheck}") {
        fun createRoute(autoCheck: Boolean = false) = "settings?autoCheck=$autoCheck"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Calculator.route,
        enterTransition = {
            slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) + fadeIn(tween(300))
        },
        exitTransition = {
            fadeOut(tween(200))
        },
        popEnterTransition = {
            fadeIn(tween(300))
        },
        popExitTransition = {
            slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) + fadeOut(tween(200))
        }
    ) {
        composable(Screen.Calculator.route) {
            CalculatorScreen(
                onNavigateToHistory = { navController.navigate(Screen.History.route) },
                onNavigateToReport = { sheetId, fromSave -> navController.navigate(Screen.Report.createRoute(sheetId, fromSave)) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.createRoute()) }
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
            arguments = listOf(
                navArgument("sheetId") { type = NavType.LongType },
                navArgument("fromSave") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) {
            ReportScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Settings.route,
            arguments = listOf(
                navArgument("autoCheck") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToChangelog = { navController.navigate(Screen.Changelog.route) },
                autoCheck = it.arguments?.getBoolean("autoCheck") ?: false
            )
        }

        composable(Screen.Changelog.route) {
            ChangelogScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
