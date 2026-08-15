package com.bhanu.ironlog.data.util

import java.util.Calendar
import kotlin.math.roundToInt

object BodyMetricsCalculator {

    fun calculateAge(dobMillis: Long): Int {
        val dob = Calendar.getInstance().apply { timeInMillis = dobMillis }
        val today = Calendar.getInstance()

        var age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--
        }

        return age
    }

    fun calculateBMI(weightKg: Double, heightCm: Double): Double {
        if (heightCm <= 0) return 0.0
        val heightMeters = heightCm / 100.0
        return weightKg / (heightMeters * heightMeters)
    }

    /**
     * Interprets BMI for adults based on Revised Consensus Body Mass Index
     * and Central Obesity Guidelines for Asian Indians (2009).
     *
     * Thresholds:
     * - Underweight: < 18.0
     * - Normal: 18.0 - 22.9
     * - Overweight: 23.0 - 24.9
     * - Obese: >= 25.0
     */
    fun interpretAdultBMI(bmi: Double): String {
        return when {
            bmi < 18.0 -> "Underweight"
            bmi < 23.0 -> "Normal"
            bmi < 25.0 -> "Overweight"
            else -> "Obese"
        }
    }

    fun isAdult(dobMillis: Long): Boolean {
        return calculateAge(dobMillis) >= 18
    }

    // Unit Conversions
    fun kgToLb(kg: Double): Double = kg * 2.20462
    fun lbToKg(lb: Double): Double = lb / 2.20462

    fun cmToFtIn(cm: Double): Pair<Int, Int> {
        val totalInches = cm / 2.54
        val feet = (totalInches / 12).toInt()
        val inches = (totalInches % 12).roundToInt()
        return if (inches == 12) (feet + 1) to 0 else feet to inches
    }

    fun ftInToCm(feet: Int, inches: Int): Double {
        val totalInches = (feet * 12) + inches
        return totalInches * 2.54
    }
}
