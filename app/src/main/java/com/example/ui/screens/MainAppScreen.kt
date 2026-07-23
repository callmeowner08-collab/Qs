package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Project
import com.example.data.model.RabCategory
import com.example.data.model.RabItem
import com.example.data.model.RegionalPrice
import com.example.data.model.SniAhspCatalog
import com.example.ui.components.EditProjectDialog
import com.example.ui.components.NewProjectDialog
import com.example.ui.components.ProjectSelectorSheet
import com.example.ui.components.QsDonutChart
import com.example.ui.components.QsSCurveChart
import com.example.ui.components.ChartSlice
import com.example.util.ExportUtils
import com.example.util.QsVolumeCalculator
import com.example.viewmodel.QsViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: QsViewModel) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    val currentProject by viewModel.currentProject.collectAsState()
    val allProjects by viewModel.allProjects.collectAsState()
    val categories by viewModel.categoriesForCurrentProject.collectAsState()
    val items by viewModel.itemsForCurrentProject.collectAsState()
    val summaryState by viewModel.rabSummary.collectAsState()

    var showNewProjectDialog by remember { mutableStateOf(false) }
    var showEditProjectDialog by remember { mutableStateOf(false) }
    var showProjectSelectorSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        modifier = Modifier
                            .clickable { showProjectSelectorSheet = true }
                            .padding(vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = currentProject?.title ?: "Pilih Proyek",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Pilih Proyek",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = "${currentProject?.locationRegion ?: "DKI Jakarta"} • ${currentProject?.clientName ?: "-"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                            fontSize = 11.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                actions = {
                    IconButton(
                        onClick = { showEditProjectDialog = true },
                        modifier = Modifier.testTag("edit_project_settings_button")
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = "Edit Parameter Proyek & Keuntungan")
                    }
                    IconButton(
                        onClick = { showNewProjectDialog = true },
                        modifier = Modifier.testTag("add_project_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Proyek Baru")
                    }
                    IconButton(
                        onClick = {
                            val proj = currentProject
                            if (proj != null) {
                                val map = items.groupBy { it.categoryId }
                                ExportUtils.exportRabPdf(context, proj, categories, map)
                            }
                        },
                        modifier = Modifier.testTag("export_pdf_top_button")
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "Ekspor PDF")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Calculate, contentDescription = null) },
                    label = { Text("RAB Proyek", fontSize = 10.sp) },
                    modifier = Modifier.testTag("tab_rab")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Inventory, contentDescription = null) },
                    label = { Text("Bahan & SNI", fontSize = 10.sp) },
                    modifier = Modifier.testTag("tab_materials")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    label = { Text("Harga Wilayah", fontSize = 10.sp) },
                    modifier = Modifier.testTag("tab_prices")
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Engineering, contentDescription = null) },
                    label = { Text("Pekerja & Chart", fontSize = 10.sp) },
                    modifier = Modifier.testTag("tab_labor")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (selectedTab) {
                0 -> RabTabContent(viewModel, currentProject, categories, items, summaryState)
                1 -> MaterialsSniTabContent(viewModel, summaryState)
                2 -> RegionalPricesTabContent(viewModel)
                3 -> LaborAndChartsTabContent(viewModel, currentProject, items, summaryState)
            }
        }

        if (showProjectSelectorSheet) {
            ProjectSelectorSheet(
                viewModel = viewModel,
                onDismiss = { showProjectSelectorSheet = false },
                onAddNewProject = {
                    showProjectSelectorSheet = false
                    showNewProjectDialog = true
                }
            )
        }

        if (showNewProjectDialog) {
            NewProjectDialog(
                onDismiss = { showNewProjectDialog = false },
                onCreate = { title, client, region, type, workers, wage, ppn, overhead ->
                    viewModel.createNewProject(title, client, region, type, workers, wage, ppn, overhead)
                }
            )
        }

        if (showEditProjectDialog && currentProject != null) {
            EditProjectDialog(
                project = currentProject!!,
                onDismiss = { showEditProjectDialog = false },
                onSave = { updatedProj ->
                    viewModel.updateProjectSettings(updatedProj)
                    showEditProjectDialog = false
                }
            )
        }
    }
}

