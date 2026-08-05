package com.bhanu.ironlog.data.local.pojo

import com.bhanu.ironlog.data.model.RestTimerState

data class RestTimerInfo(
    val state: RestTimerState = RestTimerState.IDLE,
    val remainingSeconds: Int = 0,
    val totalDurationSeconds: Int = 0,
    val elapsedGraceSeconds: Int = 0
)
