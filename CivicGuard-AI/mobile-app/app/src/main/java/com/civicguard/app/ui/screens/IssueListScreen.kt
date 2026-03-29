package com.civicguard.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.civicguard.app.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueListScreen(
    filter: String,
    mainViewModel: MainViewModel,
    onNavigateToDetail: (String) -> Unit,
    onBack: () -> Unit
) {
    val allComplaints by mainViewModel.complaints.collectAsState()
    
    val displayedComplaints = when (filter) {
        "Active" -> allComplaints.filter { it.status != "Resolved" && it.status != "Rejected" }
        "Resolved" -> allComplaints.filter { it.status == "Resolved" }
        else -> allComplaints
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$filter Issues", fontWeight = FontWeight.Bold) },
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
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(displayedComplaints) { complaint ->
                ComplaintItemCard(complaint) {
                    onNavigateToDetail(complaint.id)
                }
            }
            if (displayedComplaints.isEmpty()) {
                item {
                    Text("No $filter issues found.", color = Color(0xFFA0AEC0))
                }
            }
        }
    }
}
