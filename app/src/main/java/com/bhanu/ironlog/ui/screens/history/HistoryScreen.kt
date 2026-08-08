package com.bhanu.ironlog.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.bhanu.ironlog.data.local.pojo.WorkoutSessionWithStats
import com.bhanu.ironlog.ui.components.SearchBar
import com.bhanu.ironlog.ui.components.formatTimer
import com.bhanu.ironlog.ui.components.formatWorkoutDate
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    onNavigateToDetails: (Long) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedProgram by viewModel.selectedProgram.collectAsState()
    val hasPROnly by viewModel.hasPROnly.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    val availablePrograms by viewModel.availablePrograms.collectAsState()
    val availableDays by viewModel.availableDays.collectAsState()
    val selectedDay by viewModel.selectedDay.collectAsState()
    val startDate by viewModel.startDate.collectAsState()
    val endDate by viewModel.endDate.collectAsState()
    
    val workoutsByDay by viewModel.workoutsByDay.collectAsState()
    val currentDate by viewModel.currentDate.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("History", "Calendar")

    var showFilterSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column {
                LargeTopAppBar(
                    title = { Text("Workout History", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showFilterSheet = true }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter")
                        }
                    }
                )
                TabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) },
                            icon = {
                                Icon(
                                    imageVector = if (index == 0) Icons.AutoMirrored.Filled.List else Icons.Default.CalendarMonth,
                                    contentDescription = null
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is HistoryUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is HistoryUiState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { /* ViewModel could expose retry */ }) {
                            Text("Retry")
                        }
                    }
                }
                is HistoryUiState.Success -> {
                    if (selectedTab == 0) {
                        HistoryListContent(
                            history = state.history,
                            searchQuery = searchQuery,
                            onSearchChange = { viewModel.onSearchQueryChange(it) },
                            onItemClick = { onNavigateToDetails(it.session.sessionId) }
                        )
                    } else {
                        CalendarContent(
                            currentDate = currentDate,
                            workoutsByDay = workoutsByDay,
                            onMonthChange = { viewModel.onMonthChange(it) },
                            onWorkoutClick = { onNavigateToDetails(it.session.sessionId) }
                        )
                    }
                }
            }
        }
    }

    if (showFilterSheet) {
        HistoryFilterSheet(
            selectedProgram = selectedProgram ?: "All",
            availablePrograms = availablePrograms,
            selectedDay = selectedDay ?: "All",
            availableDays = availableDays,
            startDate = startDate,
            endDate = endDate,
            hasPROnly = hasPROnly,
            sortOption = sortOption,
            onProgramChange = { viewModel.onProgramFilterChange(it) },
            onDayChange = { viewModel.onDayFilterChange(it) },
            onDateRangeChange = { start, end -> viewModel.onDateRangeChange(start, end) },
            onHasPRToggle = { viewModel.onHasPRToggle(it) },
            onSortChange = { viewModel.onSortChange(it) },
            onResetFilters = { viewModel.resetFilters() },
            onDismiss = { showFilterSheet = false }
        )
    }
}

