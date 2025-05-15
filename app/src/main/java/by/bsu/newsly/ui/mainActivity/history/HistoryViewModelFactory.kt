package by.bsu.newsly.ui.mainActivity.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import by.bsu.newsly.domain.repository.HistoryRepository

class HistoryViewModelFactory(
    private val repository: HistoryRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}