package com.bhanu.ironlog.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1, // Single profile for the installation
    val sex: String? = null, // "Male", "Female", or null
    val dateOfBirth: Long? = null,
    val heightCm: Double? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
