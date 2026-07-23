package com.example.util

import kotlin.math.cos

object QsVolumeCalculator {

    fun calcWallArea(length: Double, height: Double, doorWindowArea: Double): Double {
        val total = (length * height) - doorWindowArea
        return if (total > 0) total else 0.0
    }

    fun calcFoundationVolume(topWidthMeter: Double, bottomWidthMeter: Double, heightMeter: Double, totalLengthMeter: Double): Double {
        val areaCrossSection = ((topWidthMeter + bottomWidthMeter) / 2.0) * heightMeter
        return areaCrossSection * totalLengthMeter
    }

    fun calcConcreteVolume(lengthMeter: Double, widthMeter: Double, heightOrThicknessMeter: Double, count: Int = 1): Double {
        return lengthMeter * widthMeter * heightOrThicknessMeter * count
    }

    fun calcFloorTileArea(lengthMeter: Double, widthMeter: Double, wastePercent: Double = 5.0): Double {
        val baseArea = lengthMeter * widthMeter
        return baseArea * (1.0 + (wastePercent / 100.0))
    }

    fun calcRoofArea(buildingLength: Double, buildingWidth: Double, roofAngleDegrees: Double = 30.0, overhangMeter: Double = 0.8): Double {
        val totalLength = buildingLength + (overhangMeter * 2)
        val totalWidth = buildingWidth + (overhangMeter * 2)
        val flatArea = totalLength * totalWidth
        val rad = Math.toRadians(roofAngleDegrees)
        val cosValue = cos(rad)
        return if (cosValue > 0) flatArea / cosValue else flatArea
    }

    fun calcEarthworkVolume(lengthMeter: Double, widthMeter: Double, depthMeter: Double): Double {
        return lengthMeter * widthMeter * depthMeter
    }

    fun calcPaintArea(wallArea: Double, coats: Int = 2): Double {
        return wallArea * coats
    }
}
