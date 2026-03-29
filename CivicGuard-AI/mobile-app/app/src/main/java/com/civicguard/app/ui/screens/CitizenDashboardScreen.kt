package com.civicguard.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.civicguard.app.model.Complaint
import com.civicguard.app.ui.components.GlassCard
import com.civicguard.app.ui.components.StatusBadge
import com.civicguard.app.ui.viewmodel.AuthViewModel
import com.civicguard.app.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitizenDashboardScreen(
    authViewModel: AuthViewModel,
    mainViewModel: MainViewModel,
    onNavigateToCommunity: () -> Unit,
    onNavigateToReport: () -> Unit,
    onLogout: () -> Unit
) {
    val user by authViewModel.currentUser.collectAsState()
    val allComplaints by mainViewModel.complaints.collectAsState()
    
    // Filter complaints for this user (mocking it simple)
    val userComplaints = allComplaints.filter { it.reporter == (user?.name ?: "") || it.reporter == "Rahul M." }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Citizen Portal", fontWeight = FontWeight.Bold) },
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToReport,
                containerColor = Color(0xFF3B82F6),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Report Issue")
            }
        },
        containerColor = Color(0xFF0F172A)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Welcome back, ${user?.name ?: "Citizen"}",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                // Quick Action
                GlassCard(
                    modifier = Modifier.clickable { onNavigateToCommunity() }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Community Hub", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text("Engage with local initiatives", color = Color(0xFFA0AEC0), fontSize = 14.sp)
                        }
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF3B82F6).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("→", color = Color(0xFF3B82F6), fontSize = 20.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Your Reports",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            items(userComplaints) { complaint ->
                ComplaintItemCard(complaint)
            }
            
            if (userComplaints.isEmpty()) {
                item {
                    Text(
                        text = "You haven't reported any issues yet.",
                        color = Color(0xFFA0AEC0),
                        modifier = Modifier.padding(top = 24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ComplaintItemCard(complaint: Complaint, onClick: () -> Unit = {}) {
    GlassCard(modifier = Modifier
        .fillMaxWidth()
        .clickable { onClick() }) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = complaint.type,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFFA0AEC0),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = complaint.location,
                        color = Color(0xFFA0AEC0),
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = complaint.date,
                    color = Color(0xFFA0AEC0),
                    fontSize = 12.sp
                )
            }
            StatusBadge(status = complaint.status)
        }
    }
}
