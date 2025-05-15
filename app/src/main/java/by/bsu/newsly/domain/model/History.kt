package by.bsu.newsly.domain.model

import by.bsu.newsly.data.local.db.entity.HistoryEntity
import java.util.UUID

data class History(
    val id: UUID,
    val query: String,
    val startDate: String,
    val endDate: String,
    val sortBy: String,
    val language: String,
    var date: String
)

fun HistoryEntity.toDomain(): History = History(
    id = id,
    query = query,
    startDate = startDate,
    endDate = endDate,
    sortBy = sortBy,
    language = language,
    date = date
)

fun History.toEntity(): HistoryEntity = HistoryEntity(
    id = id,
    query = query,
    startDate = startDate,
    endDate = endDate,
    sortBy = sortBy,
    language = language,
    date = date
)
