package by.bsu.newsly.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import by.bsu.newsly.data.local.db.entity.BookmarkEntity

@Dao
interface BookmarkDao {

    @Transaction
    @Query("SELECT * FROM bookmarks WHERE articleUrl = :url ORDER BY createdAt DESC")
    suspend fun getAllForUrl(url: String): List<BookmarkEntity>

    @Insert
    suspend fun insertBookmark(bookmark: BookmarkEntity): Long

    @Delete
    suspend fun deleteBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE articleUrl = :url")
    suspend fun deleteBookmarksByUrl(url: String)
}