package com.civicguard.ui.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.civicguard.ui.theme.BluePrimary
import com.civicguard.ui.theme.SuccessGreen
import com.civicguard.ui.theme.WarningOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Performance & RTI", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BluePrimary, titleContentColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            ChartCard("Issue Distribution", "By Category") {
                CategoryPieChart()
            }

            ChartCard("SLA Compliance", "Resolved vs Pending") {
                ComplianceBarChart()
            }

            Text("Monthly Trends", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            TrendLineChart()
        }
    }
}

@Composable
fun ChartCard(title: String, subtitle: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(subtitle, fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun CategoryPieChart() {
    Canvas(modifier = Modifier.size(200.dp).padding(16.dp).aspectRatio(1f)) {
        drawArc(color = BluePrimary, startAngle = 0f, sweepAngle = 120f, useCenter = true)
        drawArc(color = SuccessGreen, startAngle = 120f, sweepAngle = 100f, useCenter = true)
        drawArc(color = WarningOrange, startAngle = 220f, sweepAngle = 80f, useCenter = true)
        drawArc(color = Color.LightGray, startAngle = 300f, sweepAngle = 60f, useCenter = true)
    }
}

@Composable
fun ComplianceBarChart() {
    Row(
        modifier = Modifier.fillMaxWidth().height(150.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        val bars = listOf(Pair("Jan", 0.8f), Pair("Feb", 0.9f), Pair("Mar", 0.75f))
        bars.forEach { (label, value) ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    modifier = Modifier.width(32.dp).fillMaxHeight(value),
                    color = BluePrimary,
                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                ) {}
                Text(label, fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun TrendLineChart() {
    Canvas(modifier = Modifier.fillMaxWidth().height(150.dp)) {
        val points = listOf(Offset(0f, 150f), Offset(100f, 80f), Offset(200f, 120f), Offset(300f, 50f))
        for (i in 0 until points.size - 1) {
            drawLine(
                color = BluePrimary,
                start = points[i],
                end = points[i+1],
                strokeWidth = 4f
            )
        }
    }
}
