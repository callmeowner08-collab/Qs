package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.Project
import com.example.data.model.RabCategory
import com.example.data.model.RabItem
import com.example.data.model.RegionalPrice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Project::class, RabCategory::class, RabItem::class, RegionalPrice::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun rabDao(): RabDao
    abstract fun regionalPriceDao(): RegionalPriceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "qs_estimator_db"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }
        }

        private suspend fun populateInitialData(db: AppDatabase) {
            // 1. Initial Regional AHSP Price List
            val initialPrices = listOf(
                // DKI Jakarta
                RegionalPrice(regionName = "DKI Jakarta", category = "Material Utama", itemName = "Semen Portland (50 kg)", unit = "sak", price = 78000.0),
                RegionalPrice(regionName = "DKI Jakarta", category = "Material Utama", itemName = "Pasir Pasang Cor / Pasangan", unit = "m³", price = 320000.0),
                RegionalPrice(regionName = "DKI Jakarta", category = "Material Utama", itemName = "Batu Belah / Kali", unit = "m³", price = 290000.0),
                RegionalPrice(regionName = "DKI Jakarta", category = "Material Utama", itemName = "Bata Merah Pres Super", unit = "bh", price = 1100.0),
                RegionalPrice(regionName = "DKI Jakarta", category = "Material Utama", itemName = "Bata Ringan (Hebel) 10cm", unit = "m³", price = 680000.0),
                RegionalPrice(regionName = "DKI Jakarta", category = "Material Utama", itemName = "Besi Beton Polos Dia. 10mm", unit = "btg", price = 82000.0),
                RegionalPrice(regionName = "DKI Jakarta", category = "Material Finis", itemName = "Keramik Granit Tile 60x60 Polished", unit = "m²", price = 185000.0),
                RegionalPrice(regionName = "DKI Jakarta", category = "Material Finis", itemName = "Cat Dinding Interior Premium", unit = "galon", price = 245000.0),
                RegionalPrice(regionName = "DKI Jakarta", category = "Upah Tenaga Kerja", itemName = "Tukang Bangunan / Kepala Tukang", unit = "OH", price = 180000.0),
                RegionalPrice(regionName = "DKI Jakarta", category = "Upah Tenaga Kerja", itemName = "Pekerja / Laden Tukang", unit = "OH", price = 135000.0),

                // Jawa Barat
                RegionalPrice(regionName = "Jawa Barat", category = "Material Utama", itemName = "Semen Portland (50 kg)", unit = "sak", price = 72000.0),
                RegionalPrice(regionName = "Jawa Barat", category = "Material Utama", itemName = "Pasir Pasang Cor / Pasangan", unit = "m³", price = 270000.0),
                RegionalPrice(regionName = "Jawa Barat", category = "Material Utama", itemName = "Batu Belah / Kali", unit = "m³", price = 240000.0),
                RegionalPrice(regionName = "Jawa Barat", category = "Material Utama", itemName = "Bata Merah Pres Super", unit = "bh", price = 900.0),
                RegionalPrice(regionName = "Jawa Barat", category = "Material Utama", itemName = "Bata Ringan (Hebel) 10cm", unit = "m³", price = 620000.0),
                RegionalPrice(regionName = "Jawa Barat", category = "Material Utama", itemName = "Besi Beton Polos Dia. 10mm", unit = "btg", price = 78000.0),
                RegionalPrice(regionName = "Jawa Barat", category = "Material Finis", itemName = "Keramik Granit Tile 60x60 Polished", unit = "m²", price = 165000.0),
                RegionalPrice(regionName = "Jawa Barat", category = "Upah Tenaga Kerja", itemName = "Tukang Bangunan / Kepala Tukang", unit = "OH", price = 160000.0),
                RegionalPrice(regionName = "Jawa Barat", category = "Upah Tenaga Kerja", itemName = "Pekerja / Laden Tukang", unit = "OH", price = 120000.0),

                // Jawa Timur
                RegionalPrice(regionName = "Jawa Timur", category = "Material Utama", itemName = "Semen Portland (50 kg)", unit = "sak", price = 68000.0),
                RegionalPrice(regionName = "Jawa Timur", category = "Material Utama", itemName = "Pasir Pasang Cor / Pasangan", unit = "m³", price = 250000.0),
                RegionalPrice(regionName = "Jawa Timur", category = "Material Utama", itemName = "Batu Belah / Kali", unit = "m³", price = 220000.0),
                RegionalPrice(regionName = "Jawa Timur", category = "Material Utama", itemName = "Bata Merah Pres Super", unit = "bh", price = 850.0),
                RegionalPrice(regionName = "Jawa Timur", category = "Material Utama", itemName = "Bata Ringan (Hebel) 10cm", unit = "m³", price = 590000.0),
                RegionalPrice(regionName = "Jawa Timur", category = "Upah Tenaga Kerja", itemName = "Tukang Bangunan / Kepala Tukang", unit = "OH", price = 150000.0),
                RegionalPrice(regionName = "Jawa Timur", category = "Upah Tenaga Kerja", itemName = "Pekerja / Laden Tukang", unit = "OH", price = 110000.0),

                // Bali
                RegionalPrice(regionName = "Bali", category = "Material Utama", itemName = "Semen Portland (50 kg)", unit = "sak", price = 82000.0),
                RegionalPrice(regionName = "Bali", category = "Material Utama", itemName = "Pasir Pasang Cor / Pasangan", unit = "m³", price = 340000.0),
                RegionalPrice(regionName = "Bali", category = "Material Utama", itemName = "Batu Kali / Batu Hitam", unit = "m³", price = 310000.0),
                RegionalPrice(regionName = "Bali", category = "Material Utama", itemName = "Bata Ringan (Hebel) 10cm", unit = "m³", price = 720000.0),
                RegionalPrice(regionName = "Bali", category = "Upah Tenaga Kerja", itemName = "Tukang Bangunan / Ukir", unit = "OH", price = 190000.0),
                RegionalPrice(regionName = "Bali", category = "Upah Tenaga Kerja", itemName = "Pekerja / Laden Tukang", unit = "OH", price = 140000.0),

                // Sumatra Utara (Medan)
                RegionalPrice(regionName = "Sumatra Utara", category = "Material Utama", itemName = "Semen Portland (50 kg)", unit = "sak", price = 79000.0),
                RegionalPrice(regionName = "Sumatra Utara", category = "Material Utama", itemName = "Pasir Pasang Cor / Pasangan", unit = "m³", price = 310000.0),
                RegionalPrice(regionName = "Sumatra Utara", category = "Upah Tenaga Kerja", itemName = "Tukang Bangunan", unit = "OH", price = 170000.0),
                RegionalPrice(regionName = "Sumatra Utara", category = "Upah Tenaga Kerja", itemName = "Pekerja / Laden Tukang", unit = "OH", price = 125000.0)
            )
            db.regionalPriceDao().insertPrices(initialPrices)

            // 2. Initial Sample Project
            val sampleProject = Project(
                title = "Pembangunan Rumah Minimalis 2 Lantai",
                clientName = "Bpk. Christian",
                locationRegion = "DKI Jakarta",
                projectType = "Rumah Tinggal",
                targetWorkerCount = 8,
                dailyWorkerWage = 160000.0,
                progressPercent = 42f,
                ppnTaxPercent = 11.0,
                overheadPercent = 10.0,
                notes = "Lokasi proyek BSD City. Target pengerjaan 90 hari kerja."
            )
            val projectId = db.projectDao().insertProject(sampleProject)

            // Categories & Items
            val catPersiapanId = db.rabDao().insertCategory(
                RabCategory(projectId = projectId, categoryName = "I. PEKERJAAN PERSIAPAN & TANAH", sortOrder = 1)
            )
            db.rabDao().insertItem(RabItem(projectId = projectId, categoryId = catPersiapanId, itemName = "Pembersihan Lapangan & Pengukuran Bowplank", volume = 120.0, unit = "m²", unitPrice = 28000.0, laborProductivityRate = 25.0, progressPercent = 100f))
            db.rabDao().insertItem(RabItem(projectId = projectId, categoryId = catPersiapanId, itemName = "Galian Tanah Pondasi Batu Kali", volume = 45.0, unit = "m³", unitPrice = 75000.0, laborProductivityRate = 3.5, progressPercent = 100f))

            val catStrukturId = db.rabDao().insertCategory(
                RabCategory(projectId = projectId, categoryName = "II. PEKERJAAN PONDASI & STRUKTUR", sortOrder = 2)
            )
            db.rabDao().insertItem(RabItem(projectId = projectId, categoryId = catStrukturId, itemName = "Pondasi Batu Kali Adonan 1:4", volume = 28.0, unit = "m³", unitPrice = 890000.0, laborProductivityRate = 2.5, progressPercent = 90f))
            db.rabDao().insertItem(RabItem(projectId = projectId, categoryId = catStrukturId, itemName = "Cor Beton Bertulang Sloof 15x20cm", volume = 6.5, unit = "m³", unitPrice = 4200000.0, laborProductivityRate = 1.2, progressPercent = 80f))
            db.rabDao().insertItem(RabItem(projectId = projectId, categoryId = catStrukturId, itemName = "Cor Beton Bertulang Kolom Utama 20x20cm", volume = 8.2, unit = "m³", unitPrice = 4800000.0, laborProductivityRate = 1.0, progressPercent = 50f))

            val catDindingId = db.rabDao().insertCategory(
                RabCategory(projectId = projectId, categoryName = "III. PEKERJAAN DINDING & PLESTERAN", sortOrder = 3)
            )
            db.rabDao().insertItem(RabItem(projectId = projectId, categoryId = catDindingId, itemName = "Pasangan Dinding Bata Ringan (Hebel) t=10cm", volume = 210.0, unit = "m²", unitPrice = 145000.0, laborProductivityRate = 8.0, progressPercent = 40f))
            db.rabDao().insertItem(RabItem(projectId = projectId, categoryId = catDindingId, itemName = "Plesteran Acian Dinding Halus", volume = 420.0, unit = "m²", unitPrice = 82000.0, laborProductivityRate = 12.0, progressPercent = 20f))

            val catAtapLantaiId = db.rabDao().insertCategory(
                RabCategory(projectId = projectId, categoryName = "IV. PEKERJAAN ATAP & LANTAI", sortOrder = 4)
            )
            db.rabDao().insertItem(RabItem(projectId = projectId, categoryId = catAtapLantaiId, itemName = "Rangka Atap Baja Ringan C75 + Genteng Metal", volume = 135.0, unit = "m²", unitPrice = 240000.0, laborProductivityRate = 15.0, progressPercent = 0f))
            db.rabDao().insertItem(RabItem(projectId = projectId, categoryId = catAtapLantaiId, itemName = "Pemasangan Lantai Granit Tile 60x60cm", volume = 140.0, unit = "m²", unitPrice = 265000.0, laborProductivityRate = 7.0, progressPercent = 0f))

            val catFinishingId = db.rabDao().insertCategory(
                RabCategory(projectId = projectId, categoryName = "V. PEKERJAAN FINISHING & CAT", sortOrder = 5)
            )
            db.rabDao().insertItem(RabItem(projectId = projectId, categoryId = catFinishingId, itemName = "Pengecatan Dinding Interior 3 Lapis", volume = 420.0, unit = "m²", unitPrice = 38000.0, laborProductivityRate = 20.0, progressPercent = 0f))
            db.rabDao().insertItem(RabItem(projectId = projectId, categoryId = catFinishingId, itemName = "Instalasi Titik Lampu & Stop Kontak", volume = 32.0, unit = "titik", unitPrice = 185000.0, laborProductivityRate = 6.0, progressPercent = 10f))
        }
    }
}
