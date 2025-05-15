package by.bsu.newsly.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import by.bsu.newsly.data.local.db.entity.HistoryEntity

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history")
    suspend fun getAllHistory(): List<HistoryEntity>

    @Insert
    suspend fun insertHistory(historyEntity: HistoryEntity)

    @Delete
    suspend fun deleteHistory(historyEntity: HistoryEntity)

    @Update
    suspend fun updateHistory(historyEntity: HistoryEntity)
}