package by.bsu.newsly.domain.repository

import by.bsu.newsly.data.local.db.dao.BookmarkDao
import by.bsu.newsly.data.local.db.entity.BookmarkEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BookmarkRepository(private val dao: BookmarkDao) {

    suspend fun getAllForUrl(url: String): List<BookmarkEntity> =
        withContext(Dispatchers.IO) { dao.getAllForUrl(url) }

    suspend fun delete(bookmark: BookmarkEntity) =
        withContext(Dispatchers.IO) { dao.deleteBookmark(bookmark) }

    suspend fun deleteBookmarksByUrl(url: String) = withContext(Dispatchers.IO) {
        dao.deleteBookmarksByUrl(url)
    }

    suspend fun mergeAndSave(
        url: String,
        selectedText: String,
        fullText: String,
        startOffset: Int,
        endOffset: Int,
        scrollPosition: Int
    ): Long = withContext(Dispatchers.IO) {
        val slice = selectedText.trim()
        require(slice.isNotEmpty()) { "Выделите текст" }

        val existing = dao.getAllForUrl(url)
        val overlapping = mutableListOf<BookmarkEntity>()

        existing.forEach { bookmark ->
            val isInsideNew = bookmark.startOffset >= startOffset && bookmark.endOffset <= endOffset
            val isNewInside = startOffset >= bookmark.startOffset && endOffset <= bookmark.endOffset
            val isOverlap = bookmark.startOffset < endOffset && startOffset < bookmark.endOffset
            val isAdjacent = bookmark.endOffset == startOffset || endOffset == bookmark.startOffset

            if (isNewInside) {
                overlapping.add(bookmark)
                dao.deleteBookmark(bookmark)
                return@withContext dao.insertBookmark(
                    BookmarkEntity(
                        articleUrl = url,
                        selectedText = slice,
                        startOffset = startOffset,
                        endOffset = endOffset,
                        scrollPosition = scrollPosition,
                        createdAt = System.currentTimeMillis()
                    )
                )
            }

            if (isInsideNew || isOverlap || isAdjacent) {
                overlapping.add(bookmark)
            }
        }

        val newStart =
            overlapping.minOfOrNull { it.startOffset }?.coerceAtMost(startOffset) ?: startOffset
        val newEnd = overlapping.maxOfOrNull { it.endOffset }?.coerceAtLeast(endOffset) ?: endOffset

        overlapping.forEach { dao.deleteBookmark(it) }

        val mergedText = fullText.substring(newStart, newEnd).trim()
        require(mergedText.isNotEmpty()) { "Выделите текст" }

        dao.insertBookmark(
            BookmarkEntity(
                articleUrl = url,
                selectedText = mergedText,
                startOffset = newStart,
                endOffset = newEnd,
                scrollPosition = scrollPosition,
                createdAt = System.currentTimeMillis()
            )
        )
    }
}