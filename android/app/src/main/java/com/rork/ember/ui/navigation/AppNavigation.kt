package com.rork.ember.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rork.ember.ui.screens.AddHabitScreen
import com.rork.ember.ui.screens.HabitDetailScreen
import com.rork.ember.ui.screens.HomeScreen
import com.rork.ember.ui.screens.PaywallScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home",
    ) {
        composable("home") { HomeScreen(navController = navController) }
        composable("add") { AddHabitScreen(navController = navController) }
        composable(
            "edit/{habitId}",
            arguments = listOf(navArgument("habitId") { type = NavType.StringType }),
        ) { entry ->
            AddHabitScreen(
                navController = navController,
                editingId = entry.arguments?.getString("habitId"),
            )
        }
        composable("paywall") { PaywallScreen(navController = navController) }
        composable(
            "habit/{habitId}",
            arguments = listOf(navArgument("habitId") { type = NavType.StringType }),
        ) { entry ->
            HabitDetailScreen(
                navController = navController,
                habitId = entry.arguments?.getString("habitId") ?: "",
            )
        }
    }
}
