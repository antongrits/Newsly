// BookmarkViewModel.kt
package by.bsu.newsly.ui.detailedArticleActivity.bookmarks

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import by.bsu.newsly.data.local.db.entity.BookmarkEntity
import by.bsu.newsly.domain.repository.BookmarkRepository

class BookmarkViewModel(private val repo: BookmarkRepository) : ViewModel() {

    private val _fullText = MutableStateFlow("")
    val fullText = _fullText.asStateFlow()

    private val _bookmarks = MutableStateFlow<List<BookmarkEntity>>(emptyList())
    val bookmarks = _bookmarks.asStateFlow()

    fun setFullText(text: String) {
        _fullText.value = text
    }

    suspend fun loadBookmarks(url: String) {
        _bookmarks.value = repo.getAllForUrl(url)
    }

    suspend fun mergeAndSave(
        url: String,
        selectedText: String,
        startOffset: Int,
        endOffset: Int,
        scrollPosition: Int
    ): Long {
        val id = repo.mergeAndSave(
            url,
            selectedText,
            fullText.value,
            startOffset,
            endOffset,
            scrollPosition
        )
        _bookmarks.value = repo.getAllForUrl(url)
        return id
    }

    suspend fun deleteBookmark(bookmark: BookmarkEntity, url: String) {
        repo.delete(bookmark)
        _bookmarks.value = repo.getAllForUrl(url)
    }
}