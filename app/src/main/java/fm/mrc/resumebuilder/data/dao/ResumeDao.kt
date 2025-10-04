package fm.mrc.resumebuilder.data.dao

import androidx.room.*
import fm.mrc.resumebuilder.data.entity.ResumeEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Resume operations
 */
@Dao
interface ResumeDao {
    
    /**
     * Insert a new resume
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResume(resume: ResumeEntity)
    
    /**
     * Update an existing resume
     */
    @Update
    suspend fun updateResume(resume: ResumeEntity)
    
    /**
     * Get all resumes as a Flow for reactive updates
     */
    @Query("SELECT * FROM resumes ORDER BY id")
    fun getAllResumes(): Flow<List<ResumeEntity>>
    
    /**
     * Get all resumes as a list (for one-time queries)
     */
    @Query("SELECT * FROM resumes ORDER BY id")
    suspend fun getAllResumesList(): List<ResumeEntity>
    
    /**
     * Get a specific resume by ID
     */
    @Query("SELECT * FROM resumes WHERE id = :id")
    suspend fun getResumeById(id: String): ResumeEntity?
    
    /**
     * Get a specific resume by ID as Flow
     */
    @Query("SELECT * FROM resumes WHERE id = :id")
    fun getResumeByIdFlow(id: String): Flow<ResumeEntity?>
    
    /**
     * Delete a resume by ID
     */
    @Query("DELETE FROM resumes WHERE id = :id")
    suspend fun deleteResumeById(id: String)
    
    /**
     * Delete a resume entity
     */
    @Delete
    suspend fun deleteResume(resume: ResumeEntity)
    
    /**
     * Delete all resumes
     */
    @Query("DELETE FROM resumes")
    suspend fun deleteAllResumes()
    
    /**
     * Get count of resumes
     */
    @Query("SELECT COUNT(*) FROM resumes")
    suspend fun getResumeCount(): Int
}
