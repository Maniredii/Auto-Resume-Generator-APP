package fm.mrc.resumebuilder.data.backup

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import fm.mrc.resumebuilder.data.model.Resume
import fm.mrc.resumebuilder.data.repo.ResumeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Manager for JSON backup and restore functionality
 */
class JsonBackupManager(
    private val context: Context,
    private val repository: ResumeRepository
) {
    
    private val gson = Gson()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
    
    /**
     * Export all resumes to JSON file
     */
    suspend fun exportResumesToJson(): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val resumes = repository.getAllResumes()
            val jsonString = gson.toJson(resumes)
            
            val timestamp = dateFormat.format(Date())
            val fileName = "resume_backup_$timestamp.json"
            val file = File(context.cacheDir, fileName)
            
            FileOutputStream(file).use { fos ->
                fos.write(jsonString.toByteArray())
            }
            
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Import resumes from JSON file
     */
    suspend fun importResumesFromJson(uri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val inputStream: InputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(Exception("Could not open file"))
            
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            inputStream.close()
            
            val type = object : TypeToken<List<Resume>>() {}.type
            val resumes: List<Resume> = gson.fromJson(jsonString, type)
            
            var importedCount = 0
            resumes.forEach { resume ->
                try {
                    // Generate new ID to avoid conflicts
                    val newResume = resume.copy(
                        id = UUID.randomUUID().toString(),
                        metadata = resume.metadata.copy(
                            createdAt = java.time.LocalDateTime.now(),
                            updatedAt = java.time.LocalDateTime.now()
                        )
                    )
                    repository.insertResume(newResume)
                    importedCount++
                } catch (e: Exception) {
                    // Log error but continue with other resumes
                    e.printStackTrace()
                }
            }
            
            Result.success(importedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Export single resume to JSON
     */
    suspend fun exportSingleResumeToJson(resumeId: String): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val resume = repository.getResumeById(resumeId)
                ?: return@withContext Result.failure(Exception("Resume not found"))
            
            val jsonString = gson.toJson(resume)
            
            val timestamp = dateFormat.format(Date())
            val fileName = "resume_${resume.personal.fullName.replace(" ", "_")}_$timestamp.json"
            val file = File(context.cacheDir, fileName)
            
            FileOutputStream(file).use { fos ->
                fos.write(jsonString.toByteArray())
            }
            
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Validate JSON file before import
     */
    suspend fun validateJsonFile(uri: Uri): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val inputStream: InputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(Exception("Could not open file"))
            
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            inputStream.close()
            
            val type = object : TypeToken<List<Resume>>() {}.type
            gson.fromJson<List<Resume>>(jsonString, type)
            
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
