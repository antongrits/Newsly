package by.bsu.newsly.domain.repository

import by.bsu.newsly.data.local.db.dao.ArticlesDao
import by.bsu.newsly.data.remote.api.ApiService
import by.bsu.newsly.domain.model.Article
import by.bsu.newsly.domain.model.toDomain
import by.bsu.newsly.domain.model.toEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ArticleRepository(
    private val dao: ArticlesDao,
    private val apiService: ApiService
) {
    suspend fun getLocalArticles(): List<Article> = withContext(Dispatchers.IO) {
        dao.getAllArticles().map { it.toDomain() }
    }

    suspend fun insertArticle(article: Article) = withContext(Dispatchers.IO) {
        dao.insertProduct(article.toEntity())
    }

    suspend fun deleteArticle(article: Article) = withContext(Dispatchers.IO) {
        dao.deleteProduct(article.toEntity())
    }

    suspend fun getFavoriteArticles(): List<Article> = withContext(Dispatchers.IO) {
        dao.getAllArticles()
            .map { it.toDomain() }
            .filter { it.isFavorite }
    }

    suspend fun getArticlesFromApi(
        query: String,
        from: String,
        to: String,
        sortBy: String,
        language: String
    ): List<Article> = withContext(Dispatchers.IO) {
        val response = apiService.getArticles(query, from, to, sortBy, language)
        if (!response.isSuccessful || response.body()?.status != "ok") {
            throw Exception("API error: ${response.code()} – ${response.message()}")
        }
        val articlesApi = response.body()!!.articles.map { it.toDomain() }
        val articlesDB = dao.getAllArticles().map { it.toDomain() }

        articlesApi.map { api ->
            val matching = articlesDB.find { db ->
                db.title == api.title &&
                        db.author == api.author &&
                        db.publishedAt == api.publishedAt &&
                        db.description == api.description &&
                        db.url == api.url &&
                        db.urlToImage == api.urlToImage &&
                        db.source == api.source
            }
            val newId = matching?.id ?: api.id
            val isFavorite = matching != null
            api.copy(id = newId, isFavorite = isFavorite)
        }
    }

    suspend fun syncArticles(apiList: List<Article>): List<Article> = withContext(Dispatchers.IO) {
        if (apiList.isEmpty()) return@withContext apiList
        val articlesDB = dao.getAllArticles().map { it.toDomain() }
        apiList.map { api ->
            val matching = articlesDB.find { db ->
                db.title == api.title &&
                        db.author == api.author &&
                        db.publishedAt == api.publishedAt &&
                        db.description == api.description &&
                        db.url == api.url &&
                        db.urlToImage == api.urlToImage &&
                        db.source == api.source
            }
            val newId = matching?.id ?: api.id
            val isFavorite = matching != null
            api.copy(id = newId, isFavorite = isFavorite)
        }
    }
}