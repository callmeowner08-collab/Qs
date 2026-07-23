package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
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
import com.example.data.model.Project
import com.example.viewmodel.QsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectSelectorSheet(
    viewModel: QsViewModel,
    onDismiss: () -> Unit,
    onAddNewProject: () -> Unit
) {
    val projects by viewModel.allProjects.collectAsState()
    val activeProj by viewModel.currentProject.collectAsState()

    ModalBottomSheet(onDismissRequest = onDismiss) {
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
                    text = "Daftar Proyek Quantity Surveyor",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = {
                        onDismiss()
                        onAddNewProject()
                    },
                    modifier = Modifier.testTag("sheet_add_project_btn")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Proyek Baru")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.height(320.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(projects, key = { it.id }) { proj ->
                    val isSelected = proj.id == activeProj?.id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.selectProject(proj.id)
                                onDismiss()
                            }
                            .testTag("select_project_${proj.id}"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = proj.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Klien: ${proj.clientName} • Wilayah: ${proj.locationRegion} • Pekerja: ${proj.targetWorkerCount} Org",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Terpilih",
                                    tint = MaterialTheme.colorScheme.primary
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
fun NewProjectDialog(
    onDismiss: () -> Unit,
    onCreate: (title: String, client: String, region: String, type: String, workers: Int, wage: Double, ppn: Double, overhead: Double) -> Unit
) {
    var title by remember { mutableStateOf("Pembangunan Rumah 2 Lantai") }
    var client by remember { mutableStateOf("Bpk. Hendra") }
    var region by remember { mutableStateOf("DKI Jakarta") }
    var type by remember { mutableStateOf("Rumah Tinggal") }
    var workersStr by remember { mutableStateOf("8") }
    var wageStr by remember { mutableStateOf("160000") }
    var ppnStr by remember { mutableStateOf("11") }
    var overheadStr by remember { mutableStateOf("10") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Buat Proyek Quantity Surveyor Baru") },
        confirmButton = {
            Button(
                onClick = {
                    val w = workersStr.toIntOrNull() ?: 6
                    val wg = wageStr.toDoubleOrNull() ?: 150000.0
                    val ppn = ppnStr.toDoubleOrNull() ?: 11.0
                    val oh = overheadStr.toDoubleOrNull() ?: 10.0
                    if (title.isNotBlank()) {
                        onCreate(title, client, region, type, w, wg, ppn, oh)
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("submit_new_project_btn")
            ) {
                Text("Buat Proyek")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Judul / Nama Proyek") },
                    modifier = Modifier.fillMaxWidth().testTag("new_proj_title_input")
                )
                OutlinedTextField(
                    value = client,
                    onValueChange = { client = it },
                    label = { Text("Nama Pemilik / Klien") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = region,
                    onValueChange = { region = it },
                    label = { Text("Wilayah Proyek (Standar AHSP)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = workersStr,
                        onValueChange = { workersStr = it },
                        label = { Text("Target Pekerja") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = wageStr,
                        onValueChange = { wageStr = it },
                        label = { Text("Upah/Hari (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1.2f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = ppnStr,
                        onValueChange = { ppnStr = it },
                        label = { Text("PPN Tax (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = overheadStr,
                        onValueChange = { overheadStr = it },
                        label = { Text("Keuntungan & Overhead (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1.2f),
                        supportingText = { Text("Standar: 10%") }
                    )
                }
            }
        }
    )
}

@Composable
fun EditProjectDialog(
    project: Project,
    onDismiss: () -> Unit,
    onSave: (Project) -> Unit,
    onDelete: ((Project) -> Unit)? = null
) {
    var title by remember { mutableStateOf(project.title) }
    var client by remember { mutableStateOf(project.clientName) }
    var region by remember { mutableStateOf(project.locationRegion) }
    var type by remember { mutableStateOf(project.projectType) }
    var workersStr by remember { mutableStateOf(project.targetWorkerCount.toString()) }
    var wageStr by remember { mutableStateOf(project.dailyWorkerWage.toLong().toString()) }
    var ppnStr by remember { mutableStateOf(project.ppnTaxPercent.toString()) }
    var overheadStr by remember { mutableStateOf(project.overheadPercent.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Parameter Proyek & Keuntungan") },
        confirmButton = {
            Button(
                onClick = {
                    val w = workersStr.toIntOrNull() ?: project.targetWorkerCount
                    val wg = wageStr.toDoubleOrNull() ?: project.dailyWorkerWage
                    val ppn = ppnStr.toDoubleOrNull() ?: project.ppnTaxPercent
                    val oh = overheadStr.toDoubleOrNull() ?: 10.0
                    if (title.isNotBlank()) {
                        onSave(
                            project.copy(
                                title = title,
                                clientName = client,
                                locationRegion = region,
                                projectType = type,
                                targetWorkerCount = w,
                                dailyWorkerWage = wg,
                                ppnTaxPercent = ppn,
                                overheadPercent = oh
                            )
                        )
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("save_edit_project_btn")
            ) {
                Text("Simpan Perubahan")
            }
        },
        dismissButton = {
            Row {
                if (onDelete != null) {
                    TextButton(
                        onClick = {
                            onDelete(project)
                            onDismiss()
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                    ) {
                        Text("Hapus Proyek")
                    }
                }
                TextButton(onClick = onDismiss) { Text("Batal") }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Judul / Nama Proyek") },
                    modifier = Modifier.fillMaxWidth().testTag("edit_proj_title_input")
                )
                OutlinedTextField(
                    value = client,
                    onValueChange = { client = it },
                    label = { Text("Nama Pemilik / Klien") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = region,
                    onValueChange = { region = it },
                    label = { Text("Wilayah Proyek (AHSP)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = workersStr,
                        onValueChange = { workersStr = it },
                        label = { Text("Target Pekerja") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = wageStr,
                        onValueChange = { wageStr = it },
                        label = { Text("Upah/Hari (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1.2f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = ppnStr,
                        onValueChange = { ppnStr = it },
                        label = { Text("PPN Tax (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = overheadStr,
                        onValueChange = { overheadStr = it },
                        label = { Text("Keuntungan & Overhead (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1.2f),
                        supportingText = { Text("Standar: 10% (Basi Kontraktor)") }
                    )
                }
            }
        }
    )
}
