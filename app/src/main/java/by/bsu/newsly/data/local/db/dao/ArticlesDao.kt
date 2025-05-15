package by.bsu.newsly.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import by.bsu.newsly.data.local.db.entity.ArticleEntity

@Dao
interface ArticlesDao {
    @Query("SELECT * FROM articles")
    suspend fun getAllArticles(): List<ArticleEntity>

    @Insert
    suspend fun insertProduct(article: ArticleEntity)

    @Delete
    suspend fun deleteProduct(article: ArticleEntity)
}