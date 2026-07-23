package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.Project
import com.example.data.model.RabCategory
import com.example.data.model.RabItem
import com.example.data.model.RegionalPrice
import com.example.data.model.SniAhspCatalog
import com.example.data.model.SniMaterialCoef
import com.example.data.repository.QsRepository
import com.example.util.LaborEstimateResult
import com.example.util.LaborScheduler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class MaterialSummaryItem(
    val materialName: String,
    val totalQuantity: Double,
    val unit: String,
    val estimatedTotalCost: Double
)

data class LaborOhSummaryItem(
    val roleName: String,
    val totalOh: Double,
    val estimatedDailyWage: Double = 0.0,
    val estimatedTotalCost: Double = 0.0
)

data class RabSummaryState(
    val subtotalBiayaFisik: Double = 0.0,
    val ppnAmount: Double = 0.0,
    val overheadAmount: Double = 0.0,
    val grandTotalRab: Double = 0.0,
    val categoryTotals: Map<Long, Double> = emptyMap(),
    val totalVolumeProgress: Float = 0f,
    val materialSummaryList: List<MaterialSummaryItem> = emptyList(),
    val laborOhSummaryList: List<LaborOhSummaryItem> = emptyList(),
    val laborEstimate: LaborEstimateResult? = null
)

class QsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: QsRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = QsRepository(db.projectDao(), db.rabDao(), db.regionalPriceDao())
    }

    val allProjects: StateFlow<List<Project>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRegions: StateFlow<List<String>> = repository.allRegions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("DKI Jakarta", "Jawa Barat", "Jawa Timur", "Bali", "Sumatra Utara"))

    private val _selectedProjectId = MutableStateFlow<Long?>(1L)
    val selectedProjectId: StateFlow<Long?> = _selectedProjectId.asStateFlow()

    fun selectProject(projectId: Long) {
        _selectedProjectId.value = projectId
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentProject: StateFlow<Project?> = _selectedProjectId.flatMapLatest { id ->
        if (id == null) flowOf(null)
        else repository.getProject(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val categoriesForCurrentProject: StateFlow<List<RabCategory>> = _selectedProjectId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList())
        else repository.getCategoriesForProject(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val itemsForCurrentProject: StateFlow<List<RabItem>> = _selectedProjectId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList())
        else repository.getItemsForProject(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Regional Prices Flow
    private val _selectedRegion = MutableStateFlow("DKI Jakarta")
    val selectedRegion: StateFlow<String> = _selectedRegion.asStateFlow()

    fun setSelectedRegion(region: String) {
        _selectedRegion.value = region
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val regionalPrices: StateFlow<List<RegionalPrice>> = _selectedRegion.flatMapLatest { region ->
        repository.getRegionalPrices(region)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Summary calculation derived state
    val rabSummary: StateFlow<RabSummaryState> = combine(
        currentProject,
        categoriesForCurrentProject,
        itemsForCurrentProject
    ) { proj, cats, items ->
        if (proj == null || items.isEmpty()) {
            RabSummaryState()
        } else {
            var subtotal = 0.0
            val catTotals = mutableMapOf<Long, Double>()
            var totalWeightedProgress = 0.0

            for (item in items) {
                val itemTotal = item.totalPrice
                subtotal += itemTotal
                catTotals[item.categoryId] = (catTotals[item.categoryId] ?: 0.0) + itemTotal
            }

            if (subtotal > 0) {
                for (item in items) {
                    val weight = item.totalPrice / subtotal
                    totalWeightedProgress += (item.progressPercent * weight)
                }
            }

            val ppn = subtotal * (proj.ppnTaxPercent / 100.0)
            val overhead = subtotal * (proj.overheadPercent / 100.0)
            val grand = subtotal + ppn + overhead

            // Calculate Detailed SNI Material & Labor Breakdown
            val materialMap = mutableMapOf<String, Pair<Double, String>>()
            val laborOhMap = mutableMapOf<String, Double>()

            for (item in items) {
                // Find matching SNI template by name similarity or catalog match
                val matchedTemplate = SniAhspCatalog.TEMPLATES.firstOrNull { template ->
                    item.itemName.contains(template.workName.take(10), ignoreCase = true) ||
                            template.workName.contains(item.itemName.take(10), ignoreCase = true)
                }

                if (matchedTemplate != null) {
                    for (coef in matchedTemplate.materialCoefficients) {
                        val needed = item.volume * coef.coefficient
                        val current = materialMap[coef.materialName]
                        if (current == null) {
                            materialMap[coef.materialName] = Pair(needed, coef.unit)
                        } else {
                            materialMap[coef.materialName] = Pair(current.first + needed, coef.unit)
                        }
                    }

                    for (laborCoef in matchedTemplate.laborCoefficients) {
                        val neededOh = item.volume * laborCoef.coefficientOh
                        laborOhMap[laborCoef.roleName] = (laborOhMap[laborCoef.roleName] ?: 0.0) + neededOh
                    }
                }
            }

            val baseWage = proj.dailyWorkerWage

            val materialSummaryList = materialMap.map { (matName, pair) ->
                val estimatedUnitPrice = getFallbackMaterialPrice(matName)
                MaterialSummaryItem(
                    materialName = matName,
                    totalQuantity = pair.first,
                    unit = pair.second,
                    estimatedTotalCost = pair.first * estimatedUnitPrice
                )
            }.sortedByDescending { it.totalQuantity }

            val laborOhSummaryList = laborOhMap.map { (role, totalOh) ->
                val multiplier = when {
                    role.contains("Mandor", ignoreCase = true) -> 1.35
                    role.contains("Kepala", ignoreCase = true) -> 1.25
                    role.contains("Tukang", ignoreCase = true) -> 1.15
                    else -> 1.0
                }
                val wageRate = baseWage * multiplier
                LaborOhSummaryItem(
                    roleName = role,
                    totalOh = totalOh,
                    estimatedDailyWage = wageRate,
                    estimatedTotalCost = totalOh * wageRate
                )
            }.sortedByDescending { it.totalOh }

            val laborResult = LaborScheduler.calculateSchedule(
                items = items,
                workerCount = proj.targetWorkerCount,
                dailyWagePerWorker = proj.dailyWorkerWage
            )

            RabSummaryState(
                subtotalBiayaFisik = subtotal,
                ppnAmount = ppn,
                overheadAmount = overhead,
                grandTotalRab = grand,
                categoryTotals = catTotals,
                totalVolumeProgress = totalWeightedProgress.toFloat(),
                materialSummaryList = materialSummaryList,
                laborOhSummaryList = laborOhSummaryList,
                laborEstimate = laborResult
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RabSummaryState())

    // Actions
    fun createNewProject(
        title: String,
        clientName: String,
        region: String,
        projectType: String,
        workerCount: Int,
        dailyWage: Double,
        ppnPercent: Double,
        overheadPercent: Double
    ) {
        viewModelScope.launch {
            val newProject = Project(
                title = title,
                clientName = clientName,
                locationRegion = region,
                projectType = projectType,
                targetWorkerCount = workerCount,
                dailyWorkerWage = dailyWage,
                ppnTaxPercent = ppnPercent,
                overheadPercent = overheadPercent,
                startDateEpoch = System.currentTimeMillis()
            )
            val newId = repository.insertProject(newProject)

            // Auto insert basic default SNI categories
            val cat1 = repository.insertCategory(RabCategory(projectId = newId, categoryName = "I. PEKERJAAN PERSIAPAN & TANAH", sortOrder = 1))
            val cat2 = repository.insertCategory(RabCategory(projectId = newId, categoryName = "II. PEKERJAAN PONDASI & STRUKTUR", sortOrder = 2))
            val cat3 = repository.insertCategory(RabCategory(projectId = newId, categoryName = "III. PEKERJAAN DINDING & PLESTERAN", sortOrder = 3))
            val cat4 = repository.insertCategory(RabCategory(projectId = newId, categoryName = "IV. PEKERJAAN ATAP & LANTAI", sortOrder = 4))
            val cat5 = repository.insertCategory(RabCategory(projectId = newId, categoryName = "V. PEKERJAAN FINISHING & CAT", sortOrder = 5))

            _selectedProjectId.value = newId
        }
    }

    fun updateProjectSettings(project: Project) {
        viewModelScope.launch {
            repository.updateProject(project)
        }
    }

    fun deleteProject(project: Project) {
        viewModelScope.launch {
            repository.deleteProject(project)
            val first = allProjects.value.firstOrNull { it.id != project.id }
            _selectedProjectId.value = first?.id
        }
    }

    fun addCategory(projectId: Long, name: String) {
        viewModelScope.launch {
            val count = categoriesForCurrentProject.value.size
            repository.insertCategory(RabCategory(projectId = projectId, categoryName = name, sortOrder = count + 1))
        }
    }

    fun addRabItem(
        projectId: Long,
        categoryId: Long,
        itemName: String,
        volume: Double,
        unit: String,
        unitPrice: Double,
        laborRate: Double
    ) {
        viewModelScope.launch {
            repository.insertRabItem(
                RabItem(
                    projectId = projectId,
                    categoryId = categoryId,
                    itemName = itemName,
                    volume = volume,
                    unit = unit,
                    unitPrice = unitPrice,
                    laborProductivityRate = laborRate
                )
            )
        }
    }

    fun updateRabItem(item: RabItem) {
        viewModelScope.launch {
            repository.updateRabItem(item)
        }
    }

    fun deleteRabItem(item: RabItem) {
        viewModelScope.launch {
            repository.deleteRabItem(item)
        }
    }

    fun importSniTemplateItem(projectId: Long, categoryId: Long, templateCode: String) {
        val tmpl = SniAhspCatalog.TEMPLATES.firstOrNull { it.code == templateCode } ?: return
        viewModelScope.launch {
            repository.insertRabItem(
                RabItem(
                    projectId = projectId,
                    categoryId = categoryId,
                    itemName = tmpl.workName,
                    volume = 10.0,
                    unit = tmpl.unit,
                    unitPrice = tmpl.defaultUnitPrice,
                    laborProductivityRate = tmpl.laborProductivityRate
                )
            )
        }
    }

    fun updateRegionalPrice(id: Long, newPrice: Double) {
        viewModelScope.launch {
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            repository.updateRegionalPrice(id, newPrice, dateStr)
        }
    }

    fun syncProjectWithRegionalPrices() {
        val proj = currentProject.value ?: return
        val currentItems = itemsForCurrentProject.value
        val regPrices = regionalPrices.value

        if (currentItems.isEmpty() || regPrices.isEmpty()) return

        viewModelScope.launch {
            for (item in currentItems) {
                // Find matching price in regional database
                val match = regPrices.firstOrNull { rp ->
                    item.itemName.contains(rp.itemName, ignoreCase = true) ||
                            rp.itemName.contains(item.itemName, ignoreCase = true)
                }
                if (match != null && match.price > 0) {
                    repository.updateRabItem(item.copy(unitPrice = match.price))
                }
            }
        }
    }

    private fun getFallbackMaterialPrice(name: String): Double {
        val lower = name.lowercase()
        return when {
            lower.contains("semen") -> 65000.0
            lower.contains("pasir") -> 280000.0
            lower.contains("batu belah") || lower.contains("batu kali") -> 290000.0
            lower.contains("kerikil") || lower.contains("spilit") -> 320000.0
            lower.contains("besi") -> 12500.0
            lower.contains("kawat") -> 25000.0
            lower.contains("hebel") || lower.contains("bata ringan") -> 650000.0
            lower.contains("bata merah") -> 1100.0
            lower.contains("paku") -> 22000.0
            lower.contains("kayu") -> 2500000.0
            lower.contains("granit") || lower.contains("keramik") -> 220000.0
            lower.contains("truss") || lower.contains("baja ringan") -> 32000.0
            lower.contains("genteng") -> 85000.0
            lower.contains("gypsum") -> 95000.0
            lower.contains("cat") -> 45000.0
            lower.contains("pipa") -> 35000.0
            else -> 25000.0
        }
    }
}
