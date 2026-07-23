package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
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
import com.example.data.model.RegionalPrice
import com.example.util.ExportUtils
import com.example.viewmodel.QsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegionalPricesScreen(
    viewModel: QsViewModel,
    onNavigateBack: () -> Unit
) {
    val regions by viewModel.allRegions.collectAsState()
    val selectedRegion by viewModel.selectedRegion.collectAsState()
    val prices by viewModel.regionalPrices.collectAsState()
    val currentProj by viewModel.currentProject.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var editingPrice by remember { mutableStateOf<RegionalPrice?>(null) }

    val filteredPrices = remember(prices, searchQuery) {
        if (searchQuery.isBlank()) prices
        else prices.filter { it.itemName.contains(searchQuery, ignoreCase = true) || it.category.contains(searchQuery, ignoreCase = true) }
    }

    if (editingPrice != null) {
        var newPriceStr by remember { mutableStateOf(editingPrice!!.price.toLong().toString()) }

        AlertDialog(
            onDismissRequest = { editingPrice = null },
            title = { Text("Update Harga Wilayah: ${editingPrice!!.itemName}") },
            confirmButton = {
                Button(onClick = {
                    val p = newPriceStr.toDoubleOrNull()
                    if (p != null) {
                        viewModel.updateRegionalPrice(editingPrice!!.id, p)
                        editingPrice = null
                    }
                }) {
                    Text("Simpan Harga")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingPrice = null }) { Text("Batal") }
            },
            text = {
                Column {
                    Text(text = "Wilayah: $selectedRegion", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPriceStr,
                        onValueChange = { newPriceStr = it },
                        label = { Text("Harga Satuan Barukan (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Basis Data Harga Material Wilayah",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Real-time Standar AHSP & Pasaran",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("regional_back_btn")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    Button(
                        onClick = { viewModel.syncProjectWithRegionalPrices() },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("sync_to_rab_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sinkronkan ke RAB", fontSize = 11.sp)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Region Filter Tab / Row
            ScrollableTabRow(
                selectedTabIndex = regions.indexOf(selectedRegion).coerceAtLeast(0),
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                regions.forEach { reg ->
                    Tab(
                        selected = selectedRegion == reg,
                        onClick = { viewModel.setSelectedRegion(reg) },
                        text = { Text(text = reg, fontSize = 12.sp) }
                    )
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari material / upah...") },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("regional_search_input"),
                singleLine = true
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredPrices, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = item.category.uppercase(), fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text(text = item.itemName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(
                                    text = "Satuan: ${item.unit} • Update: ${item.lastUpdatedDate}",
                                    fontSize = 10.5.sp,
                                    color = Color.Gray
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = ExportUtils.formatRupiah(item.price),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                IconButton(
                                    onClick = { editingPrice = item },
                                    modifier = Modifier.testTag("edit_regional_price_${item.id}")
                                ) {
                                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Harga", modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
