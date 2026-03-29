package com.civicguard.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.civicguard.app.ui.components.GlassCard
import com.civicguard.app.ui.components.InputField
import com.civicguard.app.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportIssueScreen(
    mainViewModel: MainViewModel,
    reporterName: String,
    onBack: () -> Unit
) {
    var type by remember { mutableStateOf("Pothole") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Report Issue", fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("Issue Details", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                InputField(
                    value = type,
                    onValueChange = { type = it },
                    label = "Issue Type (e.g., Pothole, Garbage)"
                )
                InputField(
                    value = location,
                    onValueChange = { location = it },
                    label = "Location Description"
                )
                InputField(
                    value = description,
                    onValueChange = { description = it },
                    label = "Detailed Description"
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = {
                        mainViewModel.addComplaint(
                            type = type,
                            location = location,
                            lat = 12.9716, // dummy lat
                            lng = 77.5946, // dummy lng
                            description = description,
                            reporter = reporterName,
                            imageUrl = null
                        )
                        onBack()
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                    shape = RoundedCornerShape(8.dp),
                    enabled = type.isNotBlank() && location.isNotBlank() && description.isNotBlank()
                ) {
                    Text("Submit Report", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}
