package by.bsu.newsly.data.remote.model

data class NewsResponse(
    val status: String,
    val totalResults: Int?,
    val message: String?,
    val articles: List<ArticleNetwork>
)

data class ArticleNetwork(
    val source: SourceNetwork,
    val author: String?,
    val title: String,
    val description: String?,
    val url: String,
    val urlToImage: String?,
    val publishedAt: String
)

data class SourceNetwork(
    val name: String
)