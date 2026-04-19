package com.aiawareness.diary.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.aiawareness.diary.ui.screens.AboutScreen
import com.aiawareness.diary.ui.screens.AiConfigScreen
import com.aiawareness.diary.ui.screens.AiGuideWebViewScreen
import com.aiawareness.diary.ui.screens.CalendarScreen
import com.aiawareness.diary.ui.screens.DataManagementScreen
import com.aiawareness.diary.ui.screens.InputScreen
import com.aiawareness.diary.ui.screens.PrivacyPolicyScreen
import com.aiawareness.diary.ui.screens.SettingsScreen

sealed class Screen(val route: String) {
    data object Input : Screen("input")
    data object Calendar : Screen("calendar")
    data object Settings : Screen("settings")
    data object AiConfig : Screen("ai_config")
    data object AiGuide : Screen("ai_guide")
    data object DataManagement : Screen("data_management")
    data object About : Screen("about")
    data object PrivacyPolicy : Screen("privacy_policy")
}

@Composable
fun MainNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Input.route,
        modifier = modifier
    ) {
        composable(Screen.Input.route) {
            InputScreen(
                onNavigateToCalendar = { navController.navigate(Screen.Calendar.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.Calendar.route) {
            CalendarScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAiConfig = { navController.navigate(Screen.AiConfig.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAiConfig = { navController.navigate(Screen.AiConfig.route) },
                onNavigateToDataManagement = { navController.navigate(Screen.DataManagement.route) },
                onNavigateToAbout = { navController.navigate(Screen.About.route) }
            )
        }

        composable(Screen.AiConfig.route) {
            AiConfigScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAiGuide = { navController.navigate(Screen.AiGuide.route) }
            )
        }

        composable(Screen.AiGuide.route) {
            AiGuideWebViewScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.DataManagement.route) {
            DataManagementScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.About.route) {
            AboutScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPrivacyPolicy = { navController.navigate(Screen.PrivacyPolicy.route) }
            )
        }

        composable(Screen.PrivacyPolicy.route) {
            PrivacyPolicyScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
