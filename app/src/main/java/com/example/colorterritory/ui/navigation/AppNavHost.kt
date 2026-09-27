package com.example.colorterritory.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.colorterritory.ui.GameViewModel
import com.example.colorterritory.ui.SettingsViewModel
import com.example.colorterritory.ui.screens.AboutScreen
import com.example.colorterritory.ui.screens.GameScreen
import com.example.colorterritory.ui.screens.HomeScreen
import com.example.colorterritory.ui.screens.HowToPlayScreen
import com.example.colorterritory.ui.screens.ResultScreen
import com.example.colorterritory.ui.screens.SettingsScreen
import com.example.colorterritory.ui.screens.SplashScreen

object Routes {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val GAME = "game"
    const val RESULT = "result"
    const val HOW_TO_PLAY = "how_to_play"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
}

@Composable
fun AppNavHost(
    settingsViewModel: SettingsViewModel,
    navController: NavHostController = rememberNavController()
) {
    // Activity-scoped so Game and Result share the same match.
    val gameViewModel: GameViewModel = viewModel()
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
    val stats by settingsViewModel.stats.collectAsStateWithLifecycle()
    val gameUi by gameViewModel.uiState.collectAsStateWithLifecycle()

    fun goHome() {
        if (!navController.popBackStack(Routes.HOME, inclusive = false)) {
            navController.navigate(Routes.HOME) { popUpTo(0) }
        }
    }

    fun startGame() {
        gameViewModel.newGame()
        navController.navigate(Routes.GAME) {
            popUpTo(Routes.HOME)
            launchSingleTop = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        enterTransition = { fadeIn(tween(220)) },
        exitTransition = { fadeOut(tween(180)) },
        popEnterTransition = { fadeIn(tween(220)) },
        popExitTransition = { fadeOut(tween(180)) }
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(onFinished = {
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }
            })
        }
        composable(Routes.HOME) {
            HomeScreen(
                stats = stats,
                hasMatchInProgress = gameUi.hasMatchInProgress,
                onPlay = ::startGame,
                onResume = { navController.navigate(Routes.GAME) { launchSingleTop = true } },
                onHowToPlay = { navController.navigate(Routes.HOW_TO_PLAY) },
                onSettings = { navController.navigate(Routes.SETTINGS) },
                onAbout = { navController.navigate(Routes.ABOUT) }
            )
        }
        composable(Routes.GAME) {
            GameScreen(
                viewModel = gameViewModel,
                settings = settings,
                onGameFinished = {
                    navController.navigate(Routes.RESULT) {
                        popUpTo(Routes.HOME)
                        launchSingleTop = true
                    }
                },
                onHome = ::goHome
            )
        }
        composable(Routes.RESULT) {
            ResultScreen(
                game = gameUi.game,
                durationMs = gameUi.matchDurationMs,
                onPlayAgain = ::startGame,
                onHome = ::goHome
            )
        }
        composable(Routes.HOW_TO_PLAY) { HowToPlayScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                settings = settings,
                stats = stats,
                viewModel = settingsViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.ABOUT) { AboutScreen(onBack = { navController.popBackStack() }) }
    }
}
