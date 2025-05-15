package by.bsu.newsly.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import by.bsu.newsly.data.local.db.dao.ArticlesDao
import by.bsu.newsly.data.local.db.dao.HistoryDao
import by.bsu.newsly.data.local.db.dao.BookmarkDao
import by.bsu.newsly.data.local.db.entity.ArticleEntity
import by.bsu.newsly.data.local.db.entity.HistoryEntity
import by.bsu.newsly.data.local.db.entity.BookmarkEntity

@Database(
    entities = [ArticleEntity::class, HistoryEntity::class, BookmarkEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ArticlesDatabase : RoomDatabase() {
    abstract fun articlesDao(): ArticlesDao
    abstract fun historyDao(): HistoryDao
    abstract fun bookmarkDao(): BookmarkDao
}