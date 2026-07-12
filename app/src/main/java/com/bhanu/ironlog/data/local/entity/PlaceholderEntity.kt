package com.bhanu.ironlog.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "placeholder_table")
data class PlaceholderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String
)
