package com.bhanu.ironlog.ui.screens.programs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhanu.ironlog.data.local.entity.ProgramEntity
import com.bhanu.ironlog.data.local.pojo.ProgramWithStats
import com.bhanu.ironlog.data.repository.ProgramRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ProgramSortOrder {
    NAME_ASC,
    NAME_DESC,
    MODIFIED_DESC,
    MODIFIED_ASC,
    ACTIVE_FIRST
}

@HiltViewModel
class ProgramsViewModel @Inject constructor(
    private val repository: ProgramRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _sortOrder = MutableStateFlow(ProgramSortOrder.ACTIVE_FIRST)
    val sortOrder = _sortOrder.asStateFlow()

    val programs: StateFlow<List<ProgramWithStats>> = combine(
        repository.getAllProgramsWithStats(),
        _searchQuery,
        _sortOrder
    ) { programs, query, sort ->
        programs.filter { 
            it.program.name.contains(query, ignoreCase = true) 
        }.sortedWith(getComparator(sort))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val archivedPrograms: StateFlow<List<ProgramWithStats>> = repository.getArchivedProgramsWithStats()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onSortOrderChange(order: ProgramSortOrder) {
        _sortOrder.value = order
    }

    private fun getComparator(sort: ProgramSortOrder): Comparator<ProgramWithStats> {
        return when (sort) {
            ProgramSortOrder.NAME_ASC -> compareBy { it.program.name.lowercase() }
            ProgramSortOrder.NAME_DESC -> compareByDescending { it.program.name.lowercase() }
            ProgramSortOrder.MODIFIED_DESC -> compareByDescending { it.program.lastModifiedAt }
            ProgramSortOrder.MODIFIED_ASC -> compareBy { it.program.lastModifiedAt }
            ProgramSortOrder.ACTIVE_FIRST -> compareByDescending<ProgramWithStats> { it.program.isActive }.thenByDescending { it.program.lastModifiedAt }
        }
    }

    fun addProgram(name: String) {
        viewModelScope.launch {
            repository.insertProgram(ProgramEntity(name = name))
        }
    }

    fun activateProgram(id: Long) {
        viewModelScope.launch {
            repository.activateProgram(id)
        }
    }

    fun archiveProgram(program: ProgramEntity) {
        viewModelScope.launch {
            repository.archiveProgram(program)
        }
    }

    fun restoreProgram(program: ProgramEntity) {
        viewModelScope.launch {
            repository.restoreProgram(program)
        }
    }

    fun deleteProgram(program: ProgramEntity) {
        viewModelScope.launch {
            repository.deleteProgram(program)
        }
    }

    fun duplicateProgram(id: Long) {
        viewModelScope.launch {
            repository.duplicateProgram(id)
        }
    }

    fun renameProgram(program: ProgramEntity, newName: String) {
        viewModelScope.launch {
            repository.updateProgram(program.copy(name = newName))
        }
    }
}
