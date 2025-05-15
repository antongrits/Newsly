package by.bsu.newsly.ui.mainActivity.home.recommendations

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.bsu.newsly.domain.model.Article
import by.bsu.newsly.domain.repository.ArticleRepository
import by.bsu.newsly.domain.repository.RecommendationRepository
import kotlinx.coroutines.launch

class RecommendationViewModel(
    private val recRepo: RecommendationRepository,
    private val articleRepo: ArticleRepository
) : ViewModel() {

    val recommendations = MutableLiveData<MutableList<Article>?>()
    val loading = MutableLiveData<Boolean>()
    val error = MutableLiveData<String?>()
    val favoriteChanged = MutableLiveData<String>()

    init {
        load()
    }

    fun load() {
        loading.value = true
        error.value = null
        viewModelScope.launch {
            try {
                val recs = recRepo.getRecommendations()
                val favUrls = articleRepo.getFavoriteArticles().map { it.url }.toSet()
                recommendations.postValue(
                    recs.map { it.copy(isFavorite = favUrls.contains(it.url)) }
                        .toMutableList()
                )
            } catch (e: Exception) {
                error.postValue(e.message)
            } finally {
                loading.postValue(false)
            }
        }
    }

    fun updateArticle(a: Article) {
        viewModelScope.launch {
            if (a.isFavorite) {
                articleRepo.deleteArticle(a.copy(isFavorite = true))
            } else {
                articleRepo.insertArticle(a.copy(isFavorite = true))
            }
            val updated = recommendations.value
                ?.map { if (it.url == a.url) it.copy(isFavorite = !a.isFavorite) else it }
                ?.toMutableList()
            recommendations.postValue(updated)
            favoriteChanged.postValue(a.url)
        }
    }

    fun syncFavorites() {
        viewModelScope.launch {
            val favUrls = articleRepo.getFavoriteArticles().map { it.url }.toSet()
            recommendations.value = recommendations.value?.map {
                it.copy(isFavorite = favUrls.contains(it.url))
            }?.toMutableList()
        }
    }
}