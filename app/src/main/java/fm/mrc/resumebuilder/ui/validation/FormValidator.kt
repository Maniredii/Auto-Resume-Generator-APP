package fm.mrc.resumebuilder.ui.validation

import fm.mrc.resumebuilder.data.model.PersonalInfo

/**
 * Form validation utilities
 */
object FormValidator {
    
    /**
     * Validation result
     */
    data class ValidationResult(
        val isValid: Boolean,
        val errors: Map<String, String> = emptyMap()
    )
    
    /**
     * Validate personal information
     */
    fun validatePersonalInfo(personalInfo: PersonalInfo): ValidationResult {
        val errors = mutableMapOf<String, String>()
        
        // Required fields
        if (personalInfo.fullName.isBlank()) {
            errors["fullName"] = "Full name is required"
        } else if (personalInfo.fullName.length < 2) {
            errors["fullName"] = "Full name must be at least 2 characters"
        }
        
        if (personalInfo.email.isBlank()) {
            errors["email"] = "Email is required"
        } else if (!isValidEmail(personalInfo.email)) {
            errors["email"] = "Please enter a valid email address"
        }
        
        if (personalInfo.phone.isBlank()) {
            errors["phone"] = "Phone number is required"
        } else if (!isValidPhone(personalInfo.phone)) {
            errors["phone"] = "Please enter a valid phone number"
        }
        
        // Optional fields with validation
        if (personalInfo.title.isNotBlank() && personalInfo.title.length < 3) {
            errors["title"] = "Title must be at least 3 characters"
        }
        
        if (personalInfo.location.isNotBlank() && personalInfo.location.length < 2) {
            errors["location"] = "Location must be at least 2 characters"
        }
        
        // URL validation for links
        if (personalInfo.linkedin.isNotBlank() && !isValidUrl(personalInfo.linkedin)) {
            errors["linkedin"] = "Please enter a valid LinkedIn URL"
        }
        
        if (personalInfo.github.isNotBlank() && !isValidUrl(personalInfo.github)) {
            errors["github"] = "Please enter a valid GitHub URL"
        }
        
        if (personalInfo.website.isNotBlank() && !isValidUrl(personalInfo.website)) {
            errors["website"] = "Please enter a valid website URL"
        }
        
        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
    
    /**
     * Validate summary
     */
    fun validateSummary(summary: String): ValidationResult {
        val errors = mutableMapOf<String, String>()
        
        if (summary.isBlank()) {
            errors["summary"] = "Professional summary is required"
        } else if (summary.length < 50) {
            errors["summary"] = "Summary must be at least 50 characters"
        } else if (summary.length > 500) {
            errors["summary"] = "Summary must be less than 500 characters"
        }
        
        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
    
    /**
     * Validate skills
     */
    fun validateSkills(skills: List<String>): ValidationResult {
        val errors = mutableMapOf<String, String>()
        
        if (skills.isEmpty()) {
            errors["skills"] = "At least one skill is required"
        } else if (skills.size > 20) {
            errors["skills"] = "Maximum 20 skills allowed"
        } else {
            // Check for empty or invalid skills
            val invalidSkills = skills.filter { it.isBlank() || it.length < 2 }
            if (invalidSkills.isNotEmpty()) {
                errors["skills"] = "All skills must be at least 2 characters"
            }
        }
        
        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
    
    /**
     * Validate experience
     */
    fun validateExperience(experience: List<fm.mrc.resumebuilder.data.model.Experience>): ValidationResult {
        val errors = mutableMapOf<String, String>()
        
        if (experience.isEmpty()) {
            errors["experience"] = "At least one work experience is required"
        } else {
            experience.forEachIndexed { index, exp ->
                if (exp.company.isBlank()) {
                    errors["experience_${index}_company"] = "Company name is required"
                }
                if (exp.role.isBlank()) {
                    errors["experience_${index}_role"] = "Job title is required"
                }
                if (exp.start.isBlank()) {
                    errors["experience_${index}_start"] = "Start date is required"
                }
                if (exp.bullets.isEmpty()) {
                    errors["experience_${index}_bullets"] = "At least one achievement is required"
                }
            }
        }
        
        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
    
    /**
     * Validate education
     */
    fun validateEducation(education: List<fm.mrc.resumebuilder.data.model.Education>): ValidationResult {
        val errors = mutableMapOf<String, String>()
        
        if (education.isEmpty()) {
            errors["education"] = "At least one education entry is required"
        } else {
            education.forEachIndexed { index, edu ->
                if (edu.institution.isBlank()) {
                    errors["education_${index}_institution"] = "Institution name is required"
                }
                if (edu.degree.isBlank()) {
                    errors["education_${index}_degree"] = "Degree is required"
                }
                if (edu.start.isBlank()) {
                    errors["education_${index}_start"] = "Start date is required"
                }
            }
        }
        
        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
    
    /**
     * Validate entire resume
     */
    fun validateResume(
        personalInfo: PersonalInfo,
        summary: String,
        skills: List<String>,
        experience: List<fm.mrc.resumebuilder.data.model.Experience>,
        education: List<fm.mrc.resumebuilder.data.model.Education>
    ): ValidationResult {
        val allErrors = mutableMapOf<String, String>()
        
        // Validate each section
        val personalResult = validatePersonalInfo(personalInfo)
        val summaryResult = validateSummary(summary)
        val skillsResult = validateSkills(skills)
        val experienceResult = validateExperience(experience)
        val educationResult = validateEducation(education)
        
        // Combine all errors
        allErrors.putAll(personalResult.errors)
        allErrors.putAll(summaryResult.errors)
        allErrors.putAll(skillsResult.errors)
        allErrors.putAll(experienceResult.errors)
        allErrors.putAll(educationResult.errors)
        
        return ValidationResult(
            isValid = allErrors.isEmpty(),
            errors = allErrors
        )
    }
    
    // Helper functions
    
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
