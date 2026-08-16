package com.bhanu.ironlog.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Trophy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.bhanu.ironlog.data.local.entity.WorkoutSession
import com.bhanu.ironlog.data.local.pojo.ProgramWithStats
import com.bhanu.ironlog.data.local.pojo.WorkoutDayWithStats
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val IronPurple = Color(0xFF6C3DFF)
private val IronPurpleSoft = Color(0xFF9A72FF)
private val IronSurface = Color(0xFF111827)
private val IronSurface2 = Color(0xFF172033)

@Composable
fun DashboardPhase6Screen(
    onNavigateToWorkout: () -> Unit,
    onNavigateToCurrentProgram: (Long) -> Unit,
    onNavigateToRecentSession: (Long, Long) -> Unit,
    onNavigateToRecords: () -> Unit,
    onNavigateToHistory: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val activeProgram by viewModel.activeProgram.collectAsState()
    val todayWorkout by viewModel.todayWorkout.collectAsState()
    val recentHistory by viewModel.recentHistory.collectAsState()
    val weeklyVolume by viewModel.weeklyVolume.collectAsState()
    val dailyVolume by viewModel.dailyVolumeHistory.collectAsState()
    val personalRecords by viewModel.personalRecords.collectAsState()
    val activeSession by viewModel.activeSession.collectAsState()
    val currentExerciseName by viewModel.currentExerciseName.collectAsState()
    var showAddLog by remember { mutableStateOf(false) }
    val days by viewModel.activeProgramDays.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { DashboardHeaderPhase6() }
        item { PrimaryActionsPhase6(onNavigateToWorkout, { showAddLog = true }, activeProgram != null) }
        item {
            if (activeSession != null) {
                ActiveWorkoutPhase6(activeSession!!, currentExerciseName, onNavigateToWorkout)
            } else {
                TodayWorkoutPhase6(todayWorkout, onNavigateToWorkout)
            }
        }
        item { ProgramPhase6(activeProgram, onNavigateToCurrentProgram) }
        item { WeeklyVolumePhase6(weeklyVolume, dailyVolume) }
        item {
            QuickStatsPhase6(
                workouts = recentHistory.size,
                volume = weeklyVolume,
                prs = personalRecords.size,
                averageDuration = recentHistory.map { it.durationSeconds }.takeIf { it.isNotEmpty() }?.average()?.div(60.0) ?: 0.0
            )
        }
        item { RecentHistoryPhase6(recentHistory, onNavigateToRecentSession, onNavigateToHistory) }
        item { PersonalRecordsPhase6(personalRecords, onNavigateToRecords) }
    }

    if (showAddLog) {
        AddPreviousLogPhase6(activeProgram, days, { showAddLog = false }) { dayId ->
            viewModel.onAddHistoricalSession(dayId, System.currentTimeMillis())
            showAddLog = false
        }
    }
}

@Composable
private fun DashboardHeaderPhase6() {
    val date = remember { SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault()).format(Date()) }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("Good morning,", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Champ! 💪", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = {}) { Icon(Icons.Default.NotificationsNone, "Notifications") }
    }
}

@Composable
private fun PrimaryActionsPhase6(onStart: () -> Unit, onAddLog: () -> Unit, enabled: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        Button(onClick = onStart, enabled = enabled, modifier = Modifier.weight(1.2f).height(64.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = IronPurple)) {
            Icon(Icons.Default.PlayArrow, null); Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.Start) { Text("Start Today's Workout", fontWeight = FontWeight.Bold); Text("Let's get stronger", style = MaterialTheme.typography.labelSmall) }
            Spacer(Modifier.weight(1f)); Icon(Icons.Default.ChevronRight, null)
        }
        OutlinedButton(onClick = onAddLog, enabled = enabled, modifier = Modifier.weight(1f).height(64.dp), shape = RoundedCornerShape(16.dp)) {
            Icon(Icons.Default.Add, null); Spacer(Modifier.width(6.dp)); Text("Add Previous Log")
        }
    }
}

@Composable
private fun Phase6Card(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = IronSurface)) {
        Column(Modifier.padding(14.dp), content = content)
    }
}

@Composable
private fun SectionTitlePhase6(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = IronPurpleSoft, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        if (action != null && onAction != null) TextButton(onClick = onAction) { Text(action) }
    }
}

@Composable
private fun TodayWorkoutPhase6(today: WorkoutDayWithStats?, onStart: () -> Unit) {
    Phase6Card {
        SectionTitlePhase6("TODAY", Icons.Default.CalendarMonth); Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(today?.day?.name ?: "No workout scheduled", fontWeight = FontWeight.Bold)
                Text(if (today != null) "${today.exerciseCount} exercises • ~${today.day.estimatedDurationMinutes} min" else "You're free to train or add a log", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (today != null) IconButton(onClick = onStart) { Icon(Icons.Default.ChevronRight, null) }
        }
    }
}

