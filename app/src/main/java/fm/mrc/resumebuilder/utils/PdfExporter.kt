package fm.mrc.resumebuilder.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.core.content.FileProvider
import fm.mrc.resumebuilder.data.model.Resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility class for exporting resumes to PDF format
 */
class PdfExporter {
    
    companion object {
        private const val PAGE_WIDTH = 595 // A4 width in points (8.27 inches * 72 DPI)
        private const val PAGE_HEIGHT = 842 // A4 height in points (11.69 inches * 72 DPI)
        private const val MARGIN_LEFT = 50
        private const val MARGIN_RIGHT = 50
        private const val MARGIN_TOP = 50
        private const val MARGIN_BOTTOM = 50
        private const val CONTENT_WIDTH = PAGE_WIDTH - MARGIN_LEFT - MARGIN_RIGHT
        
        // Text sizes
        private const val TITLE_SIZE = 24f
        private const val SUBTITLE_SIZE = 18f
        private const val SECTION_HEADER_SIZE = 16f
        private const val BODY_SIZE = 12f
        private const val SMALL_SIZE = 10f
        
        // Line spacing
        private const val LINE_SPACING = 4f
        private const val SECTION_SPACING = 20f
        private const val PARAGRAPH_SPACING = 8f
    }
    
    /**
     * Exports a resume to PDF format
     * @param context Android context
     * @param resume Resume data to export
     * @return Uri pointing to the generated PDF file
     */
    suspend fun exportResumeToPdf(context: Context, resume: Resume): Uri {
        return withContext(Dispatchers.IO) {
            val pdfDocument = PdfDocument()
            var currentPage = 1
            var currentY = MARGIN_TOP.toFloat()
            
            // Start first page
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, currentPage).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas
            
            // Create paint objects for different text styles
            val titlePaint = createPaint(TITLE_SIZE, Typeface.BOLD)
            val subtitlePaint = createPaint(SUBTITLE_SIZE, Typeface.NORMAL)
            val sectionHeaderPaint = createPaint(SECTION_HEADER_SIZE, Typeface.BOLD)
            val bodyPaint = createPaint(BODY_SIZE, Typeface.NORMAL)
            val boldBodyPaint = createPaint(BODY_SIZE, Typeface.BOLD)
            val smallPaint = createPaint(SMALL_SIZE, Typeface.NORMAL)
            
            // Helper function to check if we need a new page
            fun checkNewPage(neededHeight: Float): Pair<PdfDocument.Page, Canvas> {
                if (currentY + neededHeight > PAGE_HEIGHT - MARGIN_BOTTOM) {
                    pdfDocument.finishPage(page)
                    currentPage++
                    val newPageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, currentPage).create()
                    page = pdfDocument.startPage(newPageInfo)
                    canvas = page.canvas
                    currentY = MARGIN_TOP.toFloat()
                }
                return Pair(page, canvas)
            }
            
            // Draw header section
            currentY = drawHeader(context, canvas, resume, titlePaint, subtitlePaint, smallPaint, currentY)
            currentY += SECTION_SPACING
            
            // Draw summary section
            if (resume.summary.isNotBlank()) {
                val (newPage, newCanvas) = checkNewPage(60f)
                page = newPage
                canvas = newCanvas
                currentY = drawSection(canvas, "PROFESSIONAL SUMMARY", resume.summary, 
                    sectionHeaderPaint, bodyPaint, currentY)
                currentY += SECTION_SPACING
            }
            
            // Draw skills section
            if (resume.skills.isNotEmpty()) {
                val (newPage, newCanvas) = checkNewPage(80f)
                page = newPage
                canvas = newCanvas
                currentY = drawSkillsSection(canvas, resume.skills, sectionHeaderPaint, bodyPaint, currentY)
                currentY += SECTION_SPACING
            }
            
