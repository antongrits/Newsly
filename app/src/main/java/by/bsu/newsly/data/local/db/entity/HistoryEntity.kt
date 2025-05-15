package by.bsu.newsly.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey val id: UUID,
    val query: String,
    val startDate: String,
    val endDate: String,
    val sortBy: String,
    val language: String,
    var date: String
)
