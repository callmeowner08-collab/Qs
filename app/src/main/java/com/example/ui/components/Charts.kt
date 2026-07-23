package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.ExportUtils

data class ChartSlice(
    val label: String,
    val value: Double,
    val color: Color
)

@Composable
fun QsDonutChart(
    slices: List<ChartSlice>,
    totalAmount: Double,
    modifier: Modifier = Modifier
) {
    val chartColors = listOf(
        Color(0xFF2563EB), // Blue
        Color(0xFFD97706), // Amber
        Color(0xFF10B981), // Emerald
        Color(0xFF8B5CF6), // Purple
        Color(0xFFEC4899), // Pink
        Color(0xFF06B6D4)  // Cyan
    )

    val validSlices = remember(slices) {
        val total = slices.sumOf { it.value }
        if (total <= 0) emptyList()
        else slices.mapIndexed { idx, slice ->
            slice.copy(color = chartColors[idx % chartColors.size])
        }
    }

    val total = remember(validSlices) { validSlices.sumOf { it.value } }

    val animateProgress = remember { Animatable(0f) }
    LaunchedEffect(validSlices) {
        animateProgress.animateTo(1f, animationSpec = tween(1000))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("donut_chart_card"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "DISTRIBUSI BIAYA RAB PROYEK",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 12.sp,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (validSlices.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada item RAB untuk menampilkan chart",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Donut Canvas
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            var startAngle = -90f
                            val strokeWidth = 26.dp.toPx()

                            validSlices.forEach { slice ->
                                val sweepAngle = ((slice.value / total) * 360f).toFloat() * animateProgress.value
                                drawArc(
                                    color = slice.color,
                                    startAngle = startAngle,
                                    sweepAngle = sweepAngle,
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth)
                                )
                                startAngle += sweepAngle
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "TOTAL BIAYA",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Text(
                                text = ExportUtils.formatRupiah(totalAmount),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Legend Column
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        validSlices.forEach { slice ->
                            val percent = if (total > 0) (slice.value / total) * 100 else 0.0
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(slice.color)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = slice.label,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = String.format("%.1f%%", percent),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QsSCurveChart(
    progressPercent: Float,
    totalWorkWeeks: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("scurve_chart_card"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PROYEKSI KURVA-S PROGRES PEKERJAAN",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Progres: ${String.format("%.1f", progressPercent)}%",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                val w = size.width
                val h = size.height

                // Grid lines
                val gridColor = Color.LightGray.copy(alpha = 0.3f)
                val gridStroke = 1.dp.toPx()

                for (i in 1..3) {
                    val y = h * (i / 4f)
                    drawLine(gridColor, Offset(0f, y), Offset(w, y), gridStroke)
                }

                // S-Curve Plan path (Theoretical Sigmoid curve)
                val planPath = Path().apply {
                    moveTo(0f, h)
                    for (x in 0..100) {
                        val xPos = w * (x / 100f)
                        // Sigmoid formula: y = 1 / (1 + exp(-k * (x - 50)))
                        val normalizedX = (x - 50) / 10f
                        val sigmoid = 1.0 / (1.0 + Math.exp(-normalizedX.toDouble()))
                        val yPos = h - (h * sigmoid.toFloat())
                        lineTo(xPos, yPos)
                    }
                }

                drawPath(
                    path = planPath,
                    color = Color.Gray.copy(alpha = 0.5f),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // Actual Progress Path
                val actualPath = Path().apply {
                    moveTo(0f, h)
                    val currentX = (progressPercent / 100f).coerceIn(0f, 1f) * w
                    val currentY = h - ((progressPercent / 100f).coerceIn(0f, 1f) * h)

                    // Control point for smooth actual curve
                    cubicTo(
                        currentX * 0.4f, h,
                        currentX * 0.7f, currentY + (h - currentY) * 0.2f,
                        currentX, currentY
                    )
                }

                drawPath(
                    path = actualPath,
                    color = Color(0xFF2563EB),
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )

                // Current Progress Marker Circle
                val currentX = (progressPercent / 100f).coerceIn(0f, 1f) * w
                val currentY = h - ((progressPercent / 100f).coerceIn(0f, 1f) * h)
                drawCircle(
                    color = Color(0xFFD97706),
                    radius = 6.dp.toPx(),
                    center = Offset(currentX, currentY)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp, 3.dp).background(Color.Gray.copy(alpha = 0.6f)))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Rencana Kurva-S", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp, 3.dp).background(Color(0xFF2563EB)))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Realisasi Fisik", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                }
                Text(
                    text = "Estimasi Durasi: ${String.format("%.1f", totalWorkWeeks)} Minggu",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}
