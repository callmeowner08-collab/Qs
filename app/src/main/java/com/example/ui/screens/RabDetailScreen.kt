package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.data.model.RabCategory
import com.example.data.model.RabItem
import com.example.data.model.SniAhspCatalog
import com.example.util.ExportUtils
import com.example.viewmodel.QsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RabDetailScreen(
    viewModel: QsViewModel,
    onNavigateBack: () -> Unit
) {
    val currentProj by viewModel.currentProject.collectAsState()
    val categories by viewModel.categoriesForCurrentProject.collectAsState()
    val items by viewModel.itemsForCurrentProject.collectAsState()
    val summary by viewModel.rabSummary.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showAddItemSheet by remember { mutableStateOf(false) }
    var selectedCategoryForAdd by remember { mutableStateOf<RabCategory?>(null) }
    var editingItem by remember { mutableStateOf<RabItem?>(null) }

    val itemsByCategory = remember(items, searchQuery) {
        val filtered = if (searchQuery.isBlank()) items else items.filter {
            it.itemName.contains(searchQuery, ignoreCase = true)
        }
        filtered.groupBy { it.categoryId }
    }

    if (showAddItemSheet && selectedCategoryForAdd != null) {
        AddItemModalSheet(
            category = selectedCategoryForAdd!!,
            projectId = currentProj?.id ?: 0L,
            onDismiss = { showAddItemSheet = false },
            onSaveManual = { projId, catId, name, vol, unit, price, labor ->
                viewModel.addRabItem(projId, catId, name, vol, unit, price, labor)
                showAddItemSheet = false
            },
            onImportTemplate = { projId, catId, code ->
                viewModel.importSniTemplateItem(projId, catId, code)
                showAddItemSheet = false
            }
        )
    }

    if (editingItem != null) {
        EditItemDialog(
            item = editingItem!!,
            onDismiss = { editingItem = null },
            onSave = { updatedItem ->
                viewModel.updateRabItem(updatedItem)
                editingItem = null
            },
            onDelete = { itemToDelete ->
                viewModel.deleteRabItem(itemToDelete)
                editingItem = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Rencana Anggaran Biaya (RAB)",
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
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("back_button")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "TOTAL FISIK + TAX", fontSize = 10.sp, color = Color.Gray)
                        Text(
                            text = ExportUtils.formatRupiah(summary.grandTotalRab),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Button(
                        onClick = {
                            if (categories.isNotEmpty()) {
                                selectedCategoryForAdd = categories.first()
                                showAddItemSheet = true
                            }
                        },
                        modifier = Modifier.testTag("add_item_fab")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tambah Item")
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari item pekerjaan...") },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("rab_search_input"),
                singleLine = true
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                categories.forEach { category ->
                    val categoryItems = itemsByCategory[category.id] ?: emptyList()

                    item(key = "cat_${category.id}") {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = category.categoryName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.weight(1f)
                                    )

                                    IconButton(
                                        onClick = {
                                            selectedCategoryForAdd = category
                                            showAddItemSheet = true
                                        },
                                        modifier = Modifier.testTag("add_to_cat_${category.id}")
                                    ) {
                                        Icon(imageVector = Icons.Default.AddCircle, contentDescription = "Tambah Ke Kategori")
                                    }
                                }

                                val catTotal = categoryItems.sumOf { it.totalPrice }
                                Text(
                                    text = "Subtotal: ${ExportUtils.formatRupiah(catTotal)}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                if (categoryItems.isEmpty()) {
                                    Text(
                                        text = "Belum ada item dalam kategori ini. Klik + untuk menambahkan.",
                                        fontSize = 11.sp,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        categoryItems.forEach { item ->
                                            RabItemRow(
                                                item = item,
                                                onEdit = { editingItem = item }
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
    }
}

@Composable
private fun RabItemRow(
    item: RabItem,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() }
            .testTag("rab_item_row_${item.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.itemName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = ExportUtils.formatRupiah(item.totalPrice),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Vol: ${String.format("%.1f", item.volume)} ${item.unit} @ ${ExportUtils.formatRupiah(item.unitPrice)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Progres: ${item.progressPercent.toInt()}%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (item.progressPercent >= 100f) Color(0xFF10B981) else Color(0xFFD97706)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddItemModalSheet(
    category: RabCategory,
    projectId: Long,
    onDismiss: () -> Unit,
    onSaveManual: (Long, Long, String, Double, String, Double, Double) -> Unit,
    onImportTemplate: (Long, Long, String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var name by remember { mutableStateOf("") }
    var volStr by remember { mutableStateOf("10.0") }
    var unit by remember { mutableStateOf("m²") }
    var priceStr by remember { mutableStateOf("150000") }
    var laborStr by remember { mutableStateOf("6.0") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Tambah Item: ${category.categoryName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Katalog AHSP SNI") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Input Manual") })
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedTab == 0) {
                LazyColumn(
                    modifier = Modifier.height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(SniAhspCatalog.TEMPLATES) { tmpl ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onImportTemplate(projectId, category.id, tmpl.code)
                                },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(text = tmpl.workName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Kode: ${tmpl.code}", fontSize = 10.sp, color = Color.Gray)
                                    Text(
                                        text = "${ExportUtils.formatRupiah(tmpl.defaultUnitPrice)} / ${tmpl.unit}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nama Pekerjaan") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = volStr,
                            onValueChange = { volStr = it },
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
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            val vol = volStr.toDoubleOrNull() ?: 1.0
                            val price = priceStr.toDoubleOrNull() ?: 0.0
                            val labor = laborStr.toDoubleOrNull() ?: 6.0
                            if (name.isNotBlank()) {
                                onSaveManual(projectId, category.id, name, vol, unit, price, labor)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Simpan Item Baru")
                    }
                }
            }
        }
    }
}

@Composable
private fun EditItemDialog(
    item: RabItem,
    onDismiss: () -> Unit,
    onSave: (RabItem) -> Unit,
    onDelete: (RabItem) -> Unit
) {
    var name by remember { mutableStateOf(item.itemName) }
    var volStr by remember { mutableStateOf(item.volume.toString()) }
    var unit by remember { mutableStateOf(item.unit) }
    var priceStr by remember { mutableStateOf(item.unitPrice.toLong().toString()) }
    var progressStr by remember { mutableStateOf(item.progressPercent.toInt().toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Item RAB Proyek") },
        confirmButton = {
            Button(onClick = {
                val vol = volStr.toDoubleOrNull() ?: item.volume
                val price = priceStr.toDoubleOrNull() ?: item.unitPrice
                val prog = (progressStr.toFloatOrNull() ?: item.progressPercent).coerceIn(0f, 100f)
                onSave(item.copy(itemName = name, volume = vol, unit = unit, unitPrice = price, progressPercent = prog))
            }) {
                Text("Simpan Edit")
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onDelete(item) },
                colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
            ) {
                Text("Hapus Item")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Item") })
                OutlinedTextField(value = volStr, onValueChange = { volStr = it }, label = { Text("Volume") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("Satuan") })
                OutlinedTextField(value = priceStr, onValueChange = { priceStr = it }, label = { Text("Harga Satuan (Rp)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = progressStr, onValueChange = { progressStr = it }, label = { Text("Progres Lapangan (%)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
        }
    )
}
