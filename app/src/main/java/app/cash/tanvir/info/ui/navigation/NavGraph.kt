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
import app.cash.tanvir.info.ui.screen.about.AboutScreen
import app.cash.tanvir.info.ui.screen.calculator.CalculatorScreen
import app.cash.tanvir.info.ui.screen.changelog.ChangelogScreen
import app.cash.tanvir.info.ui.screen.history.HistoryScreen
import app.cash.tanvir.info.ui.screen.report.ReportScreen
import app.cash.tanvir.info.ui.screen.settings.SettingsScreen
import app.cash.tanvir.info.ui.screen.settingsdetail.SettingsDetailScreen
import app.cash.tanvir.info.ui.screen.settingsdetail.SettingsSection
import app.cash.tanvir.info.ui.screen.update.UpdateScreen

sealed class Screen(val route: String) {
    object Calculator : Screen("calculator?loadDraftId={loadDraftId}") {
        fun createRoute(loadDraftId: Long = -1L) = "calculator?loadDraftId=$loadDraftId"
    }
    object History : Screen("history")
    object Report : Screen("report/{sheetId}?fromSave={fromSave}&fromDraft={fromDraft}") {
        fun createRoute(sheetId: Long, fromSave: Boolean = false, fromDraft: Boolean = false) =
            "report/$sheetId?fromSave=$fromSave&fromDraft=$fromDraft"
    }
    object Changelog : Screen("changelog")
    object Update : Screen("update")
    object About : Screen("about")
    object SettingsDetail : Screen("settings-detail?section={section}") {
        fun createRoute(section: SettingsSection) = "settings-detail?section=${section.routeParam}"
    }
    object Settings : Screen("settings")
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
        composable(
            route = Screen.Calculator.route,
            arguments = listOf(
                navArgument("loadDraftId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) {
            CalculatorScreen(
                onNavigateToHistory = { navController.navigate(Screen.History.route) },
                onNavigateToReport = { sheetId, fromSave -> navController.navigate(Screen.Report.createRoute(sheetId, fromSave)) },
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
            arguments = listOf(
                navArgument("sheetId") { type = NavType.LongType },
                navArgument("fromSave") {
                    type = NavType.BoolType
                    defaultValue = false
                },
                navArgument("fromDraft") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) {
            ReportScreen(
                onNavigateBack = { navController.popBackStack() },
                onLoadIntoCalculator = { draftId ->
                    navController.navigate(Screen.Calculator.createRoute(loadDraftId = draftId)) {
                        popUpTo(Screen.Calculator.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = Screen.Settings.route
        ) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToChangelog = { navController.navigate(Screen.Changelog.route) },
                onNavigateToUpdate = { navController.navigate(Screen.Update.route) },
                onNavigateToAbout = { navController.navigate(Screen.About.route) },
                onNavigateToSettingsDetail = { section ->
                    navController.navigate(Screen.SettingsDetail.createRoute(section))
                },
                onNavigateToDraftReport = { draftId ->
                    navController.navigate(Screen.Report.createRoute(draftId, fromDraft = true))
                }
            )
        }

        composable(Screen.Changelog.route) {
            ChangelogScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Update.route) {
            UpdateScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.About.route) {
            AboutScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.SettingsDetail.route,
            arguments = listOf(
                navArgument("section") { type = NavType.StringType }
            )
        ) {
            val section = SettingsSection.fromRouteParam(
                it.arguments?.getString("section")
            )
            SettingsDetailScreen(
                section = section,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
