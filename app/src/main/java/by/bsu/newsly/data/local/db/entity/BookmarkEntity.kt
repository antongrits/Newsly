package by.bsu.newsly.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val articleUrl: String,
    val selectedText: String,
    val startOffset: Int,
    val endOffset: Int,
    val scrollPosition: Int,
    val createdAt: Long
)