package com.civicguard.app.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView
import com.civicguard.app.ui.viewmodel.MainViewModel
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueMapScreen(
    mainViewModel: MainViewModel,
    onBack: () -> Unit
) {
    val allComplaints by mainViewModel.complaints.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Issue Map", fontWeight = FontWeight.Bold) },
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
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            factory = { context ->
                MapView(context).apply {
                    setMultiTouchControls(true)
                    controller.setZoom(12.0)
                    controller.setCenter(GeoPoint(12.9716, 77.5946)) // Bangalore Default

                    // Add markers
                    allComplaints.forEach { complaint ->
                        val marker = Marker(this)
                        marker.position = GeoPoint(complaint.lat, complaint.lng)
                        marker.title = complaint.type
                        marker.snippet = "${complaint.status} - ${complaint.location}"
                        overlays.add(marker)
                    }
                }
            },
            update = { mapView ->
                mapView.overlays.clear()
                allComplaints.forEach { complaint ->
                    val marker = Marker(mapView)
                    marker.position = GeoPoint(complaint.lat, complaint.lng)
                    marker.title = complaint.type
                    marker.snippet = "${complaint.status} - ${complaint.location}"
                    mapView.overlays.add(marker)
                }
                mapView.invalidate()
            }
        )
    }
}
