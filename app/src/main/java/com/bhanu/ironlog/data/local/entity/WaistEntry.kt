package com.bhanu.ironlog.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "waist_history")
data class WaistEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val circumferenceCm: Double,
    val timestamp: Long,
    val notes: String = ""
)
