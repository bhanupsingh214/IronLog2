package com.bhanu.ironlog.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "body_weight_history")
data class BodyWeightEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val weightKg: Double,
    val timestamp: Long,
    val notes: String = ""
)
