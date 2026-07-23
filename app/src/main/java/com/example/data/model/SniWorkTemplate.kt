package com.example.data.model

data class SniMaterialCoef(
    val materialName: String,
    val unit: String,
    val coefficient: Double
)

data class SniLaborCoef(
    val roleName: String,
    val coefficientOh: Double
)

data class SniWorkTemplate(
    val code: String,
    val workName: String,
    val unit: String,
    val category: String,
    val defaultUnitPrice: Double,
    val laborProductivityRate: Double,
    val materialCoefficients: List<SniMaterialCoef>,
    val laborCoefficients: List<SniLaborCoef> = emptyList()
)

object SniAhspCatalog {

    val TEMPLATES = listOf(
        // I. PEKERJAAN PERSIAPAN & TANAH
        SniWorkTemplate(
            code = "SNI 2835:2008 - 6.1",
            workName = "Pembersihan Lapangan & Ratakan Tanah",
            unit = "m²",
            category = "I. PEKERJAAN PERSIAPAN & TANAH",
            defaultUnitPrice = 28000.0,
            laborProductivityRate = 25.0,
            materialCoefficients = emptyList(),
            laborCoefficients = listOf(
                SniLaborCoef("Pekerja", 0.100),
                SniLaborCoef("Mandor", 0.005)
            )
        ),
        SniWorkTemplate(
            code = "SNI 2835:2008 - 6.2",
            workName = "Pemasangan Bouwplank / Pengukuran",
            unit = "m'",
            category = "I. PEKERJAAN PERSIAPAN & TANAH",
            defaultUnitPrice = 48000.0,
            laborProductivityRate = 12.0,
            materialCoefficients = listOf(
                SniMaterialCoef("Kayu Balok 5/7", "m³", 0.012),
                SniMaterialCoef("Paku 5cm - 10cm", "kg", 0.020),
                SniMaterialCoef("Papan Kayu 3/20", "m³", 0.007)
            ),
            laborCoefficients = listOf(
                SniLaborCoef("Pekerja", 0.100),
                SniLaborCoef("Tukang Kayu", 0.100),
                SniLaborCoef("Kepala Tukang", 0.010),
                SniLaborCoef("Mandor", 0.005)
            )
        ),
        SniWorkTemplate(
            code = "SNI 2835:2008 - 6.3",
            workName = "Galian Tanah Pondasi (Kedalaman < 1 Meter)",
            unit = "m³",
            category = "I. PEKERJAAN PERSIAPAN & TANAH",
            defaultUnitPrice = 85000.0,
            laborProductivityRate = 3.5,
            materialCoefficients = emptyList(),
            laborCoefficients = listOf(
                SniLaborCoef("Pekerja", 0.750),
                SniLaborCoef("Mandor", 0.025)
            )
        ),
        SniWorkTemplate(
            code = "SNI 2835:2008 - 6.9",
            workName = "Urugan Pasir Bawah Pondasi t=10cm",
            unit = "m³",
            category = "I. PEKERJAAN PERSIAPAN & TANAH",
            defaultUnitPrice = 280000.0,
            laborProductivityRate = 4.0,
            materialCoefficients = listOf(
                SniMaterialCoef("Pasir Urug Bawah Pondasi", "m³", 1.200)
            ),
            laborCoefficients = listOf(
                SniLaborCoef("Pekerja", 0.300),
                SniLaborCoef("Mandor", 0.010)
            )
        ),

        // II. PEKERJAAN PONDASI & STRUKTUR
        SniWorkTemplate(
            code = "SNI 2836:2008 - 6.2",
            workName = "Pondasi Batu Kali Adonan 1:4",
            unit = "m³",
            category = "II. PEKERJAAN PONDASI & STRUKTUR",
            defaultUnitPrice = 920000.0,
            laborProductivityRate = 2.5,
            materialCoefficients = listOf(
                SniMaterialCoef("Batu Belah / Kali 15/20cm", "m³", 1.200),
                SniMaterialCoef("Semen Portland (50kg)", "sak", 3.260),
                SniMaterialCoef("Pasir Pasang Cor", "m³", 0.520)
            ),
            laborCoefficients = listOf(
                SniLaborCoef("Pekerja", 1.500),
                SniLaborCoef("Tukang Batu", 0.750),
                SniLaborCoef("Kepala Tukang", 0.075),
                SniLaborCoef("Mandor", 0.075)
            )
        ),
        SniWorkTemplate(
            code = "SNI 7394:2008 - 6.1",
            workName = "Cor Beton Bertulang K-225 (Sloof 15x20cm)",
            unit = "m³",
            category = "II. PEKERJAAN PONDASI & STRUKTUR",
            defaultUnitPrice = 4250000.0,
            laborProductivityRate = 1.2,
            materialCoefficients = listOf(
                SniMaterialCoef("Semen Portland (50kg)", "sak", 7.420),
                SniMaterialCoef("Pasir Cor", "m³", 0.498),
                SniMaterialCoef("Kerikil / Spilit 2/3", "m³", 0.776),
                SniMaterialCoef("Besi Beton Ulir/Polos", "kg", 110.0),
                SniMaterialCoef("Kawat Beton (Bendrat)", "kg", 2.200),
                SniMaterialCoef("Kayu Bekisting 5/7", "m³", 0.270),
                SniMaterialCoef("Paku 5cm - 10cm", "kg", 2.000),
                SniMaterialCoef("Minyak Bekisting", "liter", 0.600)
            ),
            laborCoefficients = listOf(
                SniLaborCoef("Pekerja", 1.650),
                SniLaborCoef("Tukang Batu", 0.275),
                SniLaborCoef("Tukang Besi", 0.330),
                SniLaborCoef("Tukang Kayu", 0.330),
                SniLaborCoef("Mandor", 0.083)
            )
        ),
        SniWorkTemplate(
            code = "SNI 7394:2008 - 6.2",
            workName = "Cor Beton Bertulang K-225 (Kolom Utama 20x20cm)",
            unit = "m³",
            category = "II. PEKERJAAN PONDASI & STRUKTUR",
            defaultUnitPrice = 4850000.0,
            laborProductivityRate = 1.0,
            materialCoefficients = listOf(
                SniMaterialCoef("Semen Portland (50kg)", "sak", 7.420),
                SniMaterialCoef("Pasir Cor", "m³", 0.498),
                SniMaterialCoef("Kerikil / Spilit 2/3", "m³", 0.776),
                SniMaterialCoef("Besi Beton Ulir/Polos", "kg", 135.0),
                SniMaterialCoef("Kawat Beton (Bendrat)", "kg", 2.700),
                SniMaterialCoef("Multiplek Bekisting 12mm", "lembar", 3.500),
                SniMaterialCoef("Kayu Balok Support 5/7", "m³", 0.350),
                SniMaterialCoef("Paku 5cm - 10cm", "kg", 3.200),
                SniMaterialCoef("Minyak Bekisting", "liter", 1.200)
            ),
            laborCoefficients = listOf(
                SniLaborCoef("Pekerja", 2.100),
                SniLaborCoef("Tukang Batu", 0.350),
                SniLaborCoef("Tukang Besi", 0.400),
                SniLaborCoef("Tukang Kayu", 0.400),
                SniLaborCoef("Mandor", 0.105)
            )
        ),
        SniWorkTemplate(
            code = "AHSP B-3.2",
            workName = "Cor Plat Lantai Beton t=12cm (Bondek + Wiremesh)",
            unit = "m²",
            category = "II. PEKERJAAN PONDASI & STRUKTUR",
            defaultUnitPrice = 540000.0,
            laborProductivityRate = 8.0,
            materialCoefficients = listOf(
                SniMaterialCoef("Bondek Galvalum 0.75mm", "m²", 1.050),
                SniMaterialCoef("Wiremesh M8 Single Layer", "m²", 1.050),
                SniMaterialCoef("Beton Readymix K-250", "m³", 0.125)
            ),
            laborCoefficients = listOf(
                SniLaborCoef("Pekerja", 0.250),
                SniLaborCoef("Tukang Batu", 0.120),
                SniLaborCoef("Mandor", 0.015)
            )
        ),

        // III. PEKERJAAN DINDING & PLESTERAN
        SniWorkTemplate(
            code = "AHSP D-1.2",
            workName = "Pasangan Dinding Bata Ringan (Hebel) t=10cm",
            unit = "m²",
            category = "III. PEKERJAAN DINDING & PLESTERAN",
            defaultUnitPrice = 145000.0,
            laborProductivityRate = 8.0,
            materialCoefficients = listOf(
                SniMaterialCoef("Bata Ringan (Hebel) 10cm", "m³", 0.100),
                SniMaterialCoef("Semen Mortar Perekat Hebel", "kg", 4.000)
            ),
            laborCoefficients = listOf(
                SniLaborCoef("Pekerja", 0.200),
                SniLaborCoef("Tukang Batu", 0.100),
                SniLaborCoef("Mandor", 0.010)
            )
        ),
        SniWorkTemplate(
            code = "SNI 6897:2008 - 6.1",
            workName = "Pasangan Dinding Bata Merah 1/2 Batu Adonan 1:4",
            unit = "m²",
            category = "III. PEKERJAAN DINDING & PLESTERAN",
            defaultUnitPrice = 165000.0,
            laborProductivityRate = 6.0,
            materialCoefficients = listOf(
                SniMaterialCoef("Bata Merah Pres Super", "bh", 70.0),
                SniMaterialCoef("Semen Portland (50kg)", "sak", 0.230),
                SniMaterialCoef("Pasir Pasang", "m³", 0.043)
            ),
            laborCoefficients = listOf(
                SniLaborCoef("Pekerja", 0.300),
                SniLaborCoef("Tukang Batu", 0.100),
                SniLaborCoef("Mandor", 0.015)
            )
        ),
        SniWorkTemplate(
            code = "SNI 2837:2008 - 6.4",
            workName = "Plesteran Dinding Tebal 15mm Adonan 1:4",
            unit = "m²",
            category = "III. PEKERJAAN DINDING & PLESTERAN",
            defaultUnitPrice = 58000.0,
            laborProductivityRate = 12.0,
            materialCoefficients = listOf(
                SniMaterialCoef("Semen Portland (50kg)", "sak", 0.125),
                SniMaterialCoef("Pasir Pasang Fine", "m³", 0.024)
            ),
            laborCoefficients = listOf(
                SniLaborCoef("Pekerja", 0.300),
                SniLaborCoef("Tukang Batu", 0.150),
                SniLaborCoef("Mandor", 0.015)
            )
        ),
        SniWorkTemplate(
            code = "SNI 2837:2008 - 6.27",
            workName = "Acian Dinding Halus Mortar/Semen",
            unit = "m²",
            category = "III. PEKERJAAN DINDING & PLESTERAN",
            defaultUnitPrice = 32000.0,
            laborProductivityRate = 18.0,
            materialCoefficients = listOf(
                SniMaterialCoef("Semen Portland / Mortar Acian", "kg", 3.250)
            ),
            laborCoefficients = listOf(
                SniLaborCoef("Pekerja", 0.200),
                SniLaborCoef("Tukang Batu", 0.100),
                SniLaborCoef("Mandor", 0.010)
            )
        ),

        // IV. PEKERJAAN ATAP, PLAFOND & LANTAI
        SniWorkTemplate(
            code = "AHSP A-2.1",
            workName = "Rangka Atap Baja Ringan C75.75 + Genteng Metal",
            unit = "m²",
            category = "IV. PEKERJAAN ATAP & LANTAI",
            defaultUnitPrice = 245000.0,
            laborProductivityRate = 15.0,
            materialCoefficients = listOf(
                SniMaterialCoef("Truss Baja Ringan C75.75", "m'", 4.200),
                SniMaterialCoef("Reng Galvalum 0.45", "m'", 3.100),
                SniMaterialCoef("Genteng Metal Pasir", "m²", 1.050),
                SniMaterialCoef("Sekrup Roof / SDS Screw", "bh", 28.0)
            ),
            laborCoefficients = listOf(
                SniLaborCoef("Pekerja", 0.150),
                SniLaborCoef("Tukang Khusus Atap", 0.100),
                SniLaborCoef("Mandor", 0.010)
            )
        ),
        SniWorkTemplate(
            code = "SNI 7395:2008 - 6.1",
            workName = "Pemasangan Lantai Granit Tile 60x60cm Polished",
            unit = "m²",
            category = "IV. PEKERJAAN ATAP & LANTAI",
            defaultUnitPrice = 275000.0,
            laborProductivityRate = 7.0,
            materialCoefficients = listOf(
                SniMaterialCoef("Granit Tile 60x60cm", "m²", 1.050),
                SniMaterialCoef("Semen Perekat Keramik / Mortar", "kg", 8.200),
                SniMaterialCoef("Semen Warna Pengisi Nat (Grout)", "kg", 0.400)
            ),
            laborCoefficients = listOf(
                SniLaborCoef("Pekerja", 0.260),
                SniLaborCoef("Tukang Batu", 0.130),
                SniLaborCoef("Mandor", 0.013)
            )
        ),
        SniWorkTemplate(
            code = "SNI 2839:2008 - 6.1",
            workName = "Pemasangan Plafond Gypsum Board 9mm + Rangka Hollow",
            unit = "m²",
            category = "IV. PEKERJAAN ATAP & LANTAI",
            defaultUnitPrice = 115000.0,
            laborProductivityRate = 10.0,
            materialCoefficients = listOf(
                SniMaterialCoef("Gypsum Board 9mm (1.2x2.4m)", "lembar", 0.364),
                SniMaterialCoef("Hollow Galvalum 2x4 & 4x4", "m'", 3.800),
                SniMaterialCoef("Sekrup Gypsum 1 inch", "bh", 20.0),
                SniMaterialCoef("Kain Kasa Tape + Compound", "m'", 1.800)
            ),
            laborCoefficients = listOf(
                SniLaborCoef("Pekerja", 0.200),
                SniLaborCoef("Tukang Gypsum", 0.100),
                SniLaborCoef("Mandor", 0.010)
            )
        ),

        // V. PEKERJAAN FINISHING & SANITASI
        SniWorkTemplate(
            code = "SNI 2839:2008 - 6.10",
            workName = "Pengecatan Dinding Interior 3 Lapis",
            unit = "m²",
            category = "V. PEKERJAAN FINISHING & CAT",
            defaultUnitPrice = 38000.0,
            laborProductivityRate = 22.0,
            materialCoefficients = listOf(
                SniMaterialCoef("Cat Dinding Emulsi Interior", "kg", 0.260),
                SniMaterialCoef("Cat Dasar Alkali Primer", "kg", 0.100),
                SniMaterialCoef("Plamuur Dinding Tembok", "kg", 0.150),
                SniMaterialCoef("Amplas Dinding No. 120", "lembar", 0.200)
            ),
            laborCoefficients = listOf(
                SniLaborCoef("Pekerja", 0.020),
                SniLaborCoef("Tukang Cat", 0.063),
                SniLaborCoef("Mandor", 0.003)
            )
        ),
        SniWorkTemplate(
            code = "AHSP S-1.1",
            workName = "Instalasi Titik Lampu & Stop Kontak NYM 3x2.5mm",
            unit = "titik",
            category = "V. PEKERJAAN FINISHING & CAT",
            defaultUnitPrice = 195000.0,
            laborProductivityRate = 6.0,
            materialCoefficients = listOf(
                SniMaterialCoef("Kabel NYM 3x2.5mm²", "m'", 12.0),
                SniMaterialCoef("Pipa Conduit PVC 20mm", "btg", 3.0),
                SniMaterialCoef("Inbowdus & T-Dus PVC", "bh", 2.0)
            ),
            laborCoefficients = listOf(
                SniLaborCoef("Pekerja", 0.200),
                SniLaborCoef("Tukang Listrik", 0.200),
                SniLaborCoef("Mandor", 0.020)
            )
        ),

        // VI. PEKERJAAN IRIGASI, TURAP & REVETMENT
        SniWorkTemplate(
            code = "AHSP SDA 01 / SNI 2836:2008",
            workName = "Pasangan Batu Kali Adonan 1:3 (Turap / Irigasi / Revetment)",
            unit = "m³",
            category = "VI. PEKERJAAN IRIGASI & TURAP",
            defaultUnitPrice = 980000.0,
            laborProductivityRate = 2.2,
            materialCoefficients = listOf(
                SniMaterialCoef("Batu Belah / Kali 15/20cm", "m³", 1.200),
                SniMaterialCoef("Semen Portland (50kg)", "sak", 4.360),
                SniMaterialCoef("Pasir Pasang Fine", "m³", 0.432)
            ),
            laborCoefficients = listOf(
                SniLaborCoef("Pekerja", 1.500),
                SniLaborCoef("Tukang Batu", 0.750),
                SniLaborCoef("Kepala Tukang", 0.075),
                SniLaborCoef("Mandor", 0.075)
            )
        ),
        SniWorkTemplate(
            code = "AHSP SDA 02 / SNI 2836:2008",
            workName = "Pasangan Batu Kosong / Anstamping (Pencegah Erosional Revetment)",
            unit = "m³",
            category = "VI. PEKERJAAN IRIGASI & TURAP",
            defaultUnitPrice = 480000.0,
            laborProductivityRate = 3.2,
            materialCoefficients = listOf(
                SniMaterialCoef("Batu Belah / Kali 15/20cm", "m³", 1.200),
                SniMaterialCoef("Pasir Urug / Bantal", "m³", 0.300)
            ),
            laborCoefficients = listOf(
                SniLaborCoef("Pekerja", 0.780),
                SniLaborCoef("Tukang Batu", 0.390),
                SniLaborCoef("Kepala Tukang", 0.039),
                SniLaborCoef("Mandor", 0.039)
            )
        ),
        SniWorkTemplate(
            code = "AHSP SDA 03 / SNI 2837:2008",
            workName = "Plesteran Siar Pasangan Batu Adonan 1:2 (Siar Saluran Irigasi)",
            unit = "m²",
            category = "VI. PEKERJAAN IRIGASI & TURAP",
            defaultUnitPrice = 52000.0,
            laborProductivityRate = 10.0,
            materialCoefficients = listOf(
                SniMaterialCoef("Semen Portland (50kg)", "sak", 0.127),
                SniMaterialCoef("Pasir Pasang Fine", "m³", 0.016)
            ),
            laborCoefficients = listOf(
                SniLaborCoef("Pekerja", 0.250),
                SniLaborCoef("Tukang Batu", 0.125),
                SniLaborCoef("Kepala Tukang", 0.013),
                SniLaborCoef("Mandor", 0.013)
            )
        ),
        SniWorkTemplate(
            code = "AHSP SDA 04",
            workName = "Pemasangan Pipa Sulingan PVC Dia 2\" + Filter Ijuk pada Turap",
            unit = "m'",
            category = "VI. PEKERJAAN IRIGASI & TURAP",
            defaultUnitPrice = 45000.0,
            laborProductivityRate = 18.0,
            materialCoefficients = listOf(
                SniMaterialCoef("Pipa PVC AW 2 inch", "m'", 1.050),
                SniMaterialCoef("Ijuk Saringan / Geotextile", "kg", 0.150),
                SniMaterialCoef("Kerikil Penapis Resapan", "m³", 0.020)
            ),
            laborCoefficients = listOf(
                SniLaborCoef("Pekerja", 0.100),
                SniLaborCoef("Tukang Pipas", 0.050),
                SniLaborCoef("Mandor", 0.005)
            )
        ),
        SniWorkTemplate(
            code = "AHSP SDA 05",
            workName = "Pekerjaan Bronjong Kawat / Gabion Pelindung Tebing Revetment",
            unit = "m³",
            category = "VI. PEKERJAAN IRIGASI & TURAP",
            defaultUnitPrice = 680000.0,
            laborProductivityRate = 2.5,
            materialCoefficients = listOf(
                SniMaterialCoef("Anyaman Kawat Bronjong Galvanis", "unit", 1.050),
                SniMaterialCoef("Batu Belah / Kali 15/25cm", "m³", 1.200),
                SniMaterialCoef("Kawat Pengikat Bronjong", "kg", 0.800)
            ),
            laborCoefficients = listOf(
                SniLaborCoef("Pekerja", 1.200),
                SniLaborCoef("Tukang Batu", 0.400),
                SniLaborCoef("Mandor", 0.060)
            )
        )
    )
}
