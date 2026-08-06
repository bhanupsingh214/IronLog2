package com.bhanu.ironlog.data.repository

import com.bhanu.ironlog.data.local.entity.LibraryExerciseEntity

sealed interface SaveExerciseResult {
    data class Success(val id: Long) : SaveExerciseResult
    data class ExactDuplicate(val existing: LibraryExerciseEntity) : SaveExerciseResult
    data class SimilarFound(val matches: List<LibraryExerciseEntity>) : SaveExerciseResult
    data class Error(val message: String) : SaveExerciseResult
}
