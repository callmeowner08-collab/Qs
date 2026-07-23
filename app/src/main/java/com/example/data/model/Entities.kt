package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val clientName: String,
    val locationRegion: String,
    val projectType: String, // e.g., "Rumah Tinggal", "Renovasi", "Ruko", "Gedung / Komersial"
    val targetWorkerCount: Int = 6,
    val dailyWorkerWage: Double = 150000.0,
    val startDateEpoch: Long = System.currentTimeMillis(),
    val progressPercent: Float = 0f,
    val ppnTaxPercent: Double = 11.0,
    val overheadPercent: Double = 10.0,
    val notes: String = ""
)

@Entity(
    tableName = "rab_categories",
    foreignKeys = [
        ForeignKey(
            entity = Project::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class RabCategory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val categoryName: String,
    val sortOrder: Int = 0
)

@Entity(
    tableName = "rab_items",
    foreignKeys = [
        ForeignKey(
            entity = Project::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = RabCategory::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class RabItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val categoryId: Long,
    val itemName: String,
    val volume: Double,
    val unit: String, // e.g., "m²", "m³", "m'", "kg", "bh", "ls"
    val unitPrice: Double,
    val laborProductivityRate: Double = 6.0, // e.g. 6 m²/worker/day
    val progressPercent: Float = 0f,
    val notes: String = ""
) {
    val totalPrice: Double get() = volume * unitPrice
    val requiredManDays: Double get() = if (laborProductivityRate > 0) volume / laborProductivityRate else 0.0
}

@Entity(tableName = "regional_prices")
data class RegionalPrice(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val regionName: String, // e.g. "DKI Jakarta", "Jawa Barat", "Jawa Timur", "Bali", "Sumatra Utara"
    val category: String, // e.g. "Material Utama", "Material Finis", "Upah Tenaga Kerja"
    val itemName: String,
    val unit: String,
    val price: Double,
    val lastUpdatedDate: String = "2026-07-22"
)
