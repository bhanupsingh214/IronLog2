package com.bhanu.ironlog.ui.screens.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhanu.ironlog.data.local.entity.ExerciseEntity
import com.bhanu.ironlog.data.local.pojo.DailyVolume
import com.bhanu.ironlog.data.local.pojo.ExerciseStrengthHistory
import com.bhanu.ironlog.data.local.pojo.PRWithExerciseName
import com.bhanu.ironlog.data.repository.AnalyticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val analyticsRepository: AnalyticsRepository
) : ViewModel() {

    // --- Chart Controls ---
    private val _selectedExerciseId = MutableStateFlow<Long?>(null)
    val selectedExerciseId = _selectedExerciseId.asStateFlow()

    private val _isE1RMToggle = MutableStateFlow(false)
    val isE1RMToggle = _isE1RMToggle.asStateFlow()

    private val _volumeTimeFilter = MutableStateFlow(TimeFilter.Last30Days)
    val volumeTimeFilter = _volumeTimeFilter.asStateFlow()

    // --- Data Streams ---
    val allExercises: StateFlow<List<ExerciseEntity>> = analyticsRepository.getAllExercises()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val strengthHistory: StateFlow<List<ExerciseStrengthHistory>> = combine(
        _selectedExerciseId,
        _isE1RMToggle
    ) { id, isE1RM -> id to isE1RM }.flatMapLatest { (id, isE1RM) ->
        if (id == null) flowOf(emptyList())
        else analyticsRepository.getExerciseStrengthHistory(id).map { history ->
            val prPoints = mutableListOf<ExerciseStrengthHistory>()
            var currentMax = 0.0
            
            history.forEach { point ->
                val value = if (isE1RM) point.maxE1RM else point.maxWeight
                if (value > currentMax) {
                    currentMax = value
                    prPoints.add(point)
                }
            }
            prPoints
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val volumeHistory: StateFlow<List<DailyVolume>> = _volumeTimeFilter.flatMapLatest { filter ->
        analyticsRepository.getDailyVolumeHistory(filter.getSinceTimestamp())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uiState: StateFlow<ProgressUiState> = combine(
        analyticsRepository.getTotalWorkoutsCount(),
        analyticsRepository.getTotalVolume(),
        analyticsRepository.getWeightPRCount(),
        analyticsRepository.getEstimated1RMPRCount(),
        analyticsRepository.getWeeklyVolume(),
        analyticsRepository.getMonthlyVolume(),
        analyticsRepository.getLatestPRs()
    ) { args: Array<Any> ->
        ProgressUiState.Success(
            totalWorkouts = args[0] as Int,
            totalVolume = args[1] as Double,
            weightPRCount = args[2] as Int,
            e1rmPRCount = args[3] as Int,
            weeklyVolume = args[4] as Double,
            monthlyVolume = args[5] as Double,
            latestPRs = args[6] as List<PRWithExerciseName>
        ) as ProgressUiState
    }.catch { e ->
        emit(ProgressUiState.Error(e.message ?: "Unknown Error"))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProgressUiState.Loading
    )

    fun onExerciseSelected(exerciseId: Long) {
        _selectedExerciseId.value = exerciseId
    }

    fun toggleE1RM(enabled: Boolean) {
        _isE1RMToggle.value = enabled
    }

    fun onVolumeFilterSelected(filter: TimeFilter) {
        _volumeTimeFilter.value = filter
    }

    init {
        // Initialize with first exercise if available
        viewModelScope.launch {
            allExercises.filter { it.isNotEmpty() }.first().let { list ->
                if (_selectedExerciseId.value == null) {
                    _selectedExerciseId.value = list.first().id
                }
            }
        }
    }
}

enum class TimeFilter(val label: String) {
    Last7Days("7 Days"),
    Last30Days("30 Days"),
    Last3Months("3 Months"),
    Last6Months("6 Months"),
    Last1Year("1 Year"),
    AllTime("All Time");

    fun getSinceTimestamp(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        when (this) {
            Last7Days -> calendar.add(Calendar.DAY_OF_YEAR, -7)
            Last30Days -> calendar.add(Calendar.DAY_OF_YEAR, -30)
            Last3Months -> calendar.add(Calendar.MONTH, -3)
            Last6Months -> calendar.add(Calendar.MONTH, -6)
            Last1Year -> calendar.add(Calendar.YEAR, -1)
            AllTime -> return 0L
        }
        return calendar.timeInMillis
    }
}

sealed class ProgressUiState {
    object Loading : ProgressUiState()
    data class Success(
        val totalWorkouts: Int,
        val totalVolume: Double,
        val weightPRCount: Int,
        val e1rmPRCount: Int,
        val weeklyVolume: Double,
        val monthlyVolume: Double,
        val latestPRs: List<PRWithExerciseName>
    ) : ProgressUiState()
    data class Error(val message: String) : ProgressUiState()
}
