package fm.mrc.resumebuilder.data.version

import fm.mrc.resumebuilder.data.model.*
import java.time.LocalDateTime
import java.util.*

/**
 * Manager for resume versions
 */
class VersionManager {
    
    private val versions = mutableMapOf<String, MutableList<ResumeVersion>>()
    
    /**
     * Create a new version of a resume
     */
    fun createVersion(
        resumeId: String,
        resume: Resume,
        versionName: String,
        description: String? = null
    ): ResumeVersion {
        val existingVersions = versions[resumeId] ?: mutableListOf()
        val versionNumber = existingVersions.size + 1
        
        // Deactivate current active version
        existingVersions.forEach { it.copy(isActive = false) }
        
        val newVersion = ResumeVersion(
            id = UUID.randomUUID().toString(),
            resumeId = resumeId,
            versionNumber = versionNumber,
            name = versionName,
            description = description,
            createdAt = LocalDateTime.now(),
            isActive = true,
            changes = listOf(
                VersionChange(
                    field = "resume",
                    oldValue = "",
                    newValue = "Initial version",
                    changeType = ChangeType.CREATED,
                    timestamp = LocalDateTime.now()
                )
            )
        )
        
        existingVersions.add(newVersion)
        versions[resumeId] = existingVersions
        
        return newVersion
    }
    
    /**
     * Update an existing version
     */
    fun updateVersion(
        resumeId: String,
        versionId: String,
        resume: Resume,
        changes: List<VersionChange>
    ): ResumeVersion? {
        val existingVersions = versions[resumeId] ?: return null
        val versionIndex = existingVersions.indexOfFirst { it.id == versionId }
        
        if (versionIndex == -1) return null
        
        val updatedVersion = existingVersions[versionIndex].copy(
            changes = existingVersions[versionIndex].changes + changes
        )
        
        existingVersions[versionIndex] = updatedVersion
        return updatedVersion
    }
    
    /**
     * Get all versions for a resume
     */
    fun getVersions(resumeId: String): List<ResumeVersion> {
        return versions[resumeId] ?: emptyList()
    }
    
    /**
     * Get version summaries for a resume
     */
    fun getVersionSummaries(resumeId: String): List<ResumeVersionSummary> {
        return getVersions(resumeId).map { version ->
            ResumeVersionSummary(
                id = version.id,
                versionNumber = version.versionNumber,
                name = version.name,
                description = version.description,
                createdAt = version.createdAt,
                isActive = version.isActive,
                changeCount = version.changes.size
            )
        }
    }
    
    /**
     * Get active version for a resume
     */
    fun getActiveVersion(resumeId: String): ResumeVersion? {
        return getVersions(resumeId).find { it.isActive }
    }
    
    /**
     * Set active version
     */
    fun setActiveVersion(resumeId: String, versionId: String): Boolean {
        val existingVersions = versions[resumeId] ?: return false
        
        // Deactivate all versions
        existingVersions.forEachIndexed { index, version ->
            existingVersions[index] = version.copy(isActive = false)
        }
        
        // Activate selected version
        val versionIndex = existingVersions.indexOfFirst { it.id == versionId }
        if (versionIndex != -1) {
            existingVersions[versionIndex] = existingVersions[versionIndex].copy(isActive = true)
            return true
        }
        
        return false
    }
    
    /**
     * Delete a version
     */
    fun deleteVersion(resumeId: String, versionId: String): Boolean {
        val existingVersions = versions[resumeId] ?: return false
        val versionIndex = existingVersions.indexOfFirst { it.id == versionId }
        
        if (versionIndex == -1) return false
        
        val versionToDelete = existingVersions[versionIndex]
        
        // Don't delete if it's the only version
        if (existingVersions.size <= 1) return false
        
        // If deleting active version, activate the most recent one
        if (versionToDelete.isActive) {
            val mostRecentVersion = existingVersions
                .filter { it.id != versionId }
                .maxByOrNull { it.versionNumber }
            mostRecentVersion?.let {
                val index = existingVersions.indexOfFirst { v -> v.id == it.id }
                if (index != -1) {
                    existingVersions[index] = it.copy(isActive = true)
                }
            }
        }
        
        existingVersions.removeAt(versionIndex)
        return true
    }
    
    /**
     * Compare two versions
     */
    fun compareVersions(
        resumeId: String,
        versionId1: String,
        versionId2: String
    ): VersionComparison? {
        val versions = getVersions(resumeId)
        val version1 = versions.find { it.id == versionId1 } ?: return null
        val version2 = versions.find { it.id == versionId2 } ?: return null
        
        return VersionComparison(
            version1 = version1,
            version2 = version2,
            differences = calculateDifferences(version1, version2)
        )
    }
    
    /**
     * Get version history
     */
    fun getVersionHistory(resumeId: String): List<VersionHistoryItem> {
        return getVersions(resumeId)
            .sortedByDescending { it.createdAt }
            .map { version ->
                VersionHistoryItem(
                    version = version,
                    changes = version.changes,
                    changeCount = version.changes.size
                )
            }
    }
    
    private fun calculateDifferences(version1: ResumeVersion, version2: ResumeVersion): List<VersionDifference> {
        val differences = mutableListOf<VersionDifference>()
        
        // Compare changes
        val changes1 = version1.changes
        val changes2 = version2.changes
        
        // Find added changes in version2
        changes2.forEach { change2 ->
            if (!changes1.any { it.field == change2.field && it.newValue == change2.newValue }) {
                differences.add(
                    VersionDifference(
                        field = change2.field,
                        changeType = ChangeType.ADDED,
                        value = change2.newValue,
                        timestamp = change2.timestamp
                    )
                )
            }
        }
        
        // Find removed changes from version1
        changes1.forEach { change1 ->
            if (!changes2.any { it.field == change1.field && it.newValue == change1.newValue }) {
                differences.add(
                    VersionDifference(
                        field = change1.field,
                        changeType = ChangeType.REMOVED,
                        value = change1.newValue,
                        timestamp = change1.timestamp
                    )
                )
            }
        }
        
        return differences
    }
}

/**
 * Version comparison result
 */
data class VersionComparison(
    val version1: ResumeVersion,
    val version2: ResumeVersion,
    val differences: List<VersionDifference>
)

/**
 * Version difference
 */
data class VersionDifference(
    val field: String,
    val changeType: ChangeType,
    val value: String,
    val timestamp: LocalDateTime
)

/**
 * Version history item
 */
data class VersionHistoryItem(
    val version: ResumeVersion,
    val changes: List<VersionChange>,
    val changeCount: Int
)
