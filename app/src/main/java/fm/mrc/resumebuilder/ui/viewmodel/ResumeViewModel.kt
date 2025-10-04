package fm.mrc.resumebuilder.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fm.mrc.resumebuilder.data.model.*
import fm.mrc.resumebuilder.data.repo.ResumeRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.*

/**
 * ViewModel for managing individual resume operations
 */
class ResumeViewModel(
    private val repository: ResumeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResumeUiState())
    val uiState: StateFlow<ResumeUiState> = _uiState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun loadResume(resumeId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val resume = repository.getResumeById(resumeId)
                if (resume != null) {
                    _uiState.value = ResumeUiState.fromResume(resume)
                } else {
                    _errorMessage.value = "Resume not found"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createNewResume() {
        _uiState.value = ResumeUiState(
            id = UUID.randomUUID().toString(),
            isNewResume = true
        )
    }

    fun updatePersonalInfo(personalInfo: PersonalInfo) {
        _uiState.value = _uiState.value.copy(personal = personalInfo)
    }

    fun updateSummary(summary: String) {
        _uiState.value = _uiState.value.copy(summary = summary)
    }

    fun updateSkills(skills: List<String>) {
        _uiState.value = _uiState.value.copy(skills = skills)
    }

    fun addSkill(skill: String) {
        val currentSkills = _uiState.value.skills.toMutableList()
        if (skill.isNotBlank() && !currentSkills.contains(skill)) {
            currentSkills.add(skill)
            _uiState.value = _uiState.value.copy(skills = currentSkills)
        }
    }

    fun removeSkill(skill: String) {
        val currentSkills = _uiState.value.skills.toMutableList()
        currentSkills.remove(skill)
        _uiState.value = _uiState.value.copy(skills = currentSkills)
    }

    fun updateEducation(education: List<Education>) {
        _uiState.value = _uiState.value.copy(education = education)
    }

    fun addEducation() {
        val currentEducation = _uiState.value.education.toMutableList()
        currentEducation.add(
            Education(
                id = UUID.randomUUID().toString(),
                institution = "",
                degree = "",
                start = "",
                end = "",
                details = ""
            )
        )
        _uiState.value = _uiState.value.copy(education = currentEducation)
    }

    fun removeEducation(educationId: String) {
        val currentEducation = _uiState.value.education.toMutableList()
        currentEducation.removeAll { it.id == educationId }
        _uiState.value = _uiState.value.copy(education = currentEducation)
    }

    fun updateExperience(experience: List<Experience>) {
        _uiState.value = _uiState.value.copy(experience = experience)
    }

    fun addExperience() {
        val currentExperience = _uiState.value.experience.toMutableList()
        currentExperience.add(
            Experience(
                id = UUID.randomUUID().toString(),
                company = "",
                role = "",
                start = "",
                end = "",
                bullets = emptyList()
            )
        )
        _uiState.value = _uiState.value.copy(experience = currentExperience)
    }

    fun removeExperience(experienceId: String) {
        val currentExperience = _uiState.value.experience.toMutableList()
        currentExperience.removeAll { it.id == experienceId }
        _uiState.value = _uiState.value.copy(experience = currentExperience)
    }

    fun updateProjects(projects: List<Project>) {
        _uiState.value = _uiState.value.copy(projects = projects)
    }

    fun addProject() {
        val currentProjects = _uiState.value.projects.toMutableList()
        currentProjects.add(
            Project(
                id = UUID.randomUUID().toString(),
                title = "",
                description = "",
                link = "",
                tech = emptyList()
            )
        )
        _uiState.value = _uiState.value.copy(projects = currentProjects)
    }

    fun removeProject(projectId: String) {
        val currentProjects = _uiState.value.projects.toMutableList()
        currentProjects.removeAll { it.id == projectId }
        _uiState.value = _uiState.value.copy(projects = currentProjects)
    }

    fun saveResume() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentState = _uiState.value
                val now = LocalDateTime.now()
                val resume = Resume(
                    id = currentState.id,
                    metadata = ResumeMetadata(
                        createdAt = if (currentState.isNewResume) now else currentState.createdAt,
                        updatedAt = now,
                        template = currentState.template
                    ),
                    personal = currentState.personal,
                    summary = currentState.summary,
                    skills = currentState.skills,
                    education = currentState.education,
                    experience = currentState.experience,
                    projects = currentState.projects
                )

                if (currentState.isNewResume) {
                    repository.insertResume(resume)
                } else {
                    repository.updateResume(resume)
                }

                _uiState.value = _uiState.value.copy(
                    isNewResume = false,
                    updatedAt = now
                )
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    class Factory(private val repository: ResumeRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ResumeViewModel::class.java)) {
                return ResumeViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

/**
 * UI State for Resume editing
 */
data class ResumeUiState(
    val id: String = "",
    val personal: PersonalInfo = PersonalInfo("", "", "", "", "", "", "", "", null),
    val summary: String = "",
    val skills: List<String> = emptyList(),
    val education: List<Education> = emptyList(),
    val experience: List<Experience> = emptyList(),
    val projects: List<Project> = emptyList(),
    val template: String = "modern",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val isNewResume: Boolean = false
) {
    companion object {
        fun fromResume(resume: Resume): ResumeUiState {
            return ResumeUiState(
                id = resume.id,
                personal = resume.personal,
                summary = resume.summary,
                skills = resume.skills,
                education = resume.education,
                experience = resume.experience,
                projects = resume.projects,
                template = resume.metadata.template,
                createdAt = resume.metadata.createdAt,
                updatedAt = resume.metadata.updatedAt,
                isNewResume = false
            )
        }
    }
}
