package by.bsu.newsly.ui.mainActivity.home.all_articles

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.bsu.newsly.domain.model.Article
import by.bsu.newsly.domain.repository.ArticleRepository
import by.bsu.newsly.domain.repository.BookmarkRepository
import kotlinx.coroutines.launch

class AllArticlesViewModel(
    private val repo: ArticleRepository,
    private val bookmarkRepo: BookmarkRepository
) : ViewModel() {

    val articlesFromDB = MutableLiveData<MutableList<Article>>()
    val articlesFromAPI = MutableLiveData<MutableList<Article>>()
    val errorMessage = MutableLiveData<String>()
    var isDateAndSortContVisible = false

    fun loadAllArticles() {
        viewModelScope.launch {
            try {
                val list = repo.getLocalArticles()
                articlesFromDB.postValue(list.toMutableList())
                errorMessage.postValue("")
            } catch (e: Exception) {
                errorMessage.postValue(e.message)
            }
        }
    }

    fun insertArticle(article: Article) {
        viewModelScope.launch {
            try {
                repo.insertArticle(article)
                loadAllArticles()
            } catch (e: Exception) {
                errorMessage.postValue(e.message)
            }
        }
    }

    fun deleteArticle(article: Article) {
        viewModelScope.launch {
            try {
                repo.deleteArticle(article)
                bookmarkRepo.deleteBookmarksByUrl(article.url)
                loadAllArticles()
            } catch (e: Exception) {
                errorMessage.postValue(e.message)
            }
        }
    }

    fun getArticlesFromApi(
        query: String,
        startDate: String,
        endDate: String,
        sortBy: String,
        language: String
    ) {
        viewModelScope.launch {
            try {
                val list = repo.getArticlesFromApi(query, startDate, endDate, sortBy, language)
                articlesFromAPI.postValue(list.toMutableList())
                errorMessage.postValue("")
            } catch (e: Exception) {
                errorMessage.postValue(e.message)
            }
        }
    }

    fun syncArticles() {
        viewModelScope.launch {
            try {
                val apiList = articlesFromAPI.value ?: return@launch
                val synced = repo.syncArticles(apiList)
                articlesFromAPI.postValue(synced.toMutableList())
                errorMessage.postValue("")
            } catch (e: Exception) {
                errorMessage.postValue(e.message)
            }
        }
    }
}