package com.bhanu.ironlog.ui.screens.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhanu.ironlog.data.local.pojo.PRWithExerciseName
import com.bhanu.ironlog.data.repository.PersonalRecordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class RecordsViewModel @Inject constructor(
    private val prRepository: PersonalRecordRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedMuscleFilter = MutableStateFlow("All")
    val selectedMuscleFilter = _selectedMuscleFilter.asStateFlow()

    private val _selectedSort = MutableStateFlow(SortOption.LatestPR)
    val selectedSort = _selectedSort.asStateFlow()

    val records: StateFlow<RecordsUiState> = combine(
        prRepository.getAllPRsWithExerciseName(),
        _searchQuery,
        _selectedMuscleFilter,
        _selectedSort
    ) { allPRs, query, muscle, sort ->
        val filtered = allPRs.filter { pr ->
            val matchesQuery = pr.exercise.name.contains(query, ignoreCase = true)
            val matchesMuscle = muscle == "All" || pr.exercise.muscleGroup == muscle
            matchesQuery && matchesMuscle
        }

        val sorted = when (sort) {
            SortOption.LatestPR -> filtered.sortedByDescending { it.pr.updatedAt }
            SortOption.HighestWeight -> filtered.sortedByDescending { it.pr.weightPR }
            SortOption.HighestE1RM -> filtered.sortedByDescending { it.pr.estimated1RM }
            SortOption.Alphabetical -> filtered.sortedBy { it.exercise.name }
        }

        RecordsUiState.Success(sorted) as RecordsUiState
    }.catch { e ->
        emit(RecordsUiState.Error(e.message ?: "Unknown Error"))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RecordsUiState.Loading
    )

    val availableMuscleGroups: StateFlow<List<String>> = prRepository.getAllPRsWithExerciseName()
        .map { list ->
            list.map { it.exercise.muscleGroup }.distinct().sorted()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onMuscleFilterChange(muscle: String) {
        _selectedMuscleFilter.value = muscle
    }

    fun onSortChange(sort: SortOption) {
        _selectedSort.value = sort
    }
}

enum class SortOption(val label: String) {
    LatestPR("Latest PR"),
    HighestWeight("Highest Weight"),
    HighestE1RM("Highest Est. 1RM"),
    Alphabetical("Alphabetical")
}

sealed class RecordsUiState {
    object Loading : RecordsUiState()
    data class Success(val records: List<PRWithExerciseName>) : RecordsUiState()
    data class Error(val message: String) : RecordsUiState()
}
