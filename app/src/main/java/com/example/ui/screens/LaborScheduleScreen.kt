package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.ExportUtils
import com.example.viewmodel.QsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaborScheduleScreen(
    viewModel: QsViewModel,
    onNavigateBack: () -> Unit
) {
    val currentProj by viewModel.currentProject.collectAsState()
    val summary by viewModel.rabSummary.collectAsState()

    var workerCountStr by remember(currentProj) {
        mutableStateOf((currentProj?.targetWorkerCount ?: 6).toString())
    }
    var dailyWageStr by remember(currentProj) {
        mutableStateOf((currentProj?.dailyWorkerWage?.toLong() ?: 150000L).toString())
    }

    val laborEstimate = summary.laborEstimate

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Estimasi Tenaga Kerja & Target Proyek",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = currentProj?.title ?: "",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("labor_back_btn")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Configuration Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("labor_config_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Groups,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "KONFIGURASI TIM PEKERJA PROYEK",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                letterSpacing = 1.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = workerCountStr,
                                onValueChange = {
                                    workerCountStr = it
                                    val count = it.toIntOrNull()
                                    val proj = currentProj
                                    if (count != null && proj != null && count > 0) {
                                        viewModel.updateProjectSettings(proj.copy(targetWorkerCount = count))
                                    }
                                },
                                label = { Text("Jumlah Pekerja (Orang)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f).testTag("worker_count_input")
                            )

                            OutlinedTextField(
                                value = dailyWageStr,
                                onValueChange = {
                                    dailyWageStr = it
                                    val wage = it.toDoubleOrNull()
                                    val proj = currentProj
                                    if (wage != null && proj != null && wage > 0) {
                                        viewModel.updateProjectSettings(proj.copy(dailyWorkerWage = wage))
                                    }
                                },
                                label = { Text("Upah Rata-rata/Hari (Rp)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f).testTag("daily_wage_input")
                            )
                        }
                    }
                }
            }

            // Calculation Summary Card
            item {
                if (laborEstimate != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "HASIL PROYEKSI HARI KERJA & BIAYA UPAH",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(text = "Total Mandays (OH)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                                    Text(
                                        text = "${String.format("%.1f", laborEstimate.totalManDays)} OH",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }

                                Column {
                                    Text(text = "Target Durasi Kerja", fontSize = 10.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                                    Text(
                                        text = "${laborEstimate.totalWorkDays.toInt()} Hari (${String.format("%.1f", laborEstimate.totalWorkWeeks)} Mgg)",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Estimasi Biaya Upah Tenaga Kerja: ${ExportUtils.formatRupiah(laborEstimate.totalLaborCost)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            // Item-by-item breakdown table
            item {
                Text(
                    text = "RINCIAN PRODUKTIVITAS & HARI KERJA PER ITEM",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            if (laborEstimate?.itemBreakdowns.isNullOrEmpty()) {
                item {
                    Text(text = "Belum ada item pekerjaan di RAB.", fontSize = 12.sp, color = Color.Gray)
                }
            } else {
                items(laborEstimate!!.itemBreakdowns) { detail ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = detail.itemName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Vol: ${String.format("%.1f", detail.volume)} ${detail.unit} | Koef: ${detail.productivityRate} ${detail.unit}/OH",
                                    fontSize = 10.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "~${detail.daysWithTeam.toInt()} Hari Kerja",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
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