            // Draw experience section
            if (resume.experience.isNotEmpty()) {
                val (newPage, newCanvas) = checkNewPage(100f)
                page = newPage
                canvas = newCanvas
                currentY = drawExperienceSection(canvas, resume.experience, sectionHeaderPaint, 
                    boldBodyPaint, bodyPaint, smallPaint, currentY) { height ->
                    val (p, c) = checkNewPage(height)
                    page = p
                    canvas = c
                    currentY = MARGIN_TOP.toFloat()
                    currentY
                }
                currentY += SECTION_SPACING
            }
            
            // Draw education section
            if (resume.education.isNotEmpty()) {
                val (newPage, newCanvas) = checkNewPage(80f)
                page = newPage
                canvas = newCanvas
                currentY = drawEducationSection(canvas, resume.education, sectionHeaderPaint, 
                    boldBodyPaint, bodyPaint, smallPaint, currentY) { height ->
                    val (p, c) = checkNewPage(height)
                    page = p
                    canvas = c
                    currentY = MARGIN_TOP.toFloat()
                    currentY
                }
                currentY += SECTION_SPACING
            }
            
            // Draw projects section
            if (resume.projects.isNotEmpty()) {
                val (newPage, newCanvas) = checkNewPage(100f)
                page = newPage
                canvas = newCanvas
                currentY = drawProjectsSection(canvas, resume.projects, sectionHeaderPaint, 
                    boldBodyPaint, bodyPaint, smallPaint, currentY) { height ->
                    val (p, c) = checkNewPage(height)
                    page = p
                    canvas = c
                    currentY = MARGIN_TOP.toFloat()
                    currentY
                }
            }
            
            // Finish the last page
            pdfDocument.finishPage(page)
            
