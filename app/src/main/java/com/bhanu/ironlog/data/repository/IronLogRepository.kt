package com.bhanu.ironlog.data.repository

import com.bhanu.ironlog.data.local.dao.PlaceholderDao
import com.bhanu.ironlog.data.local.entity.PlaceholderEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IronLogRepository @Inject constructor(
    private val placeholderDao: PlaceholderDao
) {
    fun getAllItems(): Flow<List<PlaceholderEntity>> = placeholderDao.getAll()

    suspend fun addItem(name: String) {
        placeholderDao.insert(PlaceholderEntity(name = name))
    }
}
