package com.bhanu.ironlog.ui.screens

import android.media.RingtoneManager
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.bhanu.ironlog.data.model.RestTimerState
import com.bhanu.ironlog.ui.components.IronLogNavigationBar
import com.bhanu.ironlog.ui.components.RestTimerOverlay
import com.bhanu.ironlog.ui.navigation.SetupNavGraph

@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val restTimer by viewModel.restTimer.collectAsState()
    val settings by viewModel.workoutSettings.collectAsState()

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(restTimer?.state) {
        if (restTimer?.state == RestTimerState.COMPLETED) {
            if (settings?.hapticFeedback == true) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            if (settings?.soundAlert == true) {
                try {
                    val notification: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    RingtoneManager.getRingtone(context, notification)?.play()
                } catch (e: Exception) {
                    // Audio feedback must never interrupt workout UI state.
                }
            }
        }
    }

    Scaffold(
        bottomBar = {
            IronLogNavigationBar(navController = navController)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .fillMaxSize()
        ) {
            SetupNavGraph(navController = navController)

            restTimer?.let { timer ->
                RestTimerOverlay(
                    timerInfo = timer,
                    onPause = { viewModel.pauseTimer() },
                    onResume = { viewModel.resumeTimer() },
                    onAdjust = { viewModel.adjustTimer(it) },
                    onSkip = { viewModel.skipTimer() },
                    onDismiss = { viewModel.dismissTimer() }
                )
            }
        }
    }
}
