package by.bsu.newsly.ui.mainActivity.home.favorite_articles

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.bsu.newsly.domain.model.Article
import by.bsu.newsly.domain.repository.ArticleRepository
import by.bsu.newsly.domain.repository.BookmarkRepository
import kotlinx.coroutines.launch

class FavoriteArticlesViewModel(
    private val repo: ArticleRepository,
    private val bookmarkRepo: BookmarkRepository
) : ViewModel() {
    val favorites = MutableLiveData<MutableList<Article>>()
    val errorMessage = MutableLiveData<String>()

    fun loadFavorites() {
        viewModelScope.launch {
            try {
                val list = repo.getFavoriteArticles()
                favorites.postValue(list.toMutableList())
            } catch (e: Exception) {
                errorMessage.postValue(e.message)
            }
        }
    }

    fun deleteFavorite(article: Article) {
        viewModelScope.launch {
            try {
                repo.deleteArticle(article.copy(isFavorite = true).also { it.isFavorite = false })
                bookmarkRepo.deleteBookmarksByUrl(article.url)
                loadFavorites()
            } catch (e: Exception) {
                errorMessage.postValue(e.message)
            }
        }
    }
}