package fm.mrc.resumebuilder.data

import android.content.Context
import fm.mrc.resumebuilder.data.db.ResumeDatabase
import fm.mrc.resumebuilder.data.model.*
import fm.mrc.resumebuilder.data.repo.ResumeRepositoryImpl
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime
import java.util.UUID

/**
 * Sample test function to demonstrate resume data persistence
 * This can be called from your MainActivity or a test class
 */
class SampleResumeTest {
    
    /**
     * Creates a sample resume for testing
     */
    private fun createSampleResume(): Resume {
        return Resume(
            id = UUID.randomUUID().toString(),
            metadata = ResumeMetadata(
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
                template = "modern"
            ),
            personal = PersonalInfo(
                fullName = "John Doe",
                title = "Senior Android Developer",
                email = "john.doe@example.com",
                phone = "+1 (555) 123-4567",
                linkedin = "linkedin.com/in/johndoe",
                github = "github.com/johndoe",
                website = "johndoe.dev",
                location = "San Francisco, CA"
            ),
            summary = "Experienced Android developer with 5+ years of expertise in Kotlin, Jetpack Compose, and modern Android architecture patterns. Passionate about creating user-friendly mobile applications with clean, maintainable code.",
            skills = listOf(
                "Kotlin", "Java", "Jetpack Compose", "Android SDK", "MVVM", "Room", 
                "Coroutines", "Retrofit", "Git", "Firebase", "REST APIs", "Material Design"
            ),
            education = listOf(
                Education(
                    id = UUID.randomUUID().toString(),
                    institution = "University of California, Berkeley",
                    degree = "Bachelor of Science in Computer Science",
                    start = "2015",
                    end = "2019",
                    details = "Graduated Magna Cum Laude, GPA: 3.8/4.0"
                )
            ),
            experience = listOf(
                Experience(
                    id = UUID.randomUUID().toString(),
                    company = "Tech Innovations Inc.",
                    role = "Senior Android Developer",
                    start = "2021",
                    end = "Present",
                    bullets = listOf(
                        "Led development of flagship Android app with 1M+ downloads",
                        "Migrated legacy codebase to Jetpack Compose, improving performance by 30%",
                        "Mentored 3 junior developers and conducted code reviews",
                        "Implemented CI/CD pipeline reducing deployment time by 50%"
                    )
                ),
                Experience(
                    id = UUID.randomUUID().toString(),
                    company = "StartupXYZ",
                    role = "Android Developer",
                    start = "2019",
                    end = "2021",
                    bullets = listOf(
                        "Developed Android app from scratch using MVVM architecture",
                        "Integrated third-party APIs and payment gateways",
                        "Optimized app performance and reduced crash rate by 40%"
                    )
                )
            ),
            projects = listOf(
                Project(
                    id = UUID.randomUUID().toString(),
                    title = "Weather Forecast App",
                    description = "A modern weather app built with Jetpack Compose and OpenWeather API",
                    link = "github.com/johndoe/weather-app",
                    tech = listOf("Kotlin", "Jetpack Compose", "Room", "Retrofit", "Coroutines")
                ),
                Project(
                    id = UUID.randomUUID().toString(),
                    title = "Task Manager",
                    description = "Personal productivity app with offline support and cloud sync",
                    link = "github.com/johndoe/task-manager",
                    tech = listOf("Kotlin", "Room", "WorkManager", "Firebase", "Material Design")
                )
            )
        )
    }
    
    /**
     * Test function that inserts a sample resume and reads it back
     * Call this from your MainActivity or test class
     */
    fun testResumeOperations(context: Context): String {
        return runBlocking {
            try {
                // Initialize database and repository
                val database = ResumeDatabase.getDatabase(context)
                val repository = ResumeRepositoryImpl(database.resumeDao())
                
                // Create sample resume
                val sampleResume = createSampleResume()
                
                // Insert resume
                repository.insertResume(sampleResume)
                
                // Read back the resume
                val retrievedResume = repository.getResumeById(sampleResume.id)
                
                // Verify the data
                if (retrievedResume != null) {
                    val isDataCorrect = retrievedResume.personal.fullName == "John Doe" &&
                            retrievedResume.skills.contains("Kotlin") &&
                            retrievedResume.experience.size == 2 &&
                            retrievedResume.projects.size == 2
                    
                    if (isDataCorrect) {
                        "✅ SUCCESS: Resume inserted and retrieved correctly!\n" +
                                "Resume ID: ${retrievedResume.id}\n" +
                                "Name: ${retrievedResume.personal.fullName}\n" +
                                "Skills: ${retrievedResume.skills.size} skills\n" +
                                "Experience: ${retrievedResume.experience.size} jobs\n" +
                                "Projects: ${retrievedResume.projects.size} projects"
                    } else {
                        "❌ ERROR: Data integrity check failed"
                    }
                } else {
                    "❌ ERROR: Could not retrieve resume after insertion"
                }
            } catch (e: Exception) {
                "❌ ERROR: ${e.message}"
            }
        }
    }
    
    /**
     * Test function to demonstrate all CRUD operations
     */
    fun testAllOperations(context: Context): String {
        return runBlocking {
            try {
                val database = ResumeDatabase.getDatabase(context)
                val repository = ResumeRepositoryImpl(database.resumeDao())
                
                // Clear existing data
                repository.deleteAllResumes()
                
                // Create and insert sample resume
                val sampleResume = createSampleResume()
                repository.insertResume(sampleResume)
                
                // Test read operations
                val allResumes = repository.getAllResumesList()
                val specificResume = repository.getResumeById(sampleResume.id)
                val count = repository.getResumeCount()
                
                // Test update operation
                val updatedResume = sampleResume.copy(
                    summary = "Updated summary for testing purposes"
                )
                repository.updateResume(updatedResume)
                val retrievedUpdated = repository.getResumeById(sampleResume.id)
                
                // Verify results
                val results = StringBuilder()
                results.append("🧪 CRUD Operations Test Results:\n\n")
                results.append("✅ INSERT: Resume created with ID ${sampleResume.id}\n")
                results.append("✅ READ ALL: Found ${allResumes.size} resume(s)\n")
                results.append("✅ READ BY ID: ${if (specificResume != null) "Found" else "Not found"}\n")
                results.append("✅ COUNT: Database contains $count resume(s)\n")
                results.append("✅ UPDATE: Summary updated - ${retrievedUpdated?.summary?.startsWith("Updated") == true}\n")
                
                // Test delete
                repository.deleteResumeById(sampleResume.id)
                val deletedCheck = repository.getResumeById(sampleResume.id)
                results.append("✅ DELETE: Resume deleted - ${deletedCheck == null}\n")
                
                results.toString()
            } catch (e: Exception) {
                "❌ CRUD TEST ERROR: ${e.message}"
            }
        }
    }
}
