package fm.mrc.resumebuilder.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fm.mrc.resumebuilder.data.backup.JsonBackupManager
import fm.mrc.resumebuilder.data.db.ResumeDatabase
import fm.mrc.resumebuilder.data.repo.ResumeRepositoryImpl
import fm.mrc.resumebuilder.settings.DataStoreManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Settings screen
 */
class SettingsViewModel(
    private val dataStoreManager: DataStoreManager,
    private val jsonBackupManager: JsonBackupManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    init {
        // Load preferences
        viewModelScope.launch {
            dataStoreManager.preferences.collect { preferences ->
                _uiState.value = _uiState.value.copy(
                    isDarkTheme = preferences.isDarkTheme,
                    defaultTemplate = preferences.defaultTemplate,
                    isPremiumUser = preferences.isPremiumUser,
                    isSyncOptIn = preferences.isSyncOptIn,
                    isNotificationsEnabled = preferences.isNotificationsEnabled,
                    isAutoSaveEnabled = preferences.isAutoSaveEnabled
                )
            }
        }
    }
    
    fun updateDarkTheme(isDarkTheme: Boolean) {
        viewModelScope.launch {
            dataStoreManager.updateDarkTheme(isDarkTheme)
        }
    }
    
    fun updateDefaultTemplate(template: String) {
        viewModelScope.launch {
            dataStoreManager.updateDefaultTemplate(template)
        }
    }
    
    fun updatePremiumFlag(isPremium: Boolean) {
        viewModelScope.launch {
            dataStoreManager.updatePremiumFlag(isPremium)
        }
    }
    
    fun updateSyncOptIn(isOptIn: Boolean) {
        viewModelScope.launch {
            dataStoreManager.updateSyncOptIn(isOptIn)
        }
    }
    
    fun updateNotificationsEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.updateNotificationsEnabled(isEnabled)
        }
    }
    
    fun updateAutoSaveEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.updateAutoSaveEnabled(isEnabled)
        }
    }
    
    fun exportResumes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true)
            try {
                val result = jsonBackupManager.exportResumesToJson()
                result.fold(
                    onSuccess = { uri ->
                        _uiState.value = _uiState.value.copy(
                            isExporting = false,
                            exportUri = uri,
                            showExportSuccess = true
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isExporting = false,
                            errorMessage = error.message ?: "Export failed"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    errorMessage = e.message ?: "Export failed"
                )
            }
        }
    }
    
    fun importResumes(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isImporting = true)
            try {
                val result = jsonBackupManager.importResumesFromJson(uri)
                result.fold(
                    onSuccess = { count ->
                        _uiState.value = _uiState.value.copy(
                            isImporting = false,
                            showImportSuccess = true,
                            importedCount = count
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isImporting = false,
                            errorMessage = error.message ?: "Import failed"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    errorMessage = e.message ?: "Import failed"
                )
            }
        }
    }
    
    fun clearAllData() {
        viewModelScope.launch {
            try {
                // Clear all preferences
                dataStoreManager.clearAllPreferences()
                _uiState.value = _uiState.value.copy(
                    showClearDataSuccess = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Failed to clear data"
                )
            }
        }
    }
    
    fun dismissError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    fun dismissExportSuccess() {
        _uiState.value = _uiState.value.copy(showExportSuccess = false, exportUri = null)
    }
    
    fun dismissImportSuccess() {
        _uiState.value = _uiState.value.copy(showImportSuccess = false, importedCount = 0)
    }
    
    fun dismissClearDataSuccess() {
        _uiState.value = _uiState.value.copy(showClearDataSuccess = false)
    }
    
    companion object {
        fun Factory(context: Context): (ResumeDatabase) -> SettingsViewModel = { database ->
            val repository = ResumeRepositoryImpl(database.resumeDao())
            val dataStoreManager = DataStoreManager(context)
            val jsonBackupManager = JsonBackupManager(context, repository)
            SettingsViewModel(dataStoreManager, jsonBackupManager)
        }
    }
}

/**
 * UI State for Settings screen
 */
data class SettingsUiState(
    val isDarkTheme: Boolean = false,
    val defaultTemplate: String = "modern",
    val isPremiumUser: Boolean = false,
    val isSyncOptIn: Boolean = false,
    val isNotificationsEnabled: Boolean = true,
    val isAutoSaveEnabled: Boolean = true,
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val exportUri: Uri? = null,
    val showExportSuccess: Boolean = false,
    val showImportSuccess: Boolean = false,
    val showClearDataSuccess: Boolean = false,
    val importedCount: Int = 0,
    val errorMessage: String? = null
)
