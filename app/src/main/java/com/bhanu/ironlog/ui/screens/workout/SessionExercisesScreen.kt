package com.bhanu.ironlog.ui.screens.workout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.bhanu.ironlog.data.local.entity.ExerciseEntity
import com.bhanu.ironlog.data.local.pojo.SessionExerciseWithTemplate
import com.bhanu.ironlog.ui.components.ErrorScreen
import com.bhanu.ironlog.ui.components.ExerciseSessionItem
import com.bhanu.ironlog.ui.components.WorkoutProgress
import com.bhanu.ironlog.ui.components.formatTimer
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionExercisesScreen(
    onBack: () -> Unit,
    onNavigateToLogging: (Long, Long) -> Unit,
    viewModel: SessionExercisesViewModel = hiltViewModel()
) {
    if (!viewModel.isArgumentValid) {
        ErrorScreen(onBack = onBack, message = "Invalid Session data")
        return
    }

    val session by viewModel.session.collectAsState()
    val exercises by viewModel.exercises.collectAsState()
    val timerSeconds by viewModel.timerSeconds.collectAsState()

    // We can use a derived state to track if we've attempted to load and failed
    var loadingTimedOut by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(2000) // Reduced to 2s for better UX
        if (session == null) loadingTimedOut = true
    }

    if (loadingTimedOut && session == null) {
        ErrorScreen(onBack = onBack, message = "Workout Session not found")
        return
    }

    val completedIds = remember(session) {
        session?.completedExerciseIds?.split(",")?.filter { it.isNotBlank() }?.toSet() ?: emptySet()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(session?.dayName ?: "Workout", style = MaterialTheme.typography.titleMedium)
                        Text(session?.programName ?: "", style = MaterialTheme.typography.bodySmall)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Text(
                        text = formatTimer(timerSeconds),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Button(
                    onClick = { 
                        viewModel.finishWorkout()
                        onBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .navigationBarsPadding()
                ) {
                    Text("Finish Workout")
                }
            }
        }
    ) { padding ->
        when {
            session == null && !loadingTimedOut -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            session == null && loadingTimedOut -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Session not found", color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = onBack) { Text("Go Back") }
                    }
                }
            }
            exercises.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.FitnessCenter, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                        Spacer(Modifier.height(16.dp))
                        Text("No exercises in this session", color = MaterialTheme.colorScheme.outline)
                        Text("Add exercises to your program template first", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        WorkoutProgress(
                            completed = completedIds.size,
                            total = exercises.size
                        )
                    }

                    items(exercises, key = { it.sessionExercise.sessionExerciseId }) { item ->
                    val isCompleted = completedIds.contains(item.template.id.toString())
                    ExerciseSessionItem(
                        exercise = item.template,
                        isCompleted = isCompleted,
                        onToggleComplete = { viewModel.toggleExerciseCompletion(item.template.id) },
                        onClick = { onNavigateToLogging(item.template.id, session?.sessionId ?: 0L) }
                    )
                }
                }
            }
        }
    }
}
