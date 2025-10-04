package fm.mrc.resumebuilder.data.repo

import fm.mrc.resumebuilder.data.dao.ResumeDao
import fm.mrc.resumebuilder.data.entity.ResumeEntity
import fm.mrc.resumebuilder.data.entity.toEntity
import fm.mrc.resumebuilder.data.entity.toResume
import fm.mrc.resumebuilder.data.model.Resume
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository class that provides a clean API for data access
 * Acts as a single source of truth for resume data
 */
open class ResumeRepository(
    private val resumeDao: ResumeDao
) {
    
    /**
     * Get all resumes as a Flow for reactive updates
     */
    fun getAllResumes(): Flow<List<Resume>> {
        return resumeDao.getAllResumes().map { entities ->
            entities.map { it.toResume() }
        }
    }
    
    /**
     * Get all resumes as a list (for one-time queries)
     */
    suspend fun getAllResumesList(): List<Resume> {
        return resumeDao.getAllResumesList().map { it.toResume() }
    }
    
    /**
     * Get a specific resume by ID
     */
    suspend fun getResumeById(id: String): Resume? {
        return resumeDao.getResumeById(id)?.toResume()
    }
    
    /**
     * Get a specific resume by ID as Flow
     */
    fun getResumeByIdFlow(id: String): Flow<Resume?> {
        return resumeDao.getResumeByIdFlow(id).map { entity ->
            entity?.toResume()
        }
    }
    
    /**
     * Insert a new resume
     */
    suspend fun insertResume(resume: Resume) {
        resumeDao.insertResume(resume.toEntity())
    }
    
    /**
     * Update an existing resume
     */
    suspend fun updateResume(resume: Resume) {
        resumeDao.updateResume(resume.toEntity())
    }
    
    /**
     * Delete a resume by ID
     */
    suspend fun deleteResumeById(id: String) {
        resumeDao.deleteResumeById(id)
    }
    
    /**
     * Delete a resume
     */
    suspend fun deleteResume(resume: Resume) {
        resumeDao.deleteResume(resume.toEntity())
    }
    
    /**
     * Delete all resumes
     */
    suspend fun deleteAllResumes() {
        resumeDao.deleteAllResumes()
    }
    
    /**
     * Get count of resumes
     */
    suspend fun getResumeCount(): Int {
        return resumeDao.getResumeCount()
    }
}

/**
 * Alternative constructor for cases where dependency injection is not available
 */
class ResumeRepositoryImpl(
    private val resumeDao: ResumeDao
) : ResumeRepository(resumeDao)
