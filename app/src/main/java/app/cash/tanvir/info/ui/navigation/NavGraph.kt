package app.cash.tanvir.info.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.cash.tanvir.info.ui.animation.screenEnterTransition
import app.cash.tanvir.info.ui.animation.screenExitTransition
import app.cash.tanvir.info.ui.animation.screenPopEnterTransition
import app.cash.tanvir.info.ui.animation.screenPopExitTransition
import app.cash.tanvir.info.ui.animation.shouldReduceMotion
import app.cash.tanvir.info.ui.animation.tabEnterTransition
import app.cash.tanvir.info.ui.animation.tabExitTransition
import app.cash.tanvir.info.ui.animation.tabPopEnterTransition
import app.cash.tanvir.info.ui.animation.tabPopExitTransition
import app.cash.tanvir.info.ui.screen.about.AboutScreen
import app.cash.tanvir.info.ui.screen.calculator.CalculatorScreen
import app.cash.tanvir.info.ui.screen.changelog.ChangelogScreen
import app.cash.tanvir.info.ui.screen.draft.DraftScreen
import app.cash.tanvir.info.ui.screen.history.HistoryScreen
import app.cash.tanvir.info.ui.screen.report.ReportScreen
import app.cash.tanvir.info.ui.screen.settings.SettingsScreen
import app.cash.tanvir.info.ui.screen.settingsdetail.SettingsDetailScreen
import app.cash.tanvir.info.ui.screen.settingsdetail.SettingsSection
import app.cash.tanvir.info.ui.screen.update.UpdateScreen
import app.cash.tanvir.info.util.HapticHelper

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
    object Draft : Screen("draft")
}

/**
 * True tab architecture: a single outer Scaffold hosts the persistent bottom
 * navigation bar (visible only on the three main tabs) around the NavHost.
 * Tab switches use popUpTo(start) + launchSingleTop + save/restoreState so
 * destinations never stack duplicates and back always returns to Calculator.
 */
@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    isBangla: Boolean = false
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val context = LocalContext.current
    val reducedMotion = shouldReduceMotion()

    Scaffold(
        bottomBar = {
            if (currentRoute.isMainTab()) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute.isCalculatorTab(),
                        onClick = {
                            HapticHelper.vibrate(context)
                            navController.navigateToTab(Screen.Calculator.createRoute())
                        },
                        icon = { Icon(Icons.Filled.Calculate, contentDescription = null) },
                        label = { Text(if (isBangla) "ক্যালকুলেটর" else "Calculator") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.History.route,
                        onClick = {
                            HapticHelper.vibrate(context)
                            navController.navigateToTab(Screen.History.route)
                        },
                        icon = { Icon(Icons.Filled.History, contentDescription = null) },
                        label = { Text(if (isBangla) "ইতিহাস" else "History") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.Settings.route,
                        onClick = {
                            HapticHelper.vibrate(context)
                            navController.navigateToTab(Screen.Settings.route)
                        },
                        icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                        label = { Text(if (isBangla) "সেটিংস" else "Settings") }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Calculator.route,
            modifier = Modifier.padding(paddingValues),
            enterTransition = { screenEnterTransition(reducedMotion) },
            exitTransition = { screenExitTransition(reducedMotion) },
            popEnterTransition = { screenPopEnterTransition(reducedMotion) },
            popExitTransition = { screenPopExitTransition(reducedMotion) }
        ) {
            composable(
                route = Screen.Calculator.route,
                arguments = listOf(
                    navArgument("loadDraftId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                ),
                enterTransition = { tabEnterTransition(reducedMotion) },
                exitTransition = { tabExitTransition(reducedMotion) },
                popEnterTransition = { tabPopEnterTransition(reducedMotion) },
                popExitTransition = { tabPopExitTransition(reducedMotion) }
            ) {
                CalculatorScreen(
                    onNavigateToReport = { sheetId, fromSave -> navController.navigate(Screen.Report.createRoute(sheetId, fromSave)) }
                )
            }

            composable(
                route = Screen.History.route,
                enterTransition = { tabEnterTransition(reducedMotion) },
                exitTransition = { tabExitTransition(reducedMotion) },
                popEnterTransition = { tabPopEnterTransition(reducedMotion) },
                popExitTransition = { tabPopExitTransition(reducedMotion) }
            ) {
                HistoryScreen(
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
                route = Screen.Settings.route,
                enterTransition = { tabEnterTransition(reducedMotion) },
                exitTransition = { tabExitTransition(reducedMotion) },
                popEnterTransition = { tabPopEnterTransition(reducedMotion) },
                popExitTransition = { tabPopExitTransition(reducedMotion) }
            ) {
                SettingsScreen(
                    onNavigateToChangelog = { navController.navigate(Screen.Changelog.route) },
                    onNavigateToUpdate = { navController.navigate(Screen.Update.route) },
                    onNavigateToAbout = { navController.navigate(Screen.About.route) },
                    onNavigateToSettingsDetail = { section ->
                        navController.navigate(Screen.SettingsDetail.createRoute(section))
                    },
                    onNavigateToDraft = {
                        navController.navigate(Screen.Draft.route)
                    }
                )
            }

            composable(Screen.Draft.route) {
                DraftScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onOpenDraft = { draftId ->
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
}

/**
 * Bottom-nav tab switch: keeps the start destination (Calculator) at the stack
 * root, never stacks duplicate tabs, and saves/restores each tab's state.
 */
private fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

private fun String?.isMainTab(): Boolean =
    isCalculatorTab() || this == Screen.History.route || this == Screen.Settings.route

private fun String?.isCalculatorTab(): Boolean = this?.startsWith("calculator") == true
