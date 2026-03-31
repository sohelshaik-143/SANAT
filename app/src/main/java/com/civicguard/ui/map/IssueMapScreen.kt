package com.civicguard.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.civicguard.ui.theme.BluePrimary

@Composable
fun IssueMapScreen() {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.LightGray.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = BluePrimary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Geographic Issue Map",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Real-time visualization of civic reports",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            
            // In a real implementation, we would use:
            // GoogleMap(
            //     modifier = Modifier.fillMaxSize(),
            //     cameraPositionState = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(LatLng(12.9716, 77.5946), 12f) }
            // ) {
            //     markers.forEach { Marker(state = MarkerState(position = it.latLng), title = it.title) }
            // }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Map Legend", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                LegendItem("Road Damage", Color.Red)
                LegendItem("Water Supply", Color.Blue)
                LegendItem("Garbage", Color.Magenta)
                LegendItem("Street Lights", Color.Yellow)
            }
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Surface(modifier = Modifier.size(12.dp), shape = RoundedCornerShape(2.dp), color = color) {}
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, fontSize = 12.sp)
    }
}
