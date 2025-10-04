package fm.mrc.resumebuilder.data.analytics

import fm.mrc.resumebuilder.data.model.Resume

/**
 * Resume analytics and scoring system
 */
object ResumeAnalytics {
    
    /**
     * Resume score data class
     */
    data class ResumeScore(
        val overallScore: Int,
        val completenessScore: Int,
        val contentScore: Int,
        val formattingScore: Int,
        val atsScore: Int,
        val suggestions: List<String>,
        val strengths: List<String>,
        val weaknesses: List<String>
    )
    
    /**
     * Calculate overall resume score
     */
    fun calculateResumeScore(resume: Resume): ResumeScore {
        val completenessScore = calculateCompletenessScore(resume)
        val contentScore = calculateContentScore(resume)
        val formattingScore = calculateFormattingScore(resume)
        val atsScore = calculateATSScore(resume)
        
        val overallScore = (completenessScore + contentScore + formattingScore + atsScore) / 4
        
        val suggestions = generateSuggestions(resume, completenessScore, contentScore, formattingScore, atsScore)
        val strengths = generateStrengths(resume, completenessScore, contentScore, formattingScore, atsScore)
        val weaknesses = generateWeaknesses(resume, completenessScore, contentScore, formattingScore, atsScore)
        
        return ResumeScore(
            overallScore = overallScore,
            completenessScore = completenessScore,
            contentScore = contentScore,
            formattingScore = formattingScore,
            atsScore = atsScore,
            suggestions = suggestions,
            strengths = strengths,
            weaknesses = weaknesses
        )
    }
    
    /**
     * Calculate completeness score (0-100)
     */
    private fun calculateCompletenessScore(resume: Resume): Int {
        var score = 0
        var totalFields = 0
        
        // Personal Information (40 points)
        totalFields += 8
        if (resume.personal.fullName.isNotBlank()) score += 5
        if (resume.personal.email.isNotBlank()) score += 5
        if (resume.personal.phone.isNotBlank()) score += 5
        if (resume.personal.title.isNotBlank()) score += 5
        if (resume.personal.location.isNotBlank()) score += 5
        if (resume.personal.linkedin.isNotBlank()) score += 5
        if (resume.personal.github.isNotBlank()) score += 5
        if (resume.personal.website.isNotBlank()) score += 5
        
        // Summary (20 points)
        totalFields += 1
        if (resume.summary.isNotBlank()) {
            score += 20
        }
        
        // Skills (10 points)
        totalFields += 1
        if (resume.skills.isNotEmpty()) {
            score += 10
        }
        
        // Experience (20 points)
        totalFields += 1
        if (resume.experience.isNotEmpty()) {
            score += 20
        }
        
        // Education (10 points)
        totalFields += 1
        if (resume.education.isNotEmpty()) {
            score += 10
        }
        
        return (score * 100) / 100 // Max 100 points
    }
    
    /**
     * Calculate content quality score (0-100)
     */
    private fun calculateContentScore(resume: Resume): Int {
        var score = 0
        
        // Summary quality (30 points)
        if (resume.summary.length >= 50) score += 10
        if (resume.summary.length >= 100) score += 10
        if (resume.summary.length <= 300) score += 10
        
        // Skills quality (20 points)
        if (resume.skills.size >= 5) score += 10
        if (resume.skills.size <= 15) score += 10
        
        // Experience quality (30 points)
        if (resume.experience.isNotEmpty()) {
            val avgBullets = resume.experience.map { it.bullets.size }.average()
            if (avgBullets >= 2) score += 15
            if (avgBullets <= 5) score += 15
        }
        
        // Education quality (20 points)
        if (resume.education.isNotEmpty()) {
            score += 20
        }
        
        return score
    }
    
    /**
     * Calculate formatting score (0-100)
     */
    private fun calculateFormattingScore(resume: Resume): Int {
        var score = 100
        
        // Check for common formatting issues
        if (resume.personal.fullName.contains("  ")) score -= 10 // Double spaces
        if (resume.summary.contains("  ")) score -= 10
        if (resume.personal.email.contains(" ")) score -= 15 // Spaces in email
        if (resume.personal.phone.contains("  ")) score -= 10
        
        // Check for proper capitalization
        if (!resume.personal.fullName.matches(Regex("^[A-Z][a-z]+ [A-Z][a-z]+"))) {
            score -= 5
        }
        
        return maxOf(0, score)
    }
    
