package com.webdevry.bloodlineascension

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.webdevry.bloodlineascension.viewmodel.GameViewModel

@Composable
fun MyNavHost(
    navController: NavHostController = rememberNavController(),
    gameViewModel: GameViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = "selection",
        modifier = Modifier
    ) {
        composable("selection") {
            SelectionScreen(
                onSelectionDone = { navController.navigate("imageSelection") },
                gameViewModel = gameViewModel
            )
        }
        composable("imageSelection") {
            ImageSelectionScreen(
                onSelectionDone = { navController.navigate("gameplay") },
                gameViewModel = gameViewModel
            )
        }
        composable("gameplay") {
            GameplayScreen(
                navController = navController, // add this param if GameplayScreen needs nav
                gameViewModel = gameViewModel,
                onQuit = { navController.popBackStack("selection", inclusive = false) }
            )
        }
        composable("stats") {
            StatsScreen(
                gameViewModel = gameViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
