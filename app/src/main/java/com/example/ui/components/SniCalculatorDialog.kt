package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.QsVolumeCalculator

@Composable
fun SniCalculatorDialog(
    onDismiss: () -> Unit,
    onResultCalculated: (itemName: String, volume: Double, unit: String, recommendedPrice: Double) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Dinding", "Pondasi", "Beton Cor", "Atap", "Lantai Tile")

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("sni_calculator_dialog"),
        confirmButton = {},
        dismissButton = {},
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = "Kalkulator Volume SNI",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Kalkulator Volume SNI",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_calc_btn")) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    edgePadding = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(text = title, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (selectedTab) {
                    0 -> WallCalculatorContent(onResultCalculated, onDismiss)
                    1 -> FoundationCalculatorContent(onResultCalculated, onDismiss)
                    2 -> ConcreteCalculatorContent(onResultCalculated, onDismiss)
                    3 -> RoofCalculatorContent(onResultCalculated, onDismiss)
                    4 -> TileCalculatorContent(onResultCalculated, onDismiss)
                }
            }
        }
    )
}

@Composable
private fun WallCalculatorContent(
    onApply: (itemName: String, volume: Double, unit: String, price: Double) -> Unit,
    onDismiss: () -> Unit
) {
    var lengthStr by remember { mutableStateOf("12.0") }
    var heightStr by remember { mutableStateOf("3.5") }
    var doorWindowAreaStr by remember { mutableStateOf("4.2") }
    var wallType by remember { mutableStateOf("Pasangan Dinding Bata Ringan (Hebel) t=10cm") }

    val length = lengthStr.toDoubleOrNull() ?: 0.0
    val height = heightStr.toDoubleOrNull() ?: 0.0
    val doorArea = doorWindowAreaStr.toDoubleOrNull() ?: 0.0

    val netVolume = remember(length, height, doorArea) {
        QsVolumeCalculator.calcWallArea(length, height, doorArea)
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = lengthStr,
            onValueChange = { lengthStr = it },
            label = { Text("Panjang Total Dinding (meter)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().testTag("calc_wall_length_input")
        )

        OutlinedTextField(
            value = heightStr,
            onValueChange = { heightStr = it },
            label = { Text("Tinggi Dinding (meter)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().testTag("calc_wall_height_input")
        )

        OutlinedTextField(
            value = doorWindowAreaStr,
            onValueChange = { doorWindowAreaStr = it },
            label = { Text("Luas Kurangan Pintu/Jendela (m²)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().testTag("calc_wall_opening_input")
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "HASIL KALKULASI BERSIH SNI",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "${String.format("%.2f", netVolume)} m²",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "• Estimasi Hebel: ${String.format("%.2f", netVolume * 0.1)} m³ (${(netVolume * 8.33).toInt()} balok)\n• Estimasi Perekat Mortar: ${(netVolume * 4.0).toInt()} kg",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }

        Button(
            onClick = {
                onApply(wallType, netVolume, "m²", 145000.0)
                onDismiss()
            },
            modifier = Modifier.fillMaxWidth().testTag("apply_wall_calc_btn")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Masukkan ke RAB Proyek")
        }
    }
}

@Composable
private fun FoundationCalculatorContent(
    onApply: (itemName: String, volume: Double, unit: String, price: Double) -> Unit,
    onDismiss: () -> Unit
) {
    var topWStr by remember { mutableStateOf("0.3") }
    var botWStr by remember { mutableStateOf("0.6") }
    var heightStr by remember { mutableStateOf("0.7") }
    var totalLenStr by remember { mutableStateOf("45.0") }

    val topW = topWStr.toDoubleOrNull() ?: 0.0
    val botW = botWStr.toDoubleOrNull() ?: 0.0
    val h = heightStr.toDoubleOrNull() ?: 0.0
    val len = totalLenStr.toDoubleOrNull() ?: 0.0

    val volume = remember(topW, botW, h, len) {
        QsVolumeCalculator.calcFoundationVolume(topW, botW, h, len)
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = topWStr,
            onValueChange = { topWStr = it },
            label = { Text("Lebar Atas Pondasi (meter)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = botWStr,
            onValueChange = { botWStr = it },
            label = { Text("Lebar Bawah Pondasi (meter)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = heightStr,
            onValueChange = { heightStr = it },
            label = { Text("Tinggi Pondasi (meter)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = totalLenStr,
            onValueChange = { totalLenStr = it },
            label = { Text("Panjang Total Pondasi (meter)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "TOTAL VOLUME PONDASI BATU KALI",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${String.format("%.2f", volume)} m³",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "• Batu Belah: ${String.format("%.2f", volume * 1.2)} m³\n• Semen (50kg): ${String.format("%.1f", volume * 3.26)} sak\n• Pasir Pasang: ${String.format("%.2f", volume * 0.52)} m³",
                    fontSize = 11.sp
                )
            }
        }

        Button(
            onClick = {
                onApply("Pondasi Batu Kali Adonan 1:4", volume, "m³", 920000.0)
                onDismiss()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Masukkan ke RAB Proyek")
        }
    }
}

@Composable
private fun ConcreteCalculatorContent(
    onApply: (itemName: String, volume: Double, unit: String, price: Double) -> Unit,
    onDismiss: () -> Unit
) {
    var lenStr by remember { mutableStateOf("45.0") }
    var widthStr by remember { mutableStateOf("0.15") }
    var heightStr by remember { mutableStateOf("0.20") }
    var countStr by remember { mutableStateOf("1") }

    val len = lenStr.toDoubleOrNull() ?: 0.0
    val w = widthStr.toDoubleOrNull() ?: 0.0
    val h = heightStr.toDoubleOrNull() ?: 0.0
    val cnt = countStr.toIntOrNull() ?: 1

    val volume = remember(len, w, h, cnt) {
        QsVolumeCalculator.calcConcreteVolume(len, w, h, cnt)
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = lenStr,
            onValueChange = { lenStr = it },
            label = { Text("Panjang Total Stuktur (meter)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = widthStr,
                onValueChange = { widthStr = it },
                label = { Text("Lebar (m)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = heightStr,
                onValueChange = { heightStr = it },
                label = { Text("Tinggi/Tebal (m)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("TOTAL VOLUME BETON BERTULANG K-225", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Text("${String.format("%.2f", volume)} m³", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "• Semen (50kg): ${String.format("%.1f", volume * 7.42)} sak\n• Besi Beton: ${String.format("%.1f", volume * 110.0)} kg\n• Pasir Cor: ${String.format("%.2f", volume * 0.498)} m³ | Kerikil: ${String.format("%.2f", volume * 0.776)} m³",
                    fontSize = 11.sp
                )
            }
        }

        Button(
            onClick = {
                onApply("Cor Beton Bertulang Sloof 15x20cm K-225", volume, "m³", 4250000.0)
                onDismiss()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Masukkan ke RAB Proyek")
        }
    }
}

@Composable
private fun RoofCalculatorContent(
    onApply: (itemName: String, volume: Double, unit: String, price: Double) -> Unit,
    onDismiss: () -> Unit
) {
    var lenStr by remember { mutableStateOf("10.0") }
    var widthStr by remember { mutableStateOf("8.0") }
    var pitchStr by remember { mutableStateOf("30.0") }

    val len = lenStr.toDoubleOrNull() ?: 0.0
    val w = widthStr.toDoubleOrNull() ?: 0.0
    val pitch = pitchStr.toDoubleOrNull() ?: 30.0

    val area = remember(len, w, pitch) {
        QsVolumeCalculator.calcRoofArea(len, w, pitch)
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = lenStr,
            onValueChange = { lenStr = it },
            label = { Text("Panjang Bangunan (meter)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = widthStr,
            onValueChange = { widthStr = it },
            label = { Text("Lebar Bangunan (meter)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = pitchStr,
            onValueChange = { pitchStr = it },
            label = { Text("Sudut Kemiringan Atap (Derajat)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("ESTIMASI LUAS ATAP MIRING", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Text("${String.format("%.2f", area)} m²", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
            }
        }

        Button(
            onClick = {
                onApply("Rangka Atap Baja Ringan C75 + Genteng Metal", area, "m²", 245000.0)
                onDismiss()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Masukkan ke RAB Proyek")
        }
    }
}

@Composable
private fun TileCalculatorContent(
    onApply: (itemName: String, volume: Double, unit: String, price: Double) -> Unit,
    onDismiss: () -> Unit
) {
    var lenStr by remember { mutableStateOf("12.0") }
    var widthStr by remember { mutableStateOf("8.0") }
    var wasteStr by remember { mutableStateOf("5.0") }

    val len = lenStr.toDoubleOrNull() ?: 0.0
    val w = widthStr.toDoubleOrNull() ?: 0.0
    val waste = wasteStr.toDoubleOrNull() ?: 5.0

    val area = remember(len, w, waste) {
        QsVolumeCalculator.calcFloorTileArea(len, w, waste)
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = lenStr,
            onValueChange = { lenStr = it },
            label = { Text("Panjang Ruangan (meter)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = widthStr,
            onValueChange = { widthStr = it },
            label = { Text("Lebar Ruangan (meter)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = wasteStr,
            onValueChange = { wasteStr = it },
            label = { Text("Faktor Buangan / Waste (%)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("TOTAL LUAS KERAMIK / GRANIT TILE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Text("${String.format("%.2f", area)} m²", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
            }
        }

        Button(
            onClick = {
                onApply("Pemasangan Lantai Granit Tile 60x60cm", area, "m²", 275000.0)
                onDismiss()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Masukkan ke RAB Proyek")
        }
    }
}
