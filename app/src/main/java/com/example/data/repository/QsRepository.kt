package com.example.data.repository

import com.example.data.db.ProjectDao
import com.example.data.db.RabDao
import com.example.data.db.RegionalPriceDao
import com.example.data.model.Project
import com.example.data.model.RabCategory
import com.example.data.model.RabItem
import com.example.data.model.RegionalPrice
import kotlinx.coroutines.flow.Flow

class QsRepository(
    private val projectDao: ProjectDao,
    private val rabDao: RabDao,
    private val regionalPriceDao: RegionalPriceDao
) {
    val allProjects: Flow<List<Project>> = projectDao.getAllProjects()
    val allRegions: Flow<List<String>> = regionalPriceDao.getAllRegions()

    fun getProject(id: Long): Flow<Project?> = projectDao.getProjectById(id)

    fun getCategoriesForProject(projectId: Long): Flow<List<RabCategory>> =
        rabDao.getCategoriesForProject(projectId)

    fun getItemsForProject(projectId: Long): Flow<List<RabItem>> =
        rabDao.getItemsForProject(projectId)

    fun getRegionalPrices(region: String): Flow<List<RegionalPrice>> =
        regionalPriceDao.getPricesByRegion(region)

    fun getAllRegionalPrices(): Flow<List<RegionalPrice>> =
        regionalPriceDao.getAllPrices()

    suspend fun insertProject(project: Project): Long = projectDao.insertProject(project)

    suspend fun updateProject(project: Project) = projectDao.updateProject(project)

    suspend fun deleteProject(project: Project) = projectDao.deleteProject(project)

    suspend fun insertCategory(category: RabCategory): Long = rabDao.insertCategory(category)

    suspend fun updateCategory(category: RabCategory) = rabDao.updateCategory(category)

    suspend fun deleteCategory(category: RabCategory) = rabDao.deleteCategory(category)

    suspend fun insertRabItem(item: RabItem): Long = rabDao.insertItem(item)

    suspend fun updateRabItem(item: RabItem) = rabDao.updateItem(item)

    suspend fun deleteRabItem(item: RabItem) = rabDao.deleteItem(item)

    suspend fun updateRegionalPrice(priceId: Long, newPrice: Double, updatedDate: String) {
        regionalPriceDao.updatePriceValue(priceId, newPrice, updatedDate)
    }

    suspend fun addRegionalPrice(price: RegionalPrice) {
        regionalPriceDao.insertPrices(listOf(price))
    }

    suspend fun duplicateProject(projectId: Long, newTitle: String) {
        val original = projectDao.getProjectByIdDirect(projectId) ?: return
        val newProjId = projectDao.insertProject(
            original.copy(
                id = 0,
                title = newTitle,
                progressPercent = 0f,
                startDateEpoch = System.currentTimeMillis()
            )
        )

        val items = rabDao.getItemsForProjectDirect(projectId)
        // Note: Duplicate items cleanly with new category mapping
        // We fetch categories to preserve category structure
    }
}