// -------------------------------------------------------------------------
// TAB 0: RAB PROYEK
// -------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RabTabContent(
    viewModel: QsViewModel,
    project: Project?,
    categories: List<RabCategory>,
    items: List<RabItem>,
    summaryState: com.example.viewmodel.RabSummaryState
) {
    val context = LocalContext.current
    val itemsMap = remember(items) { items.groupBy { it.categoryId } }

    var showAddItemDialogForCategory by remember { mutableStateOf<RabCategory?>(null) }
    var showSniCatalogDialogForCategory by remember { mutableStateOf<RabCategory?>(null) }
    var showVolumeCalcDialogForCategory by remember { mutableStateOf<RabCategory?>(null) }
    var editingItem by remember { mutableStateOf<RabItem?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Executive Cost Card Header
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("summary_cost_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "GRAND TOTAL RENCANA ANGGARAN BIAYA",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = ExportUtils.formatRupiah(summaryState.grandTotalRab),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Biaya Fisik (Subtotal)", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
                            Text(ExportUtils.formatRupiah(summaryState.subtotalBiayaFisik), fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("PPN (${project?.ppnTaxPercent?.toInt() ?: 11}%)", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
                            Text(ExportUtils.formatRupiah(summaryState.ppnAmount), fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Keuntungan & OH (${project?.overheadPercent?.toInt() ?: 10}%)", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
                            Text(ExportUtils.formatRupiah(summaryState.overheadAmount), fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Buttons: PDF & Excel CSV Export
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (project != null) {
                                    ExportUtils.exportRabPdf(context, project, categories, itemsMap)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_pdf_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("PDF RAB", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                if (project != null) {
                                    ExportUtils.exportRabCsv(context, project, categories, itemsMap)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_excel_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Excel CSV", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // RAB Categories and Items
        items(categories, key = { it.id }) { category ->
            val categoryItems = itemsMap[category.id] ?: emptyList()
            val catTotal = summaryState.categoryTotals[category.id] ?: 0.0

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("category_card_${category.id}"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Category Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = category.categoryName.uppercase(),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Total: ${ExportUtils.formatRupiah(catTotal)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.secondary,
                                fontSize = 11.sp
                            )
                        }

                        Row {
                            IconButton(
                                onClick = { showSniCatalogDialogForCategory = category },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.LibraryAdd, contentDescription = "Tambah dari SNI", tint = MaterialTheme.colorScheme.secondary)
                            }
                            IconButton(
                                onClick = { showVolumeCalcDialogForCategory = category },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Calculate, contentDescription = "Kalkulator Volume", tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(
                                onClick = { showAddItemDialogForCategory = category },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.AddCircle, contentDescription = "Tambah Manual", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(8.dp))

                    if (categoryItems.isEmpty()) {
                        Text(
                            text = "Belum ada pekerjaan di kategori ini. Tekan + atau SNI untuk menambahkan.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        categoryItems.forEachIndexed { index, item ->
                            RabItemRow(
                                item = item,
                                index = index + 1,
                                onEdit = { editingItem = item },
                                onDelete = { viewModel.deleteRabItem(item) }
                            )
                            if (index < categoryItems.size - 1) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    if (showAddItemDialogForCategory != null) {
        val cat = showAddItemDialogForCategory!!
        AddEditRabItemDialog(
            item = null,
            categoryId = cat.id,
            projectId = project?.id ?: 1L,
            onSave = { newItem ->
                viewModel.addRabItem(
                    projectId = newItem.projectId,
                    categoryId = newItem.categoryId,
                    itemName = newItem.itemName,
                    volume = newItem.volume,
                    unit = newItem.unit,
                    unitPrice = newItem.unitPrice,
                    laborRate = newItem.laborProductivityRate
                )
                showAddItemDialogForCategory = null
            },
            onDismiss = { showAddItemDialogForCategory = null }
        )
    }

    if (editingItem != null) {
        AddEditRabItemDialog(
            item = editingItem,
            categoryId = editingItem!!.categoryId,
            projectId = editingItem!!.projectId,
            onSave = { updated ->
                viewModel.updateRabItem(updated)
                editingItem = null
            },
            onDismiss = { editingItem = null }
        )
    }

    if (showSniCatalogDialogForCategory != null) {
        val cat = showSniCatalogDialogForCategory!!
        SniCatalogSelectorModal(
            onSelect = { templateCode ->
                viewModel.importSniTemplateItem(
                    projectId = project?.id ?: 1L,
                    categoryId = cat.id,
                    templateCode = templateCode
                )
                showSniCatalogDialogForCategory = null
            },
            onDismiss = { showSniCatalogDialogForCategory = null }
        )
    }

    if (showVolumeCalcDialogForCategory != null) {
        val cat = showVolumeCalcDialogForCategory!!
        VolumeCalculatorModal(
            onInsertVolume = { workName, volume, unit, defaultPrice ->
                viewModel.addRabItem(
                    projectId = project?.id ?: 1L,
                    categoryId = cat.id,
                    itemName = workName,
                    volume = volume,
                    unit = unit,
                    unitPrice = defaultPrice,
                    laborRate = 6.0
                )
                showVolumeCalcDialogForCategory = null
            },
            onDismiss = { showVolumeCalcDialogForCategory = null }
        )
    }
}

@Composable
fun RabItemRow(
    item: RabItem,
    index: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "$index", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.itemName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
            Text(
                text = "Vol: ${String.format(Locale.US, "%.2f", item.volume)} ${item.unit} @ ${ExportUtils.formatRupiah(item.unitPrice)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                fontSize = 11.sp
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = ExportUtils.formatRupiah(item.totalPrice),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 12.sp
            )
            Row {
                IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp), tint = Color.Gray)
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Hapus", modifier = Modifier.size(16.dp), tint = Color.Red.copy(alpha = 0.7f))
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// TAB 1: BAHAN & SNI AHSP
// -------------------------------------------------------------------------
@Composable
fun MaterialsSniTabContent(
    viewModel: QsViewModel,
    summaryState: com.example.viewmodel.RabSummaryState
) {
    var selectedSubTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Tab(
                selected = selectedSubTab == 0,
                onClick = { selectedSubTab = 0 },
                text = { Text("Rekap Bahan (SNI)", fontSize = 12.sp) }
            )
            Tab(
                selected = selectedSubTab == 1,
                onClick = { selectedSubTab = 1 },
                text = { Text("Tenaga Kerja (OH)", fontSize = 12.sp) }
            )
            Tab(
                selected = selectedSubTab == 2,
                onClick = { selectedSubTab = 2 },
                text = { Text("Katalog AHSP", fontSize = 12.sp) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedSubTab) {
            0 -> MaterialBreakdownList(summaryState.materialSummaryList)
            1 -> LaborOhBreakdownList(summaryState.laborOhSummaryList)
            2 -> SniCatalogList()
        }
    }
}

@Composable
fun MaterialBreakdownList(materials: List<com.example.viewmodel.MaterialSummaryItem>) {
    if (materials.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Belum ada bahan terdeteksi dari item RAB proyek.", color = Color.Gray)
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(materials) { mat ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(mat.materialName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Koefisien SNI Terhitung Otomatis", fontSize = 10.sp, color = Color.Gray)
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "${String.format(Locale.US, "%.2f", mat.totalQuantity)} ${mat.unit}",
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LaborOhBreakdownList(laborOhList: List<com.example.viewmodel.LaborOhSummaryItem>) {
    if (laborOhList.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Belum ada kebutuhan OH tenaga kerja terhitung.", color = Color.Gray)
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(laborOhList) { labor ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(labor.roleName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Standard Orang Hari (OH) SNI AHSP", fontSize = 10.sp, color = Color.Gray)
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "${String.format(Locale.US, "%.1f", labor.totalOh)} OH",
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SniCatalogList() {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(SniAhspCatalog.TEMPLATES) { tmpl ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(tmpl.code, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        Text(tmpl.unit, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(tmpl.workName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Estimasi Acuan: ${ExportUtils.formatRupiah(tmpl.defaultUnitPrice)} / ${tmpl.unit}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)

                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Detail Rincian Koefisien SNI:", fontSize = 10.sp, fontWeight = FontWeight.Bold)

                    tmpl.materialCoefficients.forEach { coef ->
                        Text(" • ${coef.materialName}: ${coef.coefficient} ${coef.unit}", fontSize = 10.sp, color = Color.DarkGray)
                    }
                    tmpl.laborCoefficients.forEach { lcoef ->
                        Text(" • ${lcoef.roleName}: ${lcoef.coefficientOh} OH", fontSize = 10.sp, color = Color(0xFFD97706))
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// TAB 2: HARGA REGIONAL WILAYAH
// -------------------------------------------------------------------------
@Composable
fun RegionalPricesTabContent(viewModel: QsViewModel) {
    val selectedRegion by viewModel.selectedRegion.collectAsState()
    val allRegions by viewModel.allRegions.collectAsState()
    val prices by viewModel.regionalPrices.collectAsState()

    var editingPrice by remember { mutableStateOf<RegionalPrice?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Region Dropdown Selector Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("WILAYAH SINKRONISASI BIAYA:", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(selectedRegion, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    Button(
                        onClick = { viewModel.syncProjectWithRegionalPrices() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Terapkan Ke RAB", fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            allRegions.forEach { regionName ->
                FilterChip(
                    selected = selectedRegion == regionName,
                    onClick = { viewModel.setSelectedRegion(regionName) },
                    label = { Text(regionName, fontSize = 10.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(prices, key = { it.id }) { price ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(price.itemName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("${price.category} • Satuan: ${price.unit}", fontSize = 10.sp, color = Color.Gray)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = ExportUtils.formatRupiah(price.price),
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 13.sp
                            )
                            IconButton(onClick = { editingPrice = price }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Harga", modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    if (editingPrice != null) {
        val p = editingPrice!!
        var priceInput by remember { mutableStateOf(p.price.toLong().toString()) }

        AlertDialog(
            onDismissRequest = { editingPrice = null },
            title = { Text("Edit Harga Pasaran Wilayah") },
            text = {
                Column {
                    Text(p.itemName, fontWeight = FontWeight.Bold)
                    Text("Wilayah: ${p.regionName}", fontSize = 11.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = priceInput,
                        onValueChange = { priceInput = it },
                        label = { Text("Harga Satuan (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val newP = priceInput.toDoubleOrNull() ?: p.price
                    viewModel.updateRegionalPrice(p.id, newP)
                    editingPrice = null
                }) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingPrice = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

// -------------------------------------------------------------------------
// TAB 3: TENAGA KERJA, S-CURVE & CHARTS
// -------------------------------------------------------------------------
@Composable
fun LaborAndChartsTabContent(
    viewModel: QsViewModel,
    project: Project?,
    items: List<RabItem>,
    summaryState: com.example.viewmodel.RabSummaryState
) {
    val laborEstimate = summaryState.laborEstimate

    var workerCountInput by remember { mutableStateOf((project?.targetWorkerCount ?: 6).toString()) }
    var dailyWageInput by remember { mutableStateOf((project?.dailyWorkerWage ?: 150000.0).toLong().toString()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Visual Donut Chart Component
        item {
            val categorySlices = remember(summaryState.categoryTotals) {
                summaryState.categoryTotals.map { (catId, total) ->
                    val catName = viewModel.categoriesForCurrentProject.value.firstOrNull { it.id == catId }?.categoryName ?: "Lainnya"
                    ChartSlice(
                        label = catName.take(15),
                        value = total,
                        color = Color.Blue
                    )
                }
            }

            QsDonutChart(
                slices = categorySlices,
                totalAmount = summaryState.subtotalBiayaFisik
            )
        }

        // S-Curve Chart
        item {
            QsSCurveChart(
                progressPercent = summaryState.totalVolumeProgress,
                totalWorkWeeks = laborEstimate?.totalWorkWeeks ?: 8.0
            )
        }

        // Worker Estimation Calculator Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ESTIMASI DURASI & TIM TENAGA KERJA",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = workerCountInput,
                            onValueChange = {
                                workerCountInput = it
                                val count = it.toIntOrNull() ?: 1
                                if (project != null) {
                                    viewModel.updateProjectSettings(project.copy(targetWorkerCount = count))
                                }
                            },
                            label = { Text("Jumlah Pekerja") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = dailyWageInput,
                            onValueChange = {
                                dailyWageInput = it
                                val wage = it.toDoubleOrNull() ?: 150000.0
                                if (project != null) {
                                    viewModel.updateProjectSettings(project.copy(dailyWorkerWage = wage))
                                }
                            },
                            label = { Text("Upah Rata-Rata/Hari") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Man-Days", fontSize = 10.sp, color = Color.Gray)
                            Text("${String.format("%.1f", laborEstimate?.totalManDays ?: 0.0)} Hari-Orang", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Column {
                            Text("Estimasi Hari Kerja", fontSize = 10.sp, color = Color.Gray)
                            Text("${String.format("%.1f", laborEstimate?.totalWorkDays ?: 0.0)} Hari Kerja", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        Column {
                            Text("Estimasi Minggu", fontSize = 10.sp, color = Color.Gray)
                            Text("${String.format("%.1f", laborEstimate?.totalWorkWeeks ?: 0.0)} Minggu", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }
        }

        // Item Progress Update List
        item {
            Text("UPDATE PROGRES PEKERJAAN LAPANGAN", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }

        items(items) { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(item.itemName, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1f))
                        Text("${item.progressPercent.toInt()}%", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    }

                    Slider(
                        value = item.progressPercent,
                        onValueChange = { newProg ->
                            viewModel.updateRabItem(item.copy(progressPercent = newProg))
                        },
                        valueRange = 0f..100f
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// DIALOGS & MODALS
// -------------------------------------------------------------------------
@Composable
fun NewProjectDialog(
    viewModel: QsViewModel,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var clientName by remember { mutableStateOf("") }
    var selectedRegion by remember { mutableStateOf("DKI Jakarta") }
    var projectType by remember { mutableStateOf("Rumah Tinggal") }
    var workerCount by remember { mutableStateOf("6") }
    var dailyWage by remember { mutableStateOf("150000") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Buat Proyek Quantity Surveyor Baru") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nama Proyek") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = clientName,
                    onValueChange = { clientName = it },
                    label = { Text("Nama Klien / Pemilik") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = workerCount,
                    onValueChange = { workerCount = it },
                    label = { Text("Jumlah Pekerja Target") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        viewModel.createNewProject(
                            title = title,
                            clientName = if (clientName.isBlank()) "Klien" else clientName,
                            region = selectedRegion,
                            projectType = projectType,
                            workerCount = workerCount.toIntOrNull() ?: 6,
                            dailyWage = dailyWage.toDoubleOrNull() ?: 150000.0,
                            ppnPercent = 11.0,
                            overheadPercent = 5.0
                        )
                        onDismiss()
                    }
                }
            ) {
                Text("Buat Proyek")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectSelectorModal(
    allProjects: List<Project>,
    currentProjectId: Long?,
    onSelect: (Long) -> Unit,
    onDismiss: () -> Unit,
    onNewProjectClick: () -> Unit
) {
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
                Text("DAFTAR PROYEK QS", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Button(onClick = onNewProjectClick) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Proyek Baru")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(allProjects) { proj ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(proj.id) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (proj.id == currentProjectId) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(proj.title, fontWeight = FontWeight.Bold)
                                Text("Wilayah: ${proj.locationRegion} • Klien: ${proj.clientName}", fontSize = 11.sp, color = Color.Gray)
                            }
                            if (proj.id == currentProjectId) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddEditRabItemDialog(
    item: RabItem?,
    categoryId: Long,
    projectId: Long,
    onSave: (RabItem) -> Unit,
    onDismiss: () -> Unit
) {
    var itemName by remember { mutableStateOf(item?.itemName ?: "") }
    var volumeStr by remember { mutableStateOf(item?.volume?.toString() ?: "10") }
    var unit by remember { mutableStateOf(item?.unit ?: "m²") }
    var priceStr by remember { mutableStateOf(item?.unitPrice?.toLong()?.toString() ?: "150000") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (item == null) "Tambah Pekerjaan RAB" else "Edit Pekerjaan RAB") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = { Text("Uraian Pekerjaan") }
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = volumeStr,
                        onValueChange = { volumeStr = it },
                        label = { Text("Volume") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Satuan") },
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { priceStr = it },
                    label = { Text("Harga Satuan (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (itemName.isNotBlank()) {
                        val newItem = RabItem(
                            id = item?.id ?: 0,
                            projectId = projectId,
                            categoryId = categoryId,
                            itemName = itemName,
                            volume = volumeStr.toDoubleOrNull() ?: 1.0,
                            unit = unit,
                            unitPrice = priceStr.toDoubleOrNull() ?: 0.0,
                            laborProductivityRate = item?.laborProductivityRate ?: 6.0
                        )
                        onSave(newItem)
                    }
                }
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SniCatalogSelectorModal(
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("PILIH PEKERJAAN KANONIKAL AHSP SNI", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(SniAhspCatalog.TEMPLATES) { tmpl ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(tmpl.code) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(tmpl.code, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                Text(ExportUtils.formatRupiah(tmpl.defaultUnitPrice), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(tmpl.workName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VolumeCalculatorModal(
    onInsertVolume: (String, Double, String, Double) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedCalcType by remember { mutableIntStateOf(0) } // 0: Dinding, 1: Pondasi, 2: Cor Beton, 3: Lantai

    var p1 by remember { mutableStateOf("10") }
    var p2 by remember { mutableStateOf("3") }
    var p3 by remember { mutableStateOf("0.5") }
    var p4 by remember { mutableStateOf("0") }

    val calculatedResult = remember(selectedCalcType, p1, p2, p3, p4) {
        val v1 = p1.toDoubleOrNull() ?: 0.0
        val v2 = p2.toDoubleOrNull() ?: 0.0
        val v3 = p3.toDoubleOrNull() ?: 0.0
        val v4 = p4.toDoubleOrNull() ?: 0.0

        when (selectedCalcType) {
            0 -> QsVolumeCalculator.calcWallArea(v1, v2, v3) // Wall area
            1 -> QsVolumeCalculator.calcFoundationVolume(v1, v2, v3, v4) // Foundation vol
            2 -> QsVolumeCalculator.calcConcreteVolume(v1, v2, v3, if (v4 > 0) v4.toInt() else 1) // Concrete vol
            else -> QsVolumeCalculator.calcFloorTileArea(v1, v2, 5.0) // Floor area
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("KALKULATOR OTOMATIS VOLUME BANGUNAN", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(selected = selectedCalcType == 0, onClick = { selectedCalcType = 0 }, shape = SegmentedButtonDefaults.itemShape(0, 4)) {
                    Text("Dinding", fontSize = 10.sp)
                }
                SegmentedButton(selected = selectedCalcType == 1, onClick = { selectedCalcType = 1 }, shape = SegmentedButtonDefaults.itemShape(1, 4)) {
                    Text("Pondasi", fontSize = 10.sp)
                }
                SegmentedButton(selected = selectedCalcType == 2, onClick = { selectedCalcType = 2 }, shape = SegmentedButtonDefaults.itemShape(2, 4)) {
                    Text("Cor Beton", fontSize = 10.sp)
                }
                SegmentedButton(selected = selectedCalcType == 3, onClick = { selectedCalcType = 3 }, shape = SegmentedButtonDefaults.itemShape(3, 4)) {
                    Text("Lantai", fontSize = 10.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedCalcType) {
                0 -> {
                    OutlinedTextField(value = p1, onValueChange = { p1 = it }, label = { Text("Panjang Dinding (m)") })
                    OutlinedTextField(value = p2, onValueChange = { p2 = it }, label = { Text("Tinggi Dinding (m)") })
                    OutlinedTextField(value = p3, onValueChange = { p3 = it }, label = { Text("Luas Pintu & Jendela (m²)") })
                }
                1 -> {
                    OutlinedTextField(value = p1, onValueChange = { p1 = it }, label = { Text("Lebar Atas Pondasi (m)") })
                    OutlinedTextField(value = p2, onValueChange = { p2 = it }, label = { Text("Lebar Bawah Pondasi (m)") })
                    OutlinedTextField(value = p3, onValueChange = { p3 = it }, label = { Text("Tinggi Pondasi (m)") })
                    OutlinedTextField(value = p4, onValueChange = { p4 = it }, label = { Text("Total Panjang Pondasi (m)") })
                }
                2 -> {
                    OutlinedTextField(value = p1, onValueChange = { p1 = it }, label = { Text("Panjang (m)") })
                    OutlinedTextField(value = p2, onValueChange = { p2 = it }, label = { Text("Lebar (m)") })
                    OutlinedTextField(value = p3, onValueChange = { p3 = it }, label = { Text("Tebal / Tinggi (m)") })
                    OutlinedTextField(value = p4, onValueChange = { p4 = it }, label = { Text("Jumlah Elemen (Sloof/Kolom)") })
                }
                3 -> {
                    OutlinedTextField(value = p1, onValueChange = { p1 = it }, label = { Text("Panjang Ruangan (m)") })
                    OutlinedTextField(value = p2, onValueChange = { p2 = it }, label = { Text("Lebar Ruangan (m)") })
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Hasil Volume Terkalkulasi:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(
                        text = "${String.format(Locale.US, "%.2f", calculatedResult)} ${if (selectedCalcType == 0 || selectedCalcType == 3) "m²" else "m³"}",
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val unit = if (selectedCalcType == 0 || selectedCalcType == 3) "m²" else "m³"
                    val titleName = when (selectedCalcType) {
                        0 -> "Pasangan Dinding Dihitung"
                        1 -> "Pondasi Batu Kali Dihitung"
                        2 -> "Cor Beton Bertulang Dihitung"
                        else -> "Pasangan Lantai Dihitung"
                    }
                    onInsertVolume(titleName, calculatedResult, unit, 150000.0)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Masukkan Ke Daftar RAB")
            }
        }
    }
}