@Composable
fun HistoryListContent(
    history: List<WorkoutSessionWithStats>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onItemClick: (WorkoutSessionWithStats) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        SearchBar(
            query = searchQuery,
            onQueryChange = onSearchChange,
            placeholder = "Search workouts...",
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
        
        if (history.isEmpty()) {
            EmptyHistoryState(Modifier.weight(1f))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(history, key = { it.session.sessionId }) { item ->
                    HistoryItemWithStats(
                        item = item,
                        onClick = { onItemClick(item) }
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun HistoryItemWithStats(
    item: WorkoutSessionWithStats,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.session.dayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = item.session.programName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                if (item.prCount > 0) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 4.dp)) {
                            Icon(Icons.Default.EmojiEvents, null, modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("${item.prCount} PRs", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Volume", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text(String.format(Locale.getDefault(), "%,.0f kg", item.totalVolume), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Column(Modifier.weight(1f)) {
                    Text("Exercises", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text("${item.exerciseCount} (${item.setCount} sets)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Column(Modifier.weight(1f)) {
                    Text("Duration", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text(formatTimer(item.session.durationSeconds), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(Modifier.height(8.dp))
            Text(
                text = formatWorkoutDate(item.session.createdAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarContent(
    currentDate: Calendar,
    workoutsByDay: Map<String, List<WorkoutSessionWithStats>>,
    onMonthChange: (Int) -> Unit,
    onWorkoutClick: (WorkoutSessionWithStats) -> Unit
) {
    var selectedDayWorkouts by remember { mutableStateOf<List<WorkoutSessionWithStats>?>(null) }
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Month Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onMonthChange(-1) }) {
                Icon(Icons.Default.ChevronLeft, "Previous Month")
            }
            Text(
                text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(currentDate.time),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { onMonthChange(1) }) {
                Icon(Icons.Default.ChevronRight, "Next Month")
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Days of Week Header
        Row(modifier = Modifier.fillMaxWidth()) {
            val days = listOf("S", "M", "T", "W", "T", "F", "S")
            days.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
        
        Spacer(Modifier.height(8.dp))
        
        // Calendar Grid
        val calendar = currentDate.clone() as Calendar
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        val rows = (daysInMonth + firstDayOfWeek - 1 + 6) / 7
        val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth().height(60.dp)) {
                for (col in 1..7) {
                    val dayNum = row * 7 + col - firstDayOfWeek + 1
                    if (dayNum in 1..daysInMonth) {
                        calendar.set(Calendar.DAY_OF_MONTH, dayNum)
                        val dateStr = dayFormat.format(calendar.time)
                        val sessions = workoutsByDay[dateStr] ?: emptyList()
                        
                        CalendarDay(
                            day = dayNum,
                            sessions = sessions,
                            onClick = { 
                                if (sessions.isNotEmpty()) {
                                    if (sessions.size == 1) {
                                        onWorkoutClick(sessions[0])
                                    } else {
                                        selectedDayWorkouts = sessions
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }

    selectedDayWorkouts?.let { sessions ->
        ModalBottomSheet(onDismissRequest = { selectedDayWorkouts = null }) {
            Column(Modifier.padding(16.dp).navigationBarsPadding()) {
                Text("Select Workout", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                sessions.forEach { session ->
                    HistoryItemWithStats(item = session, onClick = { 
                        onWorkoutClick(session)
                        selectedDayWorkouts = null
                    })
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun CalendarDay(
    day: Int,
    sessions: List<WorkoutSessionWithStats>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = if (sessions.isNotEmpty()) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = day.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (sessions.isNotEmpty()) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
            if (sessions.isNotEmpty()) {
                Row(modifier = Modifier.padding(top = 2.dp)) {
                    sessions.take(3).forEach { session ->
                        val color = getWorkoutTypeColor(session.session.dayName)
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 1.dp)
                                .size(4.dp)
                                .background(color, MaterialTheme.shapes.extraSmall)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryFilterSheet(
    selectedProgram: String,
    availablePrograms: List<String>,
    selectedDay: String,
    availableDays: List<String>,
    startDate: Long?,
    endDate: Long?,
    hasPROnly: Boolean,
    sortOption: HistorySort,
    onProgramChange: (String?) -> Unit,
    onDayChange: (String?) -> Unit,
    onDateRangeChange: (Long?, Long?) -> Unit,
    onHasPRToggle: (Boolean) -> Unit,
    onSortChange: (HistorySort) -> Unit,
    onResetFilters: () -> Unit,
    onDismiss: () -> Unit
) {
    val dateRangePickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = startDate,
        initialSelectedEndDateMillis = endDate
    )
    var showDateRangePicker by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp).navigationBarsPadding().verticalScroll(rememberScrollState())) {
            Text("Filters & Sorting", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(24.dp))
            
            Text("Program", style = MaterialTheme.typography.labelLarge)
            Row(modifier = Modifier.padding(vertical = 8.dp).horizontalScroll(rememberScrollState())) {
                FilterChip(
                    selected = selectedProgram == "All",
                    onClick = { onProgramChange("All") },
                    label = { Text("All") }
                )
                availablePrograms.forEach { program ->
                    Spacer(Modifier.width(8.dp))
                    FilterChip(
                        selected = selectedProgram == program,
                        onClick = { onProgramChange(program) },
                        label = { Text(program) }
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))

            Text("Workout Day", style = MaterialTheme.typography.labelLarge)
            Row(modifier = Modifier.padding(vertical = 8.dp).horizontalScroll(rememberScrollState())) {
                FilterChip(
                    selected = selectedDay == "All",
                    onClick = { onDayChange("All") },
                    label = { Text("All") }
                )
                availableDays.forEach { day ->
                    Spacer(Modifier.width(8.dp))
                    FilterChip(
                        selected = selectedDay == day,
                        onClick = { onDayChange(day) },
                        label = { Text(day) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text("Date Range", style = MaterialTheme.typography.labelLarge)
            val dateText = if (startDate != null && endDate != null) {
                "${formatWorkoutDate(startDate)} - ${formatWorkoutDate(endDate)}"
            } else if (startDate != null) {
                "From ${formatWorkoutDate(startDate)}"
            } else {
                "All Time"
            }

            OutlinedButton(
                onClick = { showDateRangePicker = true },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Icon(Icons.Default.DateRange, null)
                Spacer(Modifier.width(8.dp))
                Text(dateText)
            }

            Spacer(Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = hasPROnly, onCheckedChange = onHasPRToggle)
                Text("Show only workouts with PRs", style = MaterialTheme.typography.bodyLarge)
            }
            
            Spacer(Modifier.height(16.dp))
            
            Text("Sort By", style = MaterialTheme.typography.labelLarge)
            HistorySort.entries.forEach { sort ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSortChange(sort) }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = sortOption == sort, onClick = { onSortChange(sort) })
                    Spacer(Modifier.width(8.dp))
                    Text(sort.label)
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            OutlinedButton(
                onClick = onResetFilters,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Reset Filters")
            }
            
            Spacer(Modifier.height(8.dp))

            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Apply")
            }
        }
    }

    if (showDateRangePicker) {
        DatePickerDialog(
            onDismissRequest = { showDateRangePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onDateRangeChange(
                        dateRangePickerState.selectedStartDateMillis,
                        dateRangePickerState.selectedEndDateMillis
                    )
                    showDateRangePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDateRangePicker = false }) { Text("Cancel") }
            }
        ) {
            DateRangePicker(
                state = dateRangePickerState, 
                modifier = Modifier.weight(1f),
                dateFormatter = remember {
                    DatePickerDefaults.dateFormatter(
                        selectedDateSkeleton = "ddMMyyyy"
                    )
                }
            )
        }
    }
}

private fun getWorkoutTypeColor(name: String): Color {
    val lower = name.lowercase()
    return when {
        lower.contains("push") -> Color(0xFFF44336)
        lower.contains("pull") -> Color(0xFF2196F3)
        lower.contains("legs") -> Color(0xFF4CAF50)
        lower.contains("upper") -> Color(0xFFFF9800)
        lower.contains("lower") -> Color(0xFF9C27B0)
        else -> Color(0xFF9E9E9E)
    }
}

@Composable
fun EmptyHistoryState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "No workout history yet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Complete your first workout to see it here",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center
            )
        }
    }
}
