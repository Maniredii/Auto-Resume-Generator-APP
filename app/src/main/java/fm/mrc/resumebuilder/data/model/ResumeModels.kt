package fm.mrc.resumebuilder.data.model

import java.time.LocalDateTime

/**
 * Data classes representing the resume schema
 */
data class Resume(
    val id: String,
    val metadata: ResumeMetadata,
    val personal: PersonalInfo,
    val summary: String,
    val skills: List<String>,
    val education: List<Education>,
    val experience: List<Experience>,
    val projects: List<Project>
)

data class ResumeMetadata(
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val template: String
)

data class PersonalInfo(
    val fullName: String,
    val title: String,
    val email: String,
    val phone: String,
    val linkedin: String,
    val github: String,
    val website: String,
    val location: String,
    val avatarUri: String? = null
)

data class Education(
    val id: String,
    val institution: String,
    val degree: String,
    val start: String,
    val end: String,
    val details: String
)

data class Experience(
    val id: String,
    val company: String,
    val role: String,
    val start: String,
    val end: String,
    val bullets: List<String>
)

data class Project(
    val id: String,
    val title: String,
    val description: String,
    val link: String,
    val tech: List<String>
)
