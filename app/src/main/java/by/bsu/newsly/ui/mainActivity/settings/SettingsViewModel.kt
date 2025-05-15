package by.bsu.newsly.ui.mainActivity.settings

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.bsu.newsly.domain.repository.SettingsRepository
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repo: SettingsRepository
) : ViewModel() {

    val adBlockingEnabled = MutableLiveData<Boolean>()
    val cacheSizeText = MutableLiveData<String>()
    val isClearing = MutableLiveData<Boolean>()
    val didClear = MutableLiveData<Boolean>()

    init {
        loadAll()
    }

    private fun loadAll() {
        adBlockingEnabled.value = repo.getAdBlockingEnabled()
        cacheSizeText.value = repo.getFormattedCacheSize()
        isClearing.value = false
        didClear.value = false
    }

    fun setAdBlocking(enabled: Boolean) {
        repo.setAdBlockingEnabled(enabled)
        adBlockingEnabled.value = enabled
    }

    fun clearCache() {
        isClearing.value = true
        viewModelScope.launch {
            repo.clearCache()
            cacheSizeText.value = repo.getFormattedCacheSize()
            isClearing.value = false
            didClear.value = true
        }
    }
}