@Composable
private fun ProgramPhase6(program: ProgramWithStats?, onClick: (Long) -> Unit) {
    Phase6Card(modifier = Modifier.clickable(enabled = program != null) { program?.let { onClick(it.program.id) } }) {
        SectionTitlePhase6("PROGRAM", Icons.AutoMirrored.Filled.EventNote); Spacer(Modifier.height(8.dp))
        Text(program?.program?.name ?: "No active program", fontWeight = FontWeight.Bold)
        if (program != null) Text("${program.dayCount} days • ${program.exerciseCount} exercises", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun WeeklyVolumePhase6(total: Double, daily: List<com.bhanu.ironlog.data.local.pojo.DailyVolume>) {
    Phase6Card {
        SectionTitlePhase6("WEEKLY VOLUME", Icons.Default.BarChart); Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.fillMaxWidth().height(78.dp)) {
            val values = daily.takeLast(7).map { it.volume }; val max = (values.maxOrNull() ?: 0.0).coerceAtLeast(1.0)
            repeat(7) { index ->
                val value = values.getOrNull(index) ?: 0.0
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                    Box(Modifier.fillMaxWidth().height((54f * (value / max).toFloat()).coerceAtLeast(if (value > 0) 8f else 3f).dp).background(IronPurple, RoundedCornerShape(5.dp)))
                }
            }
        }
        Spacer(Modifier.height(8.dp)); Text(String.format(Locale.getDefault(), "%,.0f kg", total), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Total volume logged this week", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun QuickStatsPhase6(workouts: Int, volume: Double, prs: Int, averageDuration: Double) {
    Column { SectionTitlePhase6("QUICK STATS", Icons.Default.Timeline); Spacer(Modifier.height(8.dp)); Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        StatTilePhase6("Workouts", workouts.toString(), "recent", Modifier.weight(1f)); StatTilePhase6("Volume", String.format(Locale.getDefault(), "%.1fk", volume / 1000.0), "this week", Modifier.weight(1f)); StatTilePhase6("PRs", prs.toString(), "tracked", Modifier.weight(1f)); StatTilePhase6("Avg. Duration", "${averageDuration.toInt()}m", "recent", Modifier.weight(1f))
    } }
}

@Composable
private fun StatTilePhase6(title: String, value: String, caption: String, modifier: Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = IronSurface2)) { Column(Modifier.padding(10.dp)) { Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.height(4.dp)); Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); Text(caption, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
}

@Composable
private fun RecentHistoryPhase6(history: List<WorkoutSession>, onSession: (Long, Long) -> Unit, onAll: () -> Unit) {
    Phase6Card { SectionTitlePhase6("RECENT HISTORY", Icons.Default.History, "See all", onAll); Spacer(Modifier.height(4.dp));
        if (history.isEmpty()) Text("No workouts logged yet", color = MaterialTheme.colorScheme.onSurfaceVariant) else history.take(5).forEach { session ->
            Row(Modifier.fillMaxWidth().clickable { onSession(session.workoutDayId, session.sessionId) }.padding(vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.FitnessCenter, null, tint = IronPurpleSoft, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)) { Text(session.dayName, fontWeight = FontWeight.SemiBold); Text(SimpleDateFormat("EEE, dd MMM", Locale.getDefault()).format(Date(session.createdAt)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }; Text("${session.durationSeconds / 60}m", style = MaterialTheme.typography.labelSmall); Spacer(Modifier.width(6.dp)); Icon(Icons.Default.ChevronRight, null, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun PersonalRecordsPhase6(records: List<Pair<String, Double>>, onAll: () -> Unit) {
    Phase6Card { SectionTitlePhase6("PERSONAL RECORDS", Icons.Default.EmojiEvents, "See all", onAll); Spacer(Modifier.height(6.dp));
        if (records.isEmpty()) Text("No personal records yet", color = MaterialTheme.colorScheme.onSurfaceVariant) else records.take(3).forEach { (name, weight) ->
            Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Trophy, null, tint = IronPurpleSoft, modifier = Modifier.size(22.dp)); Spacer(Modifier.width(10.dp)); Text(name, Modifier.weight(1f), fontWeight = FontWeight.SemiBold); Text(String.format(Locale.getDefault(), "%.1f kg", weight), fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun ActiveWorkoutPhase6(session: com.bhanu.ironlog.data.model.workout.WorkoutSessionAggregate, currentExerciseName: String?, onResume: () -> Unit) {
    Phase6Card { SectionTitlePhase6("WORKOUT IN PROGRESS", Icons.Default.Refresh); Spacer(Modifier.height(8.dp)); Text(session.metadata.dayName, fontWeight = FontWeight.Bold); Text(currentExerciseName ?: "Continue your workout", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.height(10.dp)); Button(onClick = onResume, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = IronPurple)) { Icon(Icons.Default.PlayArrow, null); Spacer(Modifier.width(8.dp)); Text("Resume Workout") } }
}

@Composable
private fun AddPreviousLogPhase6(activeProgram: ProgramWithStats?, days: List<WorkoutDayWithStats>, onDismiss: () -> Unit, onSelectDay: (Long) -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Add Previous Log") }, text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("Choose the workout day to log."); if (activeProgram == null || days.isEmpty()) Text("No program days available.", color = MaterialTheme.colorScheme.onSurfaceVariant) else days.forEach { day -> OutlinedButton(onClick = { onSelectDay(day.day.id) }, modifier = Modifier.fillMaxWidth()) { Text(day.day.name) } } } }, confirmButton = {}, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}
