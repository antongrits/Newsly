package by.bsu.newsly.domain.model

import by.bsu.newsly.data.local.db.entity.ArticleEntity
import by.bsu.newsly.data.remote.model.ArticleNetwork
import java.util.*

data class Article(
    val id: UUID,
    val source: String,
    val author: String?,
    val title: String,
    val description: String?,
    val url: String,
    val urlToImage: String?,
    val publishedAt: String,
    var isFavorite: Boolean = false
)

fun ArticleNetwork.toDomain(): Article {
    return Article(
        id = UUID.randomUUID(),
        source = source.name,
        author = author,
        title = title,
        description = description,
        url = url,
        urlToImage = urlToImage,
        publishedAt = publishedAt
    )
}

fun ArticleEntity.toDomain(): Article {
    return Article(
        id = UUID.fromString(id),
        source = source,
        author = author,
        title = title,
        description = description,
        url = url,
        urlToImage = urlToImage,
        publishedAt = publishedAt,
        isFavorite = isFavorite
    )
}

fun Article.toEntity(): ArticleEntity {
    return ArticleEntity(
        id = id.toString(),
        source = source,
        author = author,
        title = title,
        description = description,
        url = url,
        urlToImage = urlToImage,
        publishedAt = publishedAt,
        isFavorite = isFavorite
    )
}