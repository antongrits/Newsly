package by.bsu.newsly.ui.mainActivity.history

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.bsu.newsly.domain.model.History
import by.bsu.newsly.domain.repository.HistoryRepository
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val repo: HistoryRepository
) : ViewModel() {
    val historyFromDB = MutableLiveData<MutableList<History>>()
    val selectedHistory = MutableLiveData<History?>()
    val errorMessage = MutableLiveData<String>()

    fun loadWholeHistory() {
        viewModelScope.launch {
            try {
                historyFromDB.postValue(repo.getAllHistory().toMutableList())
                errorMessage.postValue("")
            } catch (e: Exception) {
                errorMessage.postValue(e.message)
            }
        }
    }

    fun insertHistory(h: History) {
        viewModelScope.launch {
            repo.insertHistory(h)
            loadWholeHistory()
        }
    }

    fun deleteHistory(h: History) {
        viewModelScope.launch {
            repo.deleteHistory(h)
            loadWholeHistory()
        }
    }

    fun updateHistory(h: History) {
        viewModelScope.launch {
            repo.updateHistory(h)
            loadWholeHistory()
        }
    }
}