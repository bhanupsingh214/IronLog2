package com.bhanu.ironlog.data.local.pojo

enum class PersonalRecordType(val label: String) {
    BEST_WEIGHT("Best Weight"),
    BEST_E1RM("Best Estimated 1RM"),
    HIGHEST_VOLUME("Highest Volume")
}

data class PersonalRecordAchievement(
    val exerciseName: String,
    val type: PersonalRecordType,
    val previousValue: Double,
    val newValue: Double,
    val unit: String = "kg"
)
