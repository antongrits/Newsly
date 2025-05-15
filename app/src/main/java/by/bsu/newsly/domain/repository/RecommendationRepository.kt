package by.bsu.newsly.domain.repository

import by.bsu.newsly.domain.model.Article
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RecommendationRepository(
    private val articleRepo: ArticleRepository,
    private val historyRepo: HistoryRepository
) {
    suspend fun getRecommendations(): List<Article> = withContext(Dispatchers.IO) {
        val history = historyRepo.getAllHistory()
        if (history.isEmpty()) return@withContext emptyList()
        val topQueries = history
            .groupingBy { it.query }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(3)
            .map { it.key }
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -7) }
        val weekAgo = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
        val all = mutableListOf<Article>()
        for (q in topQueries) {
            all += articleRepo.getArticlesFromApi(q, weekAgo, today, "popularity", "")
        }
        all
            .distinctBy { it.url }
            .take(20)
    }
}