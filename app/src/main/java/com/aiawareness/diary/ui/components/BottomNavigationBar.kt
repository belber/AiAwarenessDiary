package com.aiawareness.diary.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.aiawareness.diary.ui.navigation.Screen
import com.aiawareness.diary.ui.theme.BodyFontFamily
import com.aiawareness.diary.ui.theme.JournalTokens

@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    currentRoute: String
) {
    NavigationBar(
        containerColor = JournalTokens.SurfaceLow,
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Edit, contentDescription = "觉察") },
            label = { Text("觉察", fontFamily = BodyFontFamily, fontSize = 11.sp) },
            selected = currentRoute == Screen.Input.route,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = JournalTokens.Sage,
                selectedTextColor = JournalTokens.Sage,
                indicatorColor = JournalTokens.SageContainer,
                unselectedIconColor = JournalTokens.MutedInk,
                unselectedTextColor = JournalTokens.MutedInk
            ),
            onClick = {
                navController.navigate(Screen.Input.route) {
                    launchSingleTop = true
                    restoreState = true
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                    }
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "回顾") },
            label = { Text("回顾", fontFamily = BodyFontFamily, fontSize = 11.sp) },
            selected = currentRoute == Screen.Calendar.route,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = JournalTokens.Sage,
                selectedTextColor = JournalTokens.Sage,
                indicatorColor = JournalTokens.SageContainer,
                unselectedIconColor = JournalTokens.MutedInk,
                unselectedTextColor = JournalTokens.MutedInk
            ),
            onClick = {
                navController.navigate(Screen.Calendar.route) {
                    launchSingleTop = true
                    restoreState = true
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                    }
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = "设置") },
            label = { Text("设置", fontFamily = BodyFontFamily, fontSize = 11.sp) },
            selected = currentRoute == Screen.Settings.route,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = JournalTokens.Sage,
                selectedTextColor = JournalTokens.Sage,
                indicatorColor = JournalTokens.SageContainer,
                unselectedIconColor = JournalTokens.MutedInk,
                unselectedTextColor = JournalTokens.MutedInk
            ),
            onClick = {
                navController.navigate(Screen.Settings.route) {
                    launchSingleTop = true
                    restoreState = true
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                    }
                }
            }
        )
    }
}
