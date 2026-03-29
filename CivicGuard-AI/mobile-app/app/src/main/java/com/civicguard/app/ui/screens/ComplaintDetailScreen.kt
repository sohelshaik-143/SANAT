package com.civicguard.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.civicguard.app.ui.components.GlassCard
import com.civicguard.app.ui.components.StatusBadge
import com.civicguard.app.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplaintDetailScreen(
    complaintId: String,
    mainViewModel: MainViewModel,
    onBack: () -> Unit
) {
    val allComplaints by mainViewModel.complaints.collectAsState()
    val complaint = allComplaints.find { it.id == complaintId }

    if (complaint == null) {
        onBack()
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(complaint.id, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF0F172A)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(complaint.type, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        StatusBadge(status = complaint.status)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Date: ${complaint.date}", color = Color(0xFFA0AEC0))
                    Text("Reporter: ${complaint.reporter}", color = Color(0xFFA0AEC0))
                    Text("Location: ${complaint.location}", color = Color(0xFFA0AEC0))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(complaint.description, color = Color.White)
                }
            }

            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("AI Verification", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Confidence Score: ${complaint.aiConfidence}%", color = Color(0xFF22C55E), fontWeight = FontWeight.Bold)
                    Text("Automated Triage: Relevant to ${complaint.department}", color = Color(0xFFA0AEC0))
                }
            }

            // Action buttons
            item {
                Spacer(modifier = Modifier.height(24.dp))
                if (complaint.status != "Resolved") {
                    Button(
                        onClick = {
                            mainViewModel.updateStatus(complaint.id, "Resolved")
                            onBack()
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Mark Resolved", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                if (complaint.status == "Pending") {
                    Button(
                        onClick = {
                            mainViewModel.updateStatus(complaint.id, "In Progress")
                            onBack()
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Triaged / In Progress", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
