package com.bhanu.ironlog.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhanu.ironlog.data.local.pojo.WorkoutSessionWithStats
import com.bhanu.ironlog.data.repository.WorkoutSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.util.*
import java.text.SimpleDateFormat
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyRepository: com.bhanu.ironlog.data.repository.HistoryRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedProgram = MutableStateFlow<String?>("All")
    val selectedProgram = _selectedProgram.asStateFlow()

    private val _selectedDay = MutableStateFlow<String?>("All")
    val selectedDay = _selectedDay.asStateFlow()

    private val _startDate = MutableStateFlow<Long?>(null)
    val startDate = _startDate.asStateFlow()

    private val _endDate = MutableStateFlow<Long?>(null)
    val endDate = _endDate.asStateFlow()

    private val _hasPROnly = MutableStateFlow(false)
    val hasPROnly = _hasPROnly.asStateFlow()

    private val _sortOption = MutableStateFlow(HistorySort.Newest)
    val sortOption = _sortOption.asStateFlow()

    private val _history = historyRepository.getCompletedSessionsWithStats()

    // Calendar state
    private val _currentDate = MutableStateFlow(Calendar.getInstance())
    val currentDate = _currentDate.asStateFlow()

    @Suppress("UNCHECKED_CAST")
    val uiState: StateFlow<HistoryUiState> = combine(
        _history,
        _searchQuery,
        _selectedProgram,
        _selectedDay,
        _startDate,
        _endDate,
        _hasPROnly,
        _sortOption
    ) { args: Array<Any?> ->
        val history = args[0] as List<WorkoutSessionWithStats>
        val query = args[1] as String
        val program = args[2] as String
        val day = args[3] as String
        val start = args[4] as Long?
        val end = args[5] as Long?
        val hasPR = args[6] as Boolean
        val sort = args[7] as HistorySort

        val filtered = history.filter { item ->
            val matchesQuery = item.session.dayName.contains(query, ignoreCase = true) ||
                    item.session.programName.contains(query, ignoreCase = true) ||
                    item.exerciseNames.contains(query, ignoreCase = true)
            
            val matchesProgram = program == "All" || item.session.programName == program
            val matchesDay = day == "All" || item.session.dayName == day
            val matchesPR = !hasPR || item.prCount > 0
            
            val matchesDateRange = (start == null || item.session.createdAt >= start) &&
                    (end == null || item.session.createdAt <= end + 86400000) // End of day
            
            matchesQuery && matchesProgram && matchesDay && matchesPR && matchesDateRange
        }

        val sorted = when (sort) {
            HistorySort.Newest -> filtered.sortedByDescending { it.session.createdAt }
            HistorySort.Oldest -> filtered.sortedBy { it.session.createdAt }
            HistorySort.HighestVolume -> filtered.sortedByDescending { it.totalVolume }
            HistorySort.LongestDuration -> filtered.sortedByDescending { it.session.durationSeconds }
        }

        HistoryUiState.Success(sorted) as HistoryUiState
    }.catch { e ->
        emit(HistoryUiState.Error(e.message ?: "Unknown error"))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState.Loading
    )

    val workoutsByDay: StateFlow<Map<String, List<WorkoutSessionWithStats>>> = _history.map { list ->
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        list.groupBy { dateFormat.format(Date(it.session.createdAt)) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val availablePrograms: StateFlow<List<String>> = _history.map { list ->
        list.map { it.session.programName }.distinct().sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val availableDays: StateFlow<List<String>> = _history.map { list ->
        list.map { it.session.dayName }.distinct().sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onProgramFilterChange(program: String?) {
        _selectedProgram.value = program
    }

    fun onDayFilterChange(day: String?) {
        _selectedDay.value = day
    }

    fun onDateRangeChange(start: Long?, end: Long?) {
        _startDate.value = start
        _endDate.value = end
    }

    fun onHasPRToggle(hasPR: Boolean) {
        _hasPROnly.value = hasPR
    }

    fun onSortChange(sort: HistorySort) {
        _sortOption.value = sort
    }

    fun onMonthChange(increment: Int) {
        val newCal = _currentDate.value.clone() as Calendar
        newCal.add(Calendar.MONTH, increment)
        _currentDate.value = newCal
    }

    fun resetFilters() {
        _searchQuery.value = ""
        _selectedProgram.value = "All"
        _selectedDay.value = "All"
        _startDate.value = null
        _endDate.value = null
        _hasPROnly.value = false
        _sortOption.value = HistorySort.Newest
    }
}

enum class HistorySort(val label: String) {
    Newest("Newest First"),
    Oldest("Oldest First"),
    HighestVolume("Highest Volume"),
    LongestDuration("Longest Duration")
}

sealed class HistoryUiState {
    object Loading : HistoryUiState()
    data class Success(val history: List<WorkoutSessionWithStats>) : HistoryUiState()
    data class Error(val message: String) : HistoryUiState()
}