    /**
     * Calculate ATS (Applicant Tracking System) score (0-100)
     */
    private fun calculateATSScore(resume: Resume): Int {
        var score = 0
        
        // Keywords and formatting
        val keywords = listOf("experience", "skills", "education", "achievement", "project", "leadership")
        val resumeText = "${resume.summary} ${resume.skills.joinToString(" ")}".lowercase()
        
        keywords.forEach { keyword ->
            if (resumeText.contains(keyword)) score += 10
        }
        
        // Contact information
        if (resume.personal.email.isNotBlank()) score += 10
        if (resume.personal.phone.isNotBlank()) score += 10
        
        // Professional summary
        if (resume.summary.length >= 50) score += 15
        
        // Skills section
        if (resume.skills.size >= 3) score += 15
        
        // Experience with achievements
        if (resume.experience.isNotEmpty()) {
            val hasAchievements = resume.experience.any { it.bullets.isNotEmpty() }
            if (hasAchievements) score += 20
        }
        
        return minOf(100, score)
    }
    
    /**
     * Generate improvement suggestions
     */
    private fun generateSuggestions(
        resume: Resume,
        completenessScore: Int,
        contentScore: Int,
        formattingScore: Int,
        atsScore: Int
    ): List<String> {
        val suggestions = mutableListOf<String>()
        
        if (completenessScore < 80) {
            suggestions.add("Complete all required fields in personal information")
        }
        
        if (resume.summary.length < 50) {
            suggestions.add("Write a more detailed professional summary (at least 50 characters)")
        }
        
        if (resume.skills.size < 5) {
            suggestions.add("Add more relevant skills (aim for 5-15 skills)")
        }
        
        if (resume.experience.isEmpty()) {
            suggestions.add("Add your work experience")
        }
        
        if (resume.education.isEmpty()) {
            suggestions.add("Add your educational background")
        }
        
        if (formattingScore < 90) {
            suggestions.add("Check for formatting issues like double spaces or incorrect capitalization")
        }
        
        if (atsScore < 70) {
            suggestions.add("Include more relevant keywords for ATS optimization")
        }
        
        if (resume.personal.avatarUri == null) {
            suggestions.add("Consider adding a professional photo")
        }
        
        return suggestions
    }
    
    /**
     * Generate strengths
     */
    private fun generateStrengths(
        resume: Resume,
        completenessScore: Int,
        contentScore: Int,
        formattingScore: Int,
        atsScore: Int
    ): List<String> {
        val strengths = mutableListOf<String>()
        
        if (completenessScore >= 90) {
            strengths.add("Complete personal information")
        }
        
        if (resume.summary.length >= 100) {
            strengths.add("Detailed professional summary")
        }
        
        if (resume.skills.size >= 10) {
            strengths.add("Comprehensive skills list")
        }
        
        if (resume.experience.size >= 3) {
            strengths.add("Extensive work experience")
        }
        
        if (formattingScore >= 95) {
            strengths.add("Excellent formatting")
        }
        
        if (atsScore >= 80) {
            strengths.add("ATS-optimized content")
        }
        
        if (resume.personal.avatarUri != null) {
            strengths.add("Professional photo included")
        }
        
        return strengths
    }
    
    /**
     * Generate weaknesses
     */
    private fun generateWeaknesses(
        resume: Resume,
        completenessScore: Int,
        contentScore: Int,
        formattingScore: Int,
        atsScore: Int
    ): List<String> {
        val weaknesses = mutableListOf<String>()
        
        if (completenessScore < 70) {
            weaknesses.add("Incomplete information")
        }
        
        if (resume.summary.length < 30) {
            weaknesses.add("Brief professional summary")
        }
        
        if (resume.skills.size < 3) {
            weaknesses.add("Limited skills listed")
        }
        
        if (resume.experience.isEmpty()) {
            weaknesses.add("No work experience")
        }
        
        if (formattingScore < 80) {
            weaknesses.add("Formatting issues present")
        }
        
        if (atsScore < 60) {
            weaknesses.add("Poor ATS optimization")
        }
        
        return weaknesses
    }
    
    /**
     * Get score color based on score value
     */
    fun getScoreColor(score: Int): String {
        return when {
            score >= 90 -> "Excellent"
            score >= 80 -> "Good"
            score >= 70 -> "Fair"
            score >= 60 -> "Needs Improvement"
            else -> "Poor"
        }
    }
    
    /**
     * Get score emoji based on score value
     */
    fun getScoreEmoji(score: Int): String {
        return when {
            score >= 90 -> "🌟"
            score >= 80 -> "👍"
            score >= 70 -> "👌"
            score >= 60 -> "⚠️"
            else -> "❌"
        }
    }
}
