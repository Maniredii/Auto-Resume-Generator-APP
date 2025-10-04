package fm.mrc.resumebuilder.data.model

import java.time.LocalDateTime

/**
 * Resume version data class
 */
data class ResumeVersion(
    val id: String,
    val resumeId: String,
    val versionNumber: Int,
    val name: String,
    val description: String? = null,
    val createdAt: LocalDateTime,
    val isActive: Boolean = false,
    val changes: List<VersionChange> = emptyList()
)

/**
 * Version change tracking
 */
data class VersionChange(
    val field: String,
    val oldValue: String,
    val newValue: String,
    val changeType: ChangeType,
    val timestamp: LocalDateTime
)

/**
 * Types of changes
 */
enum class ChangeType {
    CREATED,
    UPDATED,
    DELETED,
    ADDED,
    REMOVED
}

/**
 * Resume version summary for display
 */
data class ResumeVersionSummary(
    val id: String,
    val versionNumber: Int,
    val name: String,
    val description: String?,
    val createdAt: LocalDateTime,
    val isActive: Boolean,
    val changeCount: Int
)