            // Save to file
            val fileName = "${resume.personal.fullName.ifBlank { "Resume" }}_${
                SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            }.pdf"
            
            val file = File(context.cacheDir, fileName)
            FileOutputStream(file).use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
            pdfDocument.close()
            
            // Return content URI via FileProvider
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        }
    }
    
    /**
     * Opens Android share sheet with the PDF file
     */
    fun shareResumePdf(context: Context, pdfUri: Uri, resumeName: String) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, pdfUri)
            putExtra(Intent.EXTRA_SUBJECT, "$resumeName - Resume")
            putExtra(Intent.EXTRA_TEXT, "Please find my resume attached.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        val chooser = Intent.createChooser(shareIntent, "Share Resume")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    /**
     * Share resume via email with pre-filled content
     */
    fun shareResumeViaEmail(context: Context, pdfUri: Uri, resumeName: String, recipientEmail: String = "") {
        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, if (recipientEmail.isNotEmpty()) arrayOf(recipientEmail) else null)
            putExtra(Intent.EXTRA_SUBJECT, "$resumeName - Resume")
            putExtra(Intent.EXTRA_TEXT, "Dear Hiring Manager,\n\nI am writing to express my interest in the position. Please find my resume attached for your review.\n\nBest regards,\n$resumeName")
            putExtra(Intent.EXTRA_STREAM, pdfUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        val chooser = Intent.createChooser(emailIntent, "Send Resume via Email")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    /**
     * Share resume via WhatsApp
     */
    fun shareResumeViaWhatsApp(context: Context, pdfUri: Uri, resumeName: String) {
        val whatsappIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            setPackage("com.whatsapp")
            putExtra(Intent.EXTRA_STREAM, pdfUri)
            putExtra(Intent.EXTRA_TEXT, "Hi! Please find my resume attached. $resumeName")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        try {
            context.startActivity(whatsappIntent)
        } catch (e: Exception) {
            // Fallback to general share if WhatsApp is not installed
            shareResumePdf(context, pdfUri, resumeName)
        }
    }

    /**
     * Share resume via LinkedIn
     */
    fun shareResumeViaLinkedIn(context: Context, pdfUri: Uri, resumeName: String) {
        val linkedinIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            setPackage("com.linkedin.android")
            putExtra(Intent.EXTRA_STREAM, pdfUri)
            putExtra(Intent.EXTRA_TEXT, "Sharing my resume: $resumeName")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        try {
            context.startActivity(linkedinIntent)
        } catch (e: Exception) {
            // Fallback to general share if LinkedIn is not installed
            shareResumePdf(context, pdfUri, resumeName)
        }
    }

    /**
     * Share resume via Telegram
     */
    fun shareResumeViaTelegram(context: Context, pdfUri: Uri, resumeName: String) {
        val telegramIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            setPackage("org.telegram.messenger")
            putExtra(Intent.EXTRA_STREAM, pdfUri)
            putExtra(Intent.EXTRA_TEXT, "My resume: $resumeName")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        try {
            context.startActivity(telegramIntent)
        } catch (e: Exception) {
            // Fallback to general share if Telegram is not installed
            shareResumePdf(context, pdfUri, resumeName)
        }
    }

    /**
     * Get available sharing options based on installed apps
     */
    fun getAvailableSharingOptions(context: Context): List<SharingOption> {
        val options = mutableListOf<SharingOption>()
        
        // Always available - general share
        options.add(SharingOption("General Share", "Share via any app", Icons.Default.Share))
        
        // Check for specific apps
        val packageManager = context.packageManager
        
        if (isAppInstalled(packageManager, "com.whatsapp")) {
            options.add(SharingOption("WhatsApp", "Share via WhatsApp", Icons.Default.Phone))
        }
        
        if (isAppInstalled(packageManager, "com.linkedin.android")) {
            options.add(SharingOption("LinkedIn", "Share via LinkedIn", Icons.Default.Person))
        }
        
        if (isAppInstalled(packageManager, "org.telegram.messenger")) {
            options.add(SharingOption("Telegram", "Share via Telegram", Icons.Default.Phone))
        }
        
        if (isAppInstalled(packageManager, "com.google.android.gm")) {
            options.add(SharingOption("Gmail", "Share via Gmail", Icons.Default.Email))
        }
        
        return options
    }

    private fun isAppInstalled(packageManager: android.content.pm.PackageManager, packageName: String): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: android.content.pm.PackageManager.NameNotFoundException) {
            false
        }
    }

    data class SharingOption(
        val name: String,
        val description: String,
        val icon: androidx.compose.ui.graphics.vector.ImageVector
    )
    
    private fun createPaint(textSize: Float, typeface: Int): Paint {
        return Paint().apply {
            this.textSize = textSize
            this.typeface = Typeface.create(Typeface.DEFAULT, typeface)
            color = android.graphics.Color.BLACK
            isAntiAlias = true
        }
    }
    
    private fun drawHeader(
        context: Context,
        canvas: Canvas,
        resume: Resume,
        titlePaint: Paint,
        subtitlePaint: Paint,
        smallPaint: Paint,
        startY: Float
    ): Float {
        var currentY = startY
        
        // Avatar (if available)
        if (resume.personal.avatarUri != null) {
            try {
                val avatarBitmap = loadBitmapFromUri(context, resume.personal.avatarUri)
                if (avatarBitmap != null) {
                    val avatarSize = 80f // Size in points
                    val avatarX = MARGIN_LEFT + (CONTENT_WIDTH - avatarSize) / 2
                    val avatarRect = Rect(
                        avatarX.toInt(),
                        currentY.toInt(),
                        (avatarX + avatarSize).toInt(),
                        (currentY + avatarSize).toInt()
                    )
                    
                    // Create circular avatar
                    val circularBitmap = createCircularBitmap(avatarBitmap, avatarSize.toInt())
                    canvas.drawBitmap(circularBitmap, null, avatarRect, null)
                    currentY += avatarSize + PARAGRAPH_SPACING
                }
            } catch (e: Exception) {
                // If avatar loading fails, continue without it
                e.printStackTrace()
            }
        }
        
        // Name
        val nameWidth = titlePaint.measureText(resume.personal.fullName)
        val nameX = MARGIN_LEFT + (CONTENT_WIDTH - nameWidth) / 2
        canvas.drawText(resume.personal.fullName, nameX, currentY, titlePaint)
        currentY += titlePaint.textSize + LINE_SPACING
        
        // Title
        if (resume.personal.title.isNotBlank()) {
            val titleWidth = subtitlePaint.measureText(resume.personal.title)
            val titleX = MARGIN_LEFT + (CONTENT_WIDTH - titleWidth) / 2
            canvas.drawText(resume.personal.title, titleX, currentY, subtitlePaint)
            currentY += subtitlePaint.textSize + LINE_SPACING
        }
        
        currentY += PARAGRAPH_SPACING
        
        // Contact information
        val contactInfo = mutableListOf<String>()
        if (resume.personal.email.isNotBlank()) contactInfo.add(resume.personal.email)
        if (resume.personal.phone.isNotBlank()) contactInfo.add(resume.personal.phone)
        if (resume.personal.location.isNotBlank()) contactInfo.add(resume.personal.location)
        
        if (contactInfo.isNotEmpty()) {
            val contactLine = contactInfo.joinToString(" • ")
            val contactWidth = smallPaint.measureText(contactLine)
            val contactX = MARGIN_LEFT + (CONTENT_WIDTH - contactWidth) / 2
            canvas.drawText(contactLine, contactX, currentY, smallPaint)
            currentY += smallPaint.textSize + LINE_SPACING
        }
        
        // Links
        val links = mutableListOf<String>()
        if (resume.personal.linkedin.isNotBlank()) links.add(resume.personal.linkedin)
        if (resume.personal.github.isNotBlank()) links.add(resume.personal.github)
        if (resume.personal.website.isNotBlank()) links.add(resume.personal.website)
        
        if (links.isNotEmpty()) {
            val linksLine = links.joinToString(" • ")
            val linksWidth = smallPaint.measureText(linksLine)
            val linksX = MARGIN_LEFT + (CONTENT_WIDTH - linksWidth) / 2
            canvas.drawText(linksLine, linksX, currentY, smallPaint)
            currentY += smallPaint.textSize + LINE_SPACING
        }
        
        // Draw separator line
        currentY += PARAGRAPH_SPACING
        canvas.drawLine(
            MARGIN_LEFT.toFloat(),
            currentY,
            (PAGE_WIDTH - MARGIN_RIGHT).toFloat(),
            currentY,
            smallPaint
        )
        currentY += PARAGRAPH_SPACING
        
        return currentY
    }
    
    private fun drawSection(
        canvas: Canvas,
        title: String,
        content: String,
        headerPaint: Paint,
        bodyPaint: Paint,
        startY: Float
    ): Float {
        var currentY = startY
        
        // Section header
        canvas.drawText(title, MARGIN_LEFT.toFloat(), currentY, headerPaint)
        currentY += headerPaint.textSize + PARAGRAPH_SPACING
        
        // Content - wrap text if needed
        val lines = wrapText(content, bodyPaint, CONTENT_WIDTH)
        for (line in lines) {
            canvas.drawText(line, MARGIN_LEFT.toFloat(), currentY, bodyPaint)
            currentY += bodyPaint.textSize + LINE_SPACING
        }
        
        return currentY
    }
    
    private fun drawSkillsSection(
        canvas: Canvas,
        skills: List<String>,
        headerPaint: Paint,
        bodyPaint: Paint,
        startY: Float
    ): Float {
        var currentY = startY
        
        // Section header
        canvas.drawText("SKILLS", MARGIN_LEFT.toFloat(), currentY, headerPaint)
        currentY += headerPaint.textSize + PARAGRAPH_SPACING
        
        // Skills as comma-separated text
        val skillsText = skills.joinToString(", ")
        val lines = wrapText(skillsText, bodyPaint, CONTENT_WIDTH)
        for (line in lines) {
            canvas.drawText(line, MARGIN_LEFT.toFloat(), currentY, bodyPaint)
            currentY += bodyPaint.textSize + LINE_SPACING
        }
        
        return currentY
    }
    
    private fun drawExperienceSection(
        canvas: Canvas,
        experiences: List<fm.mrc.resumebuilder.data.model.Experience>,
        headerPaint: Paint,
        boldPaint: Paint,
        bodyPaint: Paint,
        smallPaint: Paint,
        startY: Float,
        checkNewPage: (Float) -> Float
    ): Float {
        var currentY = startY
        
        // Section header
        canvas.drawText("WORK EXPERIENCE", MARGIN_LEFT.toFloat(), currentY, headerPaint)
        currentY += headerPaint.textSize + PARAGRAPH_SPACING
        
        for (experience in experiences) {
            // Check if we need space for this experience entry
            val estimatedHeight = boldPaint.textSize * 2 + smallPaint.textSize + 
                    (experience.bullets.size * bodyPaint.textSize) + 40f
            if (currentY + estimatedHeight > PAGE_HEIGHT - MARGIN_BOTTOM) {
                currentY = checkNewPage(estimatedHeight)
            }
            
            // Role
            canvas.drawText(experience.role, MARGIN_LEFT.toFloat(), currentY, boldPaint)
            currentY += boldPaint.textSize + LINE_SPACING
            
            // Company and dates
            val companyDate = "${experience.company} • ${experience.start} - ${experience.end}"
            canvas.drawText(companyDate, MARGIN_LEFT.toFloat(), currentY, smallPaint)
            currentY += smallPaint.textSize + PARAGRAPH_SPACING
            
            // Bullets
            for (bullet in experience.bullets) {
                val bulletText = "• $bullet"
                val lines = wrapText(bulletText, bodyPaint, CONTENT_WIDTH - 20)
                for ((index, line) in lines.withIndex()) {
                    val x = if (index == 0) MARGIN_LEFT.toFloat() else MARGIN_LEFT + 20f
                    canvas.drawText(line, x, currentY, bodyPaint)
                    currentY += bodyPaint.textSize + LINE_SPACING
                }
            }
            
            currentY += PARAGRAPH_SPACING
        }
        
        return currentY
    }
    
    private fun drawEducationSection(
        canvas: Canvas,
        educations: List<fm.mrc.resumebuilder.data.model.Education>,
        headerPaint: Paint,
        boldPaint: Paint,
        bodyPaint: Paint,
        smallPaint: Paint,
        startY: Float,
        checkNewPage: (Float) -> Float
    ): Float {
        var currentY = startY
        
        // Section header
        canvas.drawText("EDUCATION", MARGIN_LEFT.toFloat(), currentY, headerPaint)
        currentY += headerPaint.textSize + PARAGRAPH_SPACING
        
        for (education in educations) {
            // Check if we need space for this education entry
            val estimatedHeight = boldPaint.textSize + smallPaint.textSize + bodyPaint.textSize + 30f
            if (currentY + estimatedHeight > PAGE_HEIGHT - MARGIN_BOTTOM) {
                currentY = checkNewPage(estimatedHeight)
            }
            
            // Degree
            canvas.drawText(education.degree, MARGIN_LEFT.toFloat(), currentY, boldPaint)
            currentY += boldPaint.textSize + LINE_SPACING
            
            // Institution and dates
            val institutionDate = "${education.institution} • ${education.start} - ${education.end}"
            canvas.drawText(institutionDate, MARGIN_LEFT.toFloat(), currentY, smallPaint)
            currentY += smallPaint.textSize + LINE_SPACING
            
            // Details
            if (education.details.isNotBlank()) {
                val lines = wrapText(education.details, bodyPaint, CONTENT_WIDTH)
                for (line in lines) {
                    canvas.drawText(line, MARGIN_LEFT.toFloat(), currentY, bodyPaint)
                    currentY += bodyPaint.textSize + LINE_SPACING
                }
            }
            
            currentY += PARAGRAPH_SPACING
        }
        
        return currentY
    }
    
    private fun drawProjectsSection(
        canvas: Canvas,
        projects: List<fm.mrc.resumebuilder.data.model.Project>,
        headerPaint: Paint,
        boldPaint: Paint,
        bodyPaint: Paint,
        smallPaint: Paint,
        startY: Float,
        checkNewPage: (Float) -> Float
    ): Float {
        var currentY = startY
        
        // Section header
        canvas.drawText("PROJECTS", MARGIN_LEFT.toFloat(), currentY, headerPaint)
        currentY += headerPaint.textSize + PARAGRAPH_SPACING
        
        for (project in projects) {
            // Check if we need space for this project entry
            val estimatedHeight = boldPaint.textSize + smallPaint.textSize + bodyPaint.textSize * 3 + 40f
            if (currentY + estimatedHeight > PAGE_HEIGHT - MARGIN_BOTTOM) {
                currentY = checkNewPage(estimatedHeight)
            }
            
            // Project title
            canvas.drawText(project.title, MARGIN_LEFT.toFloat(), currentY, boldPaint)
            currentY += boldPaint.textSize + LINE_SPACING
            
            // Link
            if (project.link.isNotBlank()) {
                canvas.drawText(project.link, MARGIN_LEFT.toFloat(), currentY, smallPaint)
                currentY += smallPaint.textSize + LINE_SPACING
            }
            
            // Description
            if (project.description.isNotBlank()) {
                val lines = wrapText(project.description, bodyPaint, CONTENT_WIDTH)
                for (line in lines) {
                    canvas.drawText(line, MARGIN_LEFT.toFloat(), currentY, bodyPaint)
                    currentY += bodyPaint.textSize + LINE_SPACING
                }
            }
            
            // Technologies
            if (project.tech.isNotEmpty()) {
                val techText = "Technologies: ${project.tech.joinToString(", ")}"
                val lines = wrapText(techText, smallPaint, CONTENT_WIDTH)
                for (line in lines) {
                    canvas.drawText(line, MARGIN_LEFT.toFloat(), currentY, smallPaint)
                    currentY += smallPaint.textSize + LINE_SPACING
                }
            }
            
            currentY += PARAGRAPH_SPACING
        }
        
        return currentY
    }
    
    private fun wrapText(text: String, paint: Paint, maxWidth: Int): List<String> {
        val lines = mutableListOf<String>()
        val words = text.split(" ")
        var currentLine = ""
        
        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            val testWidth = paint.measureText(testLine)
            
            if (testWidth <= maxWidth) {
                currentLine = testLine
            } else {
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine)
                    currentLine = word
                } else {
                    // Single word is too long, just add it
                    lines.add(word)
                    currentLine = ""
                }
            }
        }
        
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
        }
        
        return lines.ifEmpty { listOf("") }
    }
    
    /**
     * Load bitmap from URI
     */
    private fun loadBitmapFromUri(context: Context, uriString: String): Bitmap? {
        return try {
            val uri = Uri.parse(uriString)
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Create circular bitmap from original bitmap
     */
    private fun createCircularBitmap(bitmap: Bitmap, size: Int): Bitmap {
        val circularBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(circularBitmap)
        
        val paint = Paint().apply {
            isAntiAlias = true
        }
        
        // Draw circle
        val radius = size / 2f
        canvas.drawCircle(radius, radius, radius, paint)
        
        // Use source bitmap as mask
        paint.xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN)
        
        // Scale and draw the original bitmap
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, size, size, true)
        canvas.drawBitmap(scaledBitmap, 0f, 0f, paint)
        
        return circularBitmap
    }
}
