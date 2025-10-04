package fm.mrc.resumebuilder.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import fm.mrc.resumebuilder.data.converters.Converters
import fm.mrc.resumebuilder.data.model.*

/**
 * Room Entity for storing resume data
 * Using a single entity with TypeConverters for simplicity
 */
@Entity(tableName = "resumes")
@TypeConverters(Converters::class)
data class ResumeEntity(
    @PrimaryKey
    val id: String,
    val name: String, // User-friendly name for the resume
    val metadata: ResumeMetadata,
    val personal: PersonalInfo,
    val summary: String,
    val skills: List<String>,
    val education: List<Education>,
    val experience: List<Experience>,
    val projects: List<Project>
)

/**
 * Extension functions to convert between Resume and ResumeEntity
 */
fun Resume.toEntity(): ResumeEntity {
    return ResumeEntity(
        id = id,
        name = personal.fullName.ifBlank { "Untitled Resume" },
        metadata = metadata,
        personal = personal,
        summary = summary,
        skills = skills,
        education = education,
        experience = experience,
        projects = projects
    )
}

fun ResumeEntity.toResume(): Resume {
    return Resume(
        id = id,
        metadata = metadata,
        personal = personal,
        summary = summary,
        skills = skills,
        education = education,
        experience = experience,
        projects = projects
    )
}
