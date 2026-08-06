package com.bhanu.ironlog.util

object ExerciseNormalizationUtil {
    /**
     * Normalizes an exercise name for duplicate detection.
     * 1. To Lower Case
     * 2. Remove all non-alphanumeric characters
     * 3. Trim
     *
     * Example: " Bench   Press " -> "benchpress"
     * Example: "Barbell Bench Press (Standard)" -> "barbellbenchpressstandard"
     */
    fun normalize(name: String): String {
        return name.lowercase()
            .replace(Regex("[^a-z0-9]"), "")
            .trim()
    }
}
