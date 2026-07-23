package com.example.data.db

import androidx.room.*
import com.example.data.model.Project
import com.example.data.model.RabCategory
import com.example.data.model.RabItem
import com.example.data.model.RegionalPrice
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY startDateEpoch DESC")
    fun getAllProjects(): Flow<List<Project>>

    @Query("SELECT * FROM projects WHERE id = :id")
    fun getProjectById(id: Long): Flow<Project?>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getProjectByIdDirect(id: Long): Project?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: Project): Long

    @Update
    suspend fun updateProject(project: Project)

    @Delete
    suspend fun deleteProject(project: Project)
}

@Dao
interface RabDao {
    @Query("SELECT * FROM rab_categories WHERE projectId = :projectId ORDER BY sortOrder ASC")
    fun getCategoriesForProject(projectId: Long): Flow<List<RabCategory>>

    @Query("SELECT * FROM rab_items WHERE projectId = :projectId")
    fun getItemsForProject(projectId: Long): Flow<List<RabItem>>

    @Query("SELECT * FROM rab_items WHERE projectId = :projectId")
    suspend fun getItemsForProjectDirect(projectId: Long): List<RabItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: RabCategory): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: RabItem): Long

    @Update
    suspend fun updateCategory(category: RabCategory)

    @Update
    suspend fun updateItem(item: RabItem)

    @Delete
    suspend fun deleteCategory(category: RabCategory)

    @Delete
    suspend fun deleteItem(item: RabItem)

    @Query("DELETE FROM rab_items WHERE categoryId = :categoryId")
    suspend fun deleteItemsByCategoryId(categoryId: Long)
}

@Dao
interface RegionalPriceDao {
    @Query("SELECT * FROM regional_prices WHERE regionName = :region ORDER BY category, itemName")
    fun getPricesByRegion(region: String): Flow<List<RegionalPrice>>

    @Query("SELECT DISTINCT regionName FROM regional_prices")
    fun getAllRegions(): Flow<List<String>>

    @Query("SELECT * FROM regional_prices ORDER BY regionName, category, itemName")
    fun getAllPrices(): Flow<List<RegionalPrice>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrices(prices: List<RegionalPrice>)

    @Update
    suspend fun updatePrice(price: RegionalPrice)

    @Query("UPDATE regional_prices SET price = :newPrice, lastUpdatedDate = :updatedDate WHERE id = :id")
    suspend fun updatePriceValue(id: Long, newPrice: Double, updatedDate: String)
}
