package com.civicguard.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.civicguard.app.ui.components.GlassCard
import com.civicguard.app.ui.viewmodel.AuthViewModel
import com.civicguard.app.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfficialDashboardScreen(
    authViewModel: AuthViewModel,
    mainViewModel: MainViewModel,
    onNavigateToList: (String) -> Unit,
    onNavigateToMap: () -> Unit,
    onLogout: () -> Unit
) {
    val user by authViewModel.currentUser.collectAsState()
    val allComplaints by mainViewModel.complaints.collectAsState()

    val pendingCount = allComplaints.count { it.status == "Pending" || it.status == "In Progress" || it.status == "Escalated" }
    val resolvedCount = allComplaints.count { it.status == "Resolved" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Command Center", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A),
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = Color.White)
                    }
                }
            )
        },
        containerColor = Color(0xFF0F172A)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Welcome, Officer ${user?.name ?: ""}",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = user?.designation ?: "Dept: ${user?.department ?: "Triage"}",
                color = Color(0xFFA0AEC0),
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(24.dp))
            
            // Stats Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(0.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    StatCard("Active Issues", pendingCount.toString(), Color(0xFFEAB308)) {
                        onNavigateToList("Active")
                    }
                }
                item {
                    StatCard("Resolved", resolvedCount.toString(), Color(0xFF22C55E)) {
                        onNavigateToList("Resolved")
                    }
                }
                item {
                    StatCard("Issue Map", "View", Color(0xFF3B82F6)) {
                        onNavigateToMap()
                    }
                }
                item {
                    StatCard("AI Triage", "${allComplaints.size}", Color(0xFF8B5CF6)) {
                        onNavigateToList("All")
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, accentColor: Color, onClick: () -> Unit) {
    GlassCard(modifier = Modifier
        .fillMaxWidth()
        .clickable { onClick() }
        .height(100.dp)) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = accentColor)
            Text(text = label, fontSize = 14.sp, color = Color(0xFFA0AEC0))
        }
    }
}
