package by.bsu.newsly.domain.repository

import by.bsu.newsly.data.local.db.dao.HistoryDao
import by.bsu.newsly.domain.model.History
import by.bsu.newsly.domain.model.toDomain
import by.bsu.newsly.domain.model.toEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HistoryRepository(private val dao: HistoryDao) {

    suspend fun getAllHistory(): List<History> = withContext(Dispatchers.IO) {
        dao.getAllHistory().map { it.toDomain() }
    }

    suspend fun insertHistory(history: History) = withContext(Dispatchers.IO) {
        dao.insertHistory(history.toEntity())
    }

    suspend fun deleteHistory(history: History) = withContext(Dispatchers.IO) {
        dao.deleteHistory(history.toEntity())
    }

    suspend fun updateHistory(history: History) = withContext(Dispatchers.IO) {
        dao.updateHistory(history.toEntity())
    }
}