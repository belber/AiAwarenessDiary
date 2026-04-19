package com.aiawareness.diary.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aiawareness.diary.DiaryApplication
import com.aiawareness.diary.data.local.UserPreferences
import com.aiawareness.diary.ui.navigation.MainNavigation
import com.aiawareness.diary.ui.navigation.Screen
import com.aiawareness.diary.ui.screens.PrivacyConsentDialog
import com.aiawareness.diary.ui.theme.AiAwarenessDiaryTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            val navController = rememberNavController()
            val coroutineScope = rememberCoroutineScope()
            val currentBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = currentBackStackEntry?.destination?.route
            val isPrivacyPolicyAccepted by produceState<Boolean?>(initialValue = null, userPreferences) {
                userPreferences.isPrivacyPolicyAccepted.collect { value = it }
            }

            LaunchedEffect(isPrivacyPolicyAccepted) {
                if (isPrivacyPolicyAccepted == true) {
                    DiaryApplication.ensureApmStarted()
                }
            }

            AiAwarenessDiaryTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        MainNavigation(navController = navController)

                        if (
                            isPrivacyPolicyAccepted == false &&
                            currentRoute != Screen.PrivacyPolicy.route
                        ) {
                            PrivacyConsentDialog(
                                onAgree = {
                                    coroutineScope.launch {
                                        userPreferences.setPrivacyPolicyAccepted(true)
                                    }
                                },
                                onDisagree = { finish() },
                                onOpenPrivacyPolicy = {
                                    navController.navigate(Screen.PrivacyPolicy.route) {
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
