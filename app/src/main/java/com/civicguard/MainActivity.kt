package com.civicguard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.civicguard.ui.auth.LoginScreen
import com.civicguard.ui.auth.RegisterScreen
import com.civicguard.ui.citizen.CitizenDashboard
import com.civicguard.ui.citizen.SubmitComplaintScreen
import com.civicguard.ui.officer.OfficerDashboard
import com.civicguard.ui.officer.ResolveComplaintScreen
import com.civicguard.ui.theme.BluePrimary
import com.civicguard.ui.theme.CivicEyeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CivicEyeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onLoginSuccess = { role ->
                    if (role == "CITIZEN") {
                        navController.navigate("citizen_main") {
                            popUpTo("login") { inclusive = true }
                        }
                    } else {
                        navController.navigate("officer_dashboard") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                },
                onRegisterNavigate = {
                    navController.navigate("register")
                }
            )
        }
        composable("register") {
            RegisterScreen(
                onBack = { navController.popBackStack() },
                onRegisterSuccess = { 
                    navController.navigate("citizen_main") {
                        popUpTo("register") { inclusive = true }
                    }
                }
            )
        }
        composable("citizen_main") {
            CitizenMainScaffold(
                onNavigateToSubmit = { navController.navigate("submit_complaint") },
                onLogout = { 
                    navController.navigate("login") {
                        popUpTo("citizen_main") { inclusive = true }
                    }
                }
            )
        }
        composable("submit_complaint") {
            SubmitComplaintScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("officer_dashboard") {
            OfficerDashboard(
                onResolveClick = { id -> navController.navigate("resolve_complaint/$id") }
            )
        }
        composable("resolve_complaint/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            ResolveComplaintScreen(
                complaintId = id,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun CitizenMainScaffold(
    onNavigateToSubmit: () -> Unit,
    onLogout: () -> Unit
) {
    val bottomNavController = rememberNavController()
    val items = listOf(
        NavigationItem("Dashboard", Icons.Default.Home, "dashboard"),
        NavigationItem("Map", Icons.Default.Map, "map"),
        NavigationItem("Analytics", Icons.Default.BarChart, "analytics"),
        NavigationItem("Profile", Icons.Default.Settings, "profile")
    )

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 8.dp) {
                val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = currentRoute == item.route,
                        onClick = {
                            bottomNavController.navigate(item.route) {
                                popUpTo(bottomNavController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = BluePrimary, selectedTextColor = BluePrimary)
                    )
                }
            }
        }
    ) { padding ->
        NavHost(navController = bottomNavController, startDestination = "dashboard", modifier = Modifier.padding(padding)) {
            composable("dashboard") { CitizenDashboard(onAddComplaint = onNavigateToSubmit) }
            composable("map") { com.civicguard.ui.map.IssueMapScreen() }
            composable("analytics") { com.civicguard.ui.analytics.AnalyticsScreen() }
            composable("profile") { com.civicguard.ui.profile.ProfileScreen(onLogout = onLogout) }
        }
    }
}

data class NavigationItem(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val route: String)
