package com.bhanu.ironlog.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_settings")
data class WorkoutSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val defaultRestTimerSeconds: Int = 90,
    val autoStartTimer: Boolean = true,
    val hapticFeedback: Boolean = true,
    val soundAlert: Boolean = true
)
