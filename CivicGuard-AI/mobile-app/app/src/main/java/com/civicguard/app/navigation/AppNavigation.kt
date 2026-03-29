package com.civicguard.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.civicguard.app.ui.screens.*
import com.civicguard.app.ui.viewmodel.AuthViewModel
import com.civicguard.app.ui.viewmodel.MainViewModel

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel = viewModel(),
    mainViewModel: MainViewModel = viewModel()
) {
    val navController = rememberNavController()
    val currentUser by authViewModel.currentUser.collectAsState()

    // Determine start destination
    val startDestination = if (currentUser == null) "login" 
                           else if (currentUser?.role == "official") "official_dashboard" 
                           else "citizen_dashboard"

    NavHost(navController = navController, startDestination = startDestination) {
        
        // Login Screen
        composable("login") {
            LoginScreen(
                onLoginCitizen = {
                    authViewModel.loginAsCitizen("Rahul M.")
                    navController.navigate("citizen_dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onLoginOfficial = {
                    authViewModel.loginAsOfficial("Rajesh Kumar")
                    navController.navigate("official_dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        // ---------- CITIZEN FLOW ----------
        composable("citizen_dashboard") {
            CitizenDashboardScreen(
                authViewModel = authViewModel,
                mainViewModel = mainViewModel,
                onNavigateToCommunity = { navController.navigate("community") },
                onNavigateToReport = { navController.navigate("report_issue") },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo("citizen_dashboard") { inclusive = true }
                    }
                }
            )
        }
        
        composable("community") {
            CommunityScreen(onBack = { navController.popBackStack() })
        }
        
        composable("report_issue") {
            ReportIssueScreen(
                mainViewModel = mainViewModel,
                reporterName = currentUser?.name ?: "Unknown",
                onBack = { navController.popBackStack() }
            )
        }

        // ---------- OFFICIAL FLOW ----------
        composable("official_dashboard") {
            OfficialDashboardScreen(
                authViewModel = authViewModel,
                mainViewModel = mainViewModel,
                onNavigateToList = { filter -> navController.navigate("issue_list/$filter") },
                onNavigateToMap = { navController.navigate("issue_map") },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo("official_dashboard") { inclusive = true }
                    }
                }
            )
        }

        composable(
            "issue_list/{filter}",
            arguments = listOf(navArgument("filter") { type = NavType.StringType })
        ) { backStackEntry ->
            val filter = backStackEntry.arguments?.getString("filter") ?: "All"
            IssueListScreen(
                filter = filter,
                mainViewModel = mainViewModel,
                onNavigateToDetail = { id -> navController.navigate("issue_detail/$id") },
                onBack = { navController.popBackStack() }
            )
        }

        composable("issue_map") {
            IssueMapScreen(
                mainViewModel = mainViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            "issue_detail/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            ComplaintDetailScreen(
                complaintId = id,
                mainViewModel = mainViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
