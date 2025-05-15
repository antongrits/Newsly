package by.bsu.newsly.ui.mainActivity.home.all_articles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import by.bsu.newsly.domain.repository.ArticleRepository
import by.bsu.newsly.domain.repository.BookmarkRepository

class AllArticlesViewModelFactory(
    private val repository: ArticleRepository,
    private val bookmarkRepo: BookmarkRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AllArticlesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AllArticlesViewModel(repository, bookmarkRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}