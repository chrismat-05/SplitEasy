package com.spliteasy.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.spliteasy.ui.screens.AddExpenseScreen
import com.spliteasy.ui.screens.DashboardScreen
import com.spliteasy.ui.screens.GroupDetailScreen
import com.spliteasy.ui.screens.LoginScreen
import com.spliteasy.ui.screens.SettingsScreen

@Composable
fun SplitEasyApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("dashboard") {
            DashboardScreen(
                onGroupClick = { groupId ->
                    navController.navigate("group_detail/$groupId")
                },
                onSettingsClick = {
                    navController.navigate("settings")
                }
            )
        }

        composable("settings") {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "group_detail/{group_id}",
            arguments = listOf(navArgument("group_id") { type = NavType.IntType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getInt("group_id") ?: return@composable
            GroupDetailScreen(
                groupId = groupId,
                onBack = { navController.popBackStack() },
                onAddExpenseClick = { navController.navigate("add_expense/$groupId") }
            )
        }

        composable(
            route = "add_expense/{group_id}",
            arguments = listOf(navArgument("group_id") { type = NavType.IntType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getInt("group_id") ?: return@composable
            AddExpenseScreen(
                groupId = groupId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
