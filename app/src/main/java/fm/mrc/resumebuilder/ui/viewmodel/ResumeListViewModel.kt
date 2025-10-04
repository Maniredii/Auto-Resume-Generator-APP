package fm.mrc.resumebuilder.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fm.mrc.resumebuilder.data.model.Resume
import fm.mrc.resumebuilder.data.repo.ResumeRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for managing the list of resumes
 */
class ResumeListViewModel(
    private val repository: ResumeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResumeListUiState())
    val uiState: StateFlow<ResumeListUiState> = _uiState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadResumes()
    }

    private fun loadResumes() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.getAllResumes().collect { resumes ->
                    _uiState.value = _uiState.value.copy(
                        resumes = resumes.sortedByDescending { it.metadata.updatedAt }
                    )
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
                _isLoading.value = false
            }
        }
    }

    fun refreshResumes() {
        loadResumes()
    }

    fun deleteResume(resumeId: String) {
        viewModelScope.launch {
            try {
                repository.deleteResumeById(resumeId)
                // The Flow will automatically update the UI
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    class Factory(private val repository: ResumeRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ResumeListViewModel::class.java)) {
                return ResumeListViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

/**
 * UI State for Resume list
 */
data class ResumeListUiState(
    val resumes: List<Resume> = emptyList()
)
