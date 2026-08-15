package com.bhanu.ironlog.data.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class BodyMetricsCalculatorTest {

    @Test
    fun calculateAge_correctlyCalculatesAge() {
        val dob = Calendar.getInstance().apply {
            set(2000, Calendar.JANUARY, 1)
        }.timeInMillis

        val age = BodyMetricsCalculator.calculateAge(dob)

        val today = Calendar.getInstance()
        var expectedAge = today.get(Calendar.YEAR) - 2000
        if (today.get(Calendar.MONTH) < Calendar.JANUARY || (today.get(Calendar.MONTH) == Calendar.JANUARY && today.get(Calendar.DAY_OF_MONTH) < 1)) {
            expectedAge--
        }

        assertEquals(expectedAge, age)
    }

    @Test
    fun calculateBMI_correctlyCalculatesBMI() {
        val weight = 70.0 // kg
        val height = 175.0 // cm
        val expectedBmi = weight / (1.75 * 1.75)

        assertEquals(expectedBmi, BodyMetricsCalculator.calculateBMI(weight, height), 0.001)
    }

    @Test
    fun interpretAdultBMI_returnsCorrectCategory() {
        assertEquals("Underweight", BodyMetricsCalculator.interpretAdultBMI(17.5))
        assertEquals("Normal", BodyMetricsCalculator.interpretAdultBMI(20.0))
        assertEquals("Overweight", BodyMetricsCalculator.interpretAdultBMI(24.0))
        assertEquals("Obese", BodyMetricsCalculator.interpretAdultBMI(26.0))
    }

    @Test
    fun unitConversions_areAccurate() {
        // cm -> ft in
        val ftIn = BodyMetricsCalculator.cmToFtIn(180.0)
        assertEquals(5, ftIn.first)
        assertEquals(11, ftIn.second)

        // ft in -> cm
        val cm = BodyMetricsCalculator.ftInToCm(5, 11)
        assertEquals(180.34, cm, 0.01)

        // kg -> lb
        assertEquals(22.046, BodyMetricsCalculator.kgToLb(10.0), 0.001)

        // lb -> kg
        assertEquals(10.0, BodyMetricsCalculator.lbToKg(22.0462), 0.001)
    }
}
