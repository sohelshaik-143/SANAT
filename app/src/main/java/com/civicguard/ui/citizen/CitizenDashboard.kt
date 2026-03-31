package com.civicguard.ui.citizen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.civicguard.data.remote.dto.ComplaintResponse
import com.civicguard.ui.theme.BluePrimary
import com.civicguard.ui.theme.SuccessGreen
import com.civicguard.ui.theme.WarningOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitizenDashboard(
    onAddComplaint: () -> Unit,
    viewModel: CitizenDashboardViewModel = hiltViewModel()
) {
    val complaints by viewModel.complaints.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Citizen Dashboard", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { /* TODO: Notifications */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BluePrimary, titleContentColor = Color.White, actionIconContentColor = Color.White)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddComplaint, containerColor = BluePrimary, contentColor = Color.White) {
                Icon(Icons.Default.Add, contentDescription = "Add Complaint")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (complaints.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No complaints found. Tap + to report an issue.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(complaints) { complaint ->
                        ComplaintItem(complaint)
                    }
                }
            }
        }
    }
}

@Composable
fun ComplaintItem(complaint: ComplaintResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = complaint.category.uppercase(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BluePrimary
                )
                StatusBadge(complaint.status)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(text = complaint.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(text = "Ticket: ${complaint.ticketNumber}", fontSize = 14.sp, color = Color.Gray)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (complaint.authentic) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Verified Authentic by AI", fontSize = 12.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val color = when(status) {
        "SUBMITTED" -> Color.Gray
        "ASSIGNED" -> WarningOrange
        "RESOLVED" -> SuccessGreen
        "PENDING_VERIFICATION" -> BluePrimary
        else -> Color.Gray
    }
    
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
