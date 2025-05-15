package by.bsu.newsly.ui.mainActivity.home.recommendations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import by.bsu.newsly.domain.repository.ArticleRepository
import by.bsu.newsly.domain.repository.RecommendationRepository

class RecommendationViewModelFactory(
    private val repo: RecommendationRepository,
    private val articleRepo: ArticleRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecommendationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecommendationViewModel(repo, articleRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}