package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Project
import com.example.ui.components.ChartSlice
import com.example.ui.components.EditProjectDialog
import com.example.ui.components.QsDonutChart
import com.example.ui.components.QsSCurveChart
import com.example.ui.components.SniCalculatorDialog
import com.example.util.ExportUtils
import com.example.viewmodel.QsViewModel
import com.example.viewmodel.RabSummaryState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: QsViewModel,
    onNavigateToRab: () -> Unit,
    onNavigateToLabor: () -> Unit,
    onNavigateToRegional: () -> Unit,
    onOpenProjectSelector: () -> Unit,
    onAddNewProject: () -> Unit
) {
    val context = LocalContext.current
    val currentProj by viewModel.currentProject.collectAsState()
    val summary by viewModel.rabSummary.collectAsState()
    val categories by viewModel.categoriesForCurrentProject.collectAsState()
    val items by viewModel.itemsForCurrentProject.collectAsState()

    var showSniCalculator by remember { mutableStateOf(false) }
    var showEditProjectDialog by remember { mutableStateOf(false) }
    var isMaterialExpanded by remember { mutableStateOf(false) }
    var isLaborExpanded by remember { mutableStateOf(false) }

    if (showSniCalculator) {
        SniCalculatorDialog(
            onDismiss = { showSniCalculator = false },
            onResultCalculated = { name, vol, unit, price ->
                val proj = currentProj
                val cat = categories.firstOrNull()
                if (proj != null && cat != null) {
                    viewModel.addRabItem(proj.id, cat.id, name, vol, unit, price, 6.0)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        modifier = Modifier
                            .clickable { onOpenProjectSelector() }
                            .testTag("project_selector_header")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = currentProj?.title ?: "Pilih / Buat Proyek",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Switch Project"
                            )
                        }
                        Text(
                            text = "${currentProj?.locationRegion ?: "Indonesia"} • ${currentProj?.projectType ?: "Konstruksi"}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showEditProjectDialog = true },
                        modifier = Modifier.testTag("dashboard_edit_project_icon")
                    ) {
                        Icon(imageVector = Icons.Default.Tune, contentDescription = "Edit Parameter Proyek & Keuntungan")
                    }
                    IconButton(
                        onClick = { showSniCalculator = true },
                        modifier = Modifier.testTag("open_sni_calc_icon")
                    ) {
                        Icon(imageVector = Icons.Default.Calculate, contentDescription = "Kalkulator Volume SNI")
                    }
                    IconButton(
                        onClick = { onAddNewProject() },
                        modifier = Modifier.testTag("add_project_icon")
                    ) {
                        Icon(imageVector = Icons.Default.AddBusiness, contentDescription = "Tambah Proyek Baru")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        if (currentProj == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Architecture,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Belum Ada Proyek",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onAddNewProject,
                        modifier = Modifier.testTag("create_first_proj_btn")
                    ) {
                        Text("Buat Proyek Quantity Surveyor Baru")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Quick Action Bar
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val proj = currentProj
                                val itemsMap = items.groupBy { it.categoryId }
                                if (proj != null) {
                                    ExportUtils.exportRabPdf(context, proj, categories, itemsMap)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_pdf_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            contentPadding = PaddingValues(vertical = 10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PDF RAB", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val proj = currentProj
                                val itemsMap = items.groupBy { it.categoryId }
                                if (proj != null) {
                                    ExportUtils.exportRabCsv(context, proj, categories, itemsMap)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_excel_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
                            contentPadding = PaddingValues(vertical = 10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Excel CSV", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { showSniCalculator = true },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("calc_vol_btn"),
                            contentPadding = PaddingValues(vertical = 10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Vol SNI", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Summary Cards Grid
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Grand Total RAB Card
                        Card(
                            modifier = Modifier
                                .weight(1.2f)
                                .clickable { onNavigateToRab() }
                                .testTag("grand_total_card"),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "GRAND TOTAL RAB",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = ExportUtils.formatRupiah(summary.grandTotalRab),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Termasuk PPN ${currentProj?.ppnTaxPercent?.toInt()}% & Keuntungan ${currentProj?.overheadPercent?.toInt()}%",
                                    fontSize = 9.5.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }

                        // Physical Progress Card
                        Card(
                            modifier = Modifier
                                .weight(0.8f)
                                .clickable { onNavigateToLabor() }
                                .testTag("progress_card"),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "PROGRES FISIK",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${String.format("%.1f", summary.totalVolumeProgress)}%",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Target: ${summary.laborEstimate?.totalWorkDays?.toInt() ?: 0} Hari Kerja",
                                    fontSize = 9.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }

                // Donut Chart - Category Distribution
                item {
                    val slices = categories.map { cat ->
                        ChartSlice(
                            label = cat.categoryName.replace("PEKERJAAN ", "").take(18),
                            value = summary.categoryTotals[cat.id] ?: 0.0,
                            color = Color.Unspecified
                        )
                    }.filter { it.value > 0 }

                    QsDonutChart(
                        slices = slices,
                        totalAmount = summary.grandTotalRab
                    )
                }

                // S-Curve Gantt Progress Chart
                item {
                    QsSCurveChart(
                        progressPercent = summary.totalVolumeProgress,
                        totalWorkWeeks = summary.laborEstimate?.totalWorkWeeks ?: 0.0
                    )
                }

                // SNI Material Breakdown Requirements Section
                item {
                    val totalMatCost = summary.materialSummaryList.sumOf { it.estimatedTotalCost }
                    val displayMaterials = if (isMaterialExpanded) summary.materialSummaryList else summary.materialSummaryList.take(5)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("material_breakdown_card"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Inventory2,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "DETAIL KEBUTUHAN MATERIAL (SNI)",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            letterSpacing = 0.5.sp
                                        )
                                        Text(
                                            text = "${summary.materialSummaryList.size} jenis material terdeteksi",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }

                                TextButton(
                                    onClick = { onNavigateToRegional() },
                                    modifier = Modifier.testTag("sync_regional_price_btn")
                                ) {
                                    Text("Harga Wilayah", fontSize = 11.sp)
                                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            if (summary.materialSummaryList.isNotEmpty()) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("ESTIMASI BIAYA MATERIAL", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                            Text(
                                                text = ExportUtils.formatRupiah(totalMatCost),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Text("Koefisien AHSP SNI", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            if (summary.materialSummaryList.isEmpty()) {
                                Text(
                                    text = "Tambahkan item RAB standar SNI untuk melihat rincian kebutuhan semen, pasir, besi, batu, dll.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    displayMaterials.forEach { mat ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(CircleShape)
                                                        .background(MaterialTheme.colorScheme.primary)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(
                                                        text = mat.materialName,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 12.sp
                                                    )
                                                    if (mat.estimatedTotalCost > 0) {
                                                        Text(
                                                            text = "Est. ${ExportUtils.formatRupiah(mat.estimatedTotalCost)}",
                                                            fontSize = 10.sp,
                                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                                        )
                                                    }
                                                }
                                            }
                                            Text(
                                                text = "${String.format("%.2f", mat.totalQuantity)} ${mat.unit}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    if (summary.materialSummaryList.size > 5) {
                                        TextButton(
                                            onClick = { isMaterialExpanded = !isMaterialExpanded },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("toggle_materials_btn")
                                        ) {
                                            Text(
                                                if (isMaterialExpanded) "Sembunyikan Sebagian"
                                                else "Lihat Seluruh ${summary.materialSummaryList.size} Material",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Icon(
                                                imageVector = if (isMaterialExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                contentDescription = null
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Labor & Worker Requirement Breakdown Section
                item {
                    val totalLaborOh = summary.laborOhSummaryList.sumOf { it.totalOh }
                    val totalLaborCost = summary.laborOhSummaryList.sumOf { it.estimatedTotalCost }
                    val displayLabor = if (isLaborExpanded) summary.laborOhSummaryList else summary.laborOhSummaryList.take(5)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("labor_breakdown_card"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Engineering,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "DETAIL KEBUTUHAN UPAH & TUKANG (OH)",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            letterSpacing = 0.5.sp
                                        )
                                        Text(
                                            text = "${String.format("%.1f", totalLaborOh)} Orang-Hari (OH) dibutuhkan",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }

                                TextButton(
                                    onClick = { onNavigateToLabor() },
                                    modifier = Modifier.testTag("nav_to_labor_btn")
                                ) {
                                    Text("Jadwal Tukang", fontSize = 11.sp)
                                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            if (summary.laborOhSummaryList.isNotEmpty()) {
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("ESTIMASI BIAYA UPAH KERJA", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                            Text(
                                                text = ExportUtils.formatRupiah(totalLaborCost),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("Tim: ${currentProj?.targetWorkerCount ?: 0} Pekerja", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            Text("Wage: ${ExportUtils.formatRupiah(currentProj?.dailyWorkerWage ?: 0.0)}/hari", fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            if (summary.laborOhSummaryList.isEmpty()) {
                                Text(
                                    text = "Tambahkan pekerjaan RAB untuk menghitung estimasi kebutuhan Orang-Hari (OH) Tukang, Pekerja, dan Mandor.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    displayLabor.forEach { labor ->
                                        val maxOh = summary.laborOhSummaryList.maxOfOrNull { it.totalOh } ?: 1.0
                                        val fraction = (labor.totalOh / maxOh).toFloat().coerceIn(0f, 1f)

                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .padding(10.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Groups,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.secondary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = labor.roleName,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 12.sp
                                                    )
                                                }
                                                Column(horizontalAlignment = Alignment.End) {
                                                    Text(
                                                        text = "${String.format("%.1f", labor.totalOh)} OH",
                                                        fontWeight = FontWeight.ExtraBold,
                                                        fontSize = 12.sp,
                                                        color = MaterialTheme.colorScheme.secondary
                                                    )
                                                    Text(
                                                        text = "Tarif: ${ExportUtils.formatRupiah(labor.estimatedDailyWage)}/OH",
                                                        fontSize = 9.5.sp,
                                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            LinearProgressIndicator(
                                                progress = { fraction },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(4.dp)
                                                    .clip(RoundedCornerShape(2.dp)),
                                                color = MaterialTheme.colorScheme.secondary,
                                                trackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                            )
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 4.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Est. Subtotal Biaya Peran:", fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                                Text(
                                                    text = ExportUtils.formatRupiah(labor.estimatedTotalCost),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }

                                    if (summary.laborOhSummaryList.size > 5) {
                                        TextButton(
                                            onClick = { isLaborExpanded = !isLaborExpanded },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("toggle_labor_btn")
                                        ) {
                                            Text(
                                                if (isLaborExpanded) "Sembunyikan Sebagian"
                                                else "Lihat Seluruh ${summary.laborOhSummaryList.size} Peran Tukang",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Icon(
                                                imageVector = if (isLaborExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                contentDescription = null
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (showEditProjectDialog && currentProj != null) {
            EditProjectDialog(
                project = currentProj!!,
                onDismiss = { showEditProjectDialog = false },
                onSave = { updated ->
                    viewModel.updateProjectSettings(updated)
                    showEditProjectDialog = false
                }
            )
        }
    }
}
