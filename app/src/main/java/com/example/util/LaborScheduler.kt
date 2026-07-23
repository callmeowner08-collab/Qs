package com.example.util

import com.example.data.model.RabItem

data class LaborEstimateResult(
    val totalManDays: Double,
    val totalWorkDays: Double,
    val totalWorkWeeks: Double,
    val totalLaborCost: Double,
    val itemBreakdowns: List<ItemLaborDetail>
)

data class ItemLaborDetail(
    val itemId: Long,
    val itemName: String,
    val volume: Double,
    val unit: String,
    val productivityRate: Double,
    val manDaysRequired: Double,
    val daysWithTeam: Double
)

object LaborScheduler {

    fun calculateSchedule(
        items: List<RabItem>,
        workerCount: Int,
        dailyWagePerWorker: Double
    ): LaborEstimateResult {
        var totalManDays = 0.0
        val details = mutableListOf<ItemLaborDetail>()

        val safeWorkerCount = if (workerCount > 0) workerCount else 1

        for (item in items) {
            val md = item.requiredManDays
            totalManDays += md

            val daysWithTeam = if (safeWorkerCount > 0) md / safeWorkerCount else md
            details.add(
                ItemLaborDetail(
                    itemId = item.id,
                    itemName = item.itemName,
                    volume = item.volume,
                    unit = item.unit,
                    productivityRate = item.laborProductivityRate,
                    manDaysRequired = md,
                    daysWithTeam = daysWithTeam
                )
            )
        }

        val totalWorkDays = totalManDays / safeWorkerCount
        val totalWorkWeeks = totalWorkDays / 6.0 // 6 days / week standard
        val totalLaborCost = totalManDays * dailyWagePerWorker

        return LaborEstimateResult(
            totalManDays = totalManDays,
            totalWorkDays = totalWorkDays,
            totalWorkWeeks = totalWorkWeeks,
            totalLaborCost = totalLaborCost,
            itemBreakdowns = details
        )
    }
}
