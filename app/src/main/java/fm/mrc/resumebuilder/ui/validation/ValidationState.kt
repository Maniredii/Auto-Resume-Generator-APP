package fm.mrc.resumebuilder.ui.validation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import fm.mrc.resumebuilder.data.model.PersonalInfo

/**
 * State management for form validation
 */
class ValidationState {
    private var _errors by mutableStateOf<Map<String, String>>(emptyMap())
    val errors: Map<String, String> get() = _errors
    
    private var _isValidating by mutableStateOf(false)
    val isValidating: Boolean get() = _isValidating
    
    private var _showErrors by mutableStateOf(false)
    val showErrors: Boolean get() = _showErrors
    
    /**
     * Validate a specific field
     */
    fun validateField(fieldName: String, value: String, validationType: ValidationType) {
        val error = when (validationType) {
            ValidationType.REQUIRED -> if (value.isBlank()) "This field is required" else null
            ValidationType.EMAIL -> if (value.isNotBlank() && !isValidEmail(value)) "Invalid email format" else null
            ValidationType.PHONE -> if (value.isNotBlank() && !isValidPhone(value)) "Invalid phone format" else null
            ValidationType.URL -> if (value.isNotBlank() && !isValidUrl(value)) "Invalid URL format" else null
            ValidationType.MIN_LENGTH -> if (value.isNotBlank() && value.length < 2) "Must be at least 2 characters" else null
            ValidationType.MAX_LENGTH -> if (value.length > 100) "Must be less than 100 characters" else null
        }
        
        updateFieldError(fieldName, error)
    }
    
    /**
     * Validate personal info section
     */
    fun validatePersonalInfo(personalInfo: PersonalInfo) {
        _isValidating = true
        
        val result = FormValidator.validatePersonalInfo(personalInfo)
        _errors = result.errors
        _showErrors = true
        
        _isValidating = false
    }
    
    /**
     * Validate summary
     */
    fun validateSummary(summary: String) {
        _isValidating = true
        
        val result = FormValidator.validateSummary(summary)
        _errors = _errors + result.errors
        _showErrors = true
        
        _isValidating = false
    }
    
    /**
     * Validate skills
     */
    fun validateSkills(skills: List<String>) {
        _isValidating = true
        
        val result = FormValidator.validateSkills(skills)
        _errors = _errors + result.errors
        _showErrors = true
        
        _isValidating = false
    }
    
    /**
     * Get error for specific field
     */
    fun getFieldError(fieldName: String): String? {
        return if (_showErrors) _errors[fieldName] else null
    }
    
    /**
     * Check if field has error
     */
    fun hasFieldError(fieldName: String): Boolean {
        return _showErrors && _errors.containsKey(fieldName)
    }
    
    /**
     * Clear all errors
     */
    fun clearErrors() {
        _errors = emptyMap()
        _showErrors = false
    }
    
    /**
     * Clear error for specific field
     */
    fun clearFieldError(fieldName: String) {
        _errors = _errors.filterKeys { it != fieldName }
    }
    
    /**
     * Update error for specific field
     */
    private fun updateFieldError(fieldName: String, error: String?) {
        _errors = if (error != null) {
            _errors + (fieldName to error)
        } else {
            _errors.filterKeys { it != fieldName }
        }
    }
    
    // Helper validation functions
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$".toRegex()
        return emailRegex.matches(email)
    }
    
    private fun isValidPhone(phone: String): Boolean {
        val phoneRegex = "^[+]?[0-9\\s\\-\\(\\)]{10,}$".toRegex()
        return phoneRegex.matches(phone.replace(" ", ""))
    }
    
    private fun isValidUrl(url: String): Boolean {
        val urlRegex = "^(https?://)?([\\da-z\\.-]+)\\.([a-z\\.]{2,6})([/\\w \\.-]*)*/?$".toRegex()
        return urlRegex.matches(url)
    }
}

/**
 * Validation types for different field validations
 */
enum class ValidationType {
    REQUIRED,
    EMAIL,
    PHONE,
    URL,
    MIN_LENGTH,
    MAX_LENGTH
}
