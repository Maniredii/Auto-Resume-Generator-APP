package fm.mrc.resumebuilder.utils

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.FileProvider
import fm.mrc.resumebuilder.data.model.Resume
import fm.mrc.resumebuilder.data.pdf.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Enhanced PDF exporter with customization options
 */
class EnhancedPdfExporter {
    
    /**
     * Export resume to PDF with customization
     */
    suspend fun exportResumeToPdf(
        context: Context, 
        resume: Resume,
        customization: PdfCustomization = PdfCustomization()
    ): Uri {
        return withContext(Dispatchers.IO) {
            val pdfDocument = PdfDocument()
            var currentPage = 1
            var currentY = customization.margins.top
            
            // Calculate page dimensions based on orientation
            val pageWidth = if (customization.orientation == PageOrientation.LANDSCAPE) {
                customization.pageSize.height
            } else {
                customization.pageSize.width
            }
            val pageHeight = if (customization.orientation == PageOrientation.LANDSCAPE) {
                customization.pageSize.width
            } else {
                customization.pageSize.height
            }
            
            val contentWidth = pageWidth - customization.margins.left - customization.margins.right
            
            // Start first page
            val pageInfo = PdfDocument.PageInfo.Builder(
                pageWidth.toInt(), 
                pageHeight.toInt(), 
                currentPage
            ).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas
            
            // Create paint objects with customization
            val titlePaint = createCustomPaint(
                customization.fontSettings.titleSize,
                Typeface.BOLD,
                customization.colorScheme.textColor
            )
            val subtitlePaint = createCustomPaint(
                customization.fontSettings.subtitleSize,
                Typeface.NORMAL,
                customization.colorScheme.lightTextColor
            )
            val sectionHeaderPaint = createCustomPaint(
                customization.fontSettings.sectionHeaderSize,
                Typeface.BOLD,
                customization.colorScheme.primaryColor
            )
            val bodyPaint = createCustomPaint(
                customization.fontSettings.bodySize,
                Typeface.NORMAL,
                customization.colorScheme.textColor
            )
            val smallPaint = createCustomPaint(
                customization.fontSettings.smallSize,
                Typeface.NORMAL,
                customization.colorScheme.lightTextColor
            )
            
            // Helper function to check if we need a new page
            fun checkNewPage(neededHeight: Float): Pair<PdfDocument.Page, Canvas> {
                if (currentY + neededHeight > pageHeight - customization.margins.bottom) {
                    // Add footer to current page
                    drawFooter(canvas, customization, pageWidth, pageHeight, currentPage)
                    
                    pdfDocument.finishPage(page)
                    currentPage++
                    val newPageInfo = PdfDocument.PageInfo.Builder(
                        pageWidth.toInt(), 
                        pageHeight.toInt(), 
                        currentPage
                    ).create()
                    page = pdfDocument.startPage(newPageInfo)
                    canvas = page.canvas
                    currentY = customization.margins.top
                    
                    // Add header to new page
                    drawHeader(canvas, customization, pageWidth, currentPage)
                }
                return Pair(page, canvas)
            }
            
            // Draw header on first page
            drawHeader(canvas, customization, pageWidth, currentPage)
            
            // Draw resume header section
            currentY = drawResumeHeader(
                canvas, 
                resume, 
                titlePaint, 
                subtitlePaint, 
                smallPaint, 
                currentY,
                customization,
                contentWidth
            )
            currentY += 20f
            
            // Draw summary section
            if (resume.summary.isNotBlank()) {
                val (newPage, newCanvas) = checkNewPage(60f)
                page = newPage
                canvas = newCanvas
                currentY = drawSummarySection(
                    canvas, 
                    resume.summary, 
                    sectionHeaderPaint, 
                    bodyPaint, 
                    currentY,
                    customization,
                    contentWidth
                )
                currentY += 20f
            }
            
            // Draw skills section
            if (resume.skills.isNotEmpty()) {
                val (newPage, newCanvas) = checkNewPage(80f)
                page = newPage
                canvas = newCanvas
                currentY = drawSkillsSection(
                    canvas, 
                    resume.skills, 
                    sectionHeaderPaint, 
                    bodyPaint, 
                    currentY,
                    customization,
                    contentWidth
                )
                currentY += 20f
            }
            
            // Draw experience section
            if (resume.experience.isNotEmpty()) {
                val (newPage, newCanvas) = checkNewPage(100f)
                page = newPage
                canvas = newCanvas
                currentY = drawExperienceSection(
                    canvas, 
                    resume.experience, 
                    sectionHeaderPaint, 
                    bodyPaint, 
                    smallPaint, 
                    currentY,
                    customization,
                    contentWidth
                ) { height ->
                    val (p, c) = checkNewPage(height)
                    page = p
                    canvas = c
                    currentY = customization.margins.top
                    currentY
                }
                currentY += 20f
            }
            
            // Draw education section
            if (resume.education.isNotEmpty()) {
                val (newPage, newCanvas) = checkNewPage(80f)
                page = newPage
                canvas = newCanvas
                currentY = drawEducationSection(
                    canvas, 
                    resume.education, 
                    sectionHeaderPaint, 
                    bodyPaint, 
                    smallPaint, 
                    currentY,
                    customization,
                    contentWidth
                ) { height ->
                    val (p, c) = checkNewPage(height)
                    page = p
                    canvas = c
                    currentY = customization.margins.top
                    currentY
                }
                currentY += 20f
            }
            
            // Draw projects section
            if (resume.projects.isNotEmpty()) {
                val (newPage, newCanvas) = checkNewPage(100f)
                page = newPage
                canvas = newCanvas
                currentY = drawProjectsSection(
                    canvas, 
                    resume.projects, 
                    sectionHeaderPaint, 
                    bodyPaint, 
                    smallPaint, 
                    currentY,
                    customization,
                    contentWidth
                ) { height ->
                    val (p, c) = checkNewPage(height)
                    page = p
                    canvas = c
                    currentY = customization.margins.top
                    currentY
                }
            }
            
            // Add footer to last page
            drawFooter(canvas, customization, pageWidth, pageHeight, currentPage)
            
            // Finish the last page
            pdfDocument.finishPage(page)
            
            // Save to file
            val fileName = "${resume.personal.fullName.ifBlank { "Resume" }}_${getTimestamp()}.pdf"
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
     * Create custom paint with settings
     */
    private fun createCustomPaint(
        textSize: Float,
        typeface: Int,
        color: androidx.compose.ui.graphics.Color
    ): Paint {
        return Paint().apply {
            this.textSize = textSize
            this.typeface = Typeface.DEFAULT
            this.color = color.toArgb()
            isAntiAlias = true
        }
    }
    
    /**
     * Draw page header
     */
    private fun drawHeader(
        canvas: Canvas,
        customization: PdfCustomization,
        pageWidth: Float,
        pageNumber: Int
    ) {
        if (customization.headerFooter.showHeader) {
            val headerPaint = createCustomPaint(
                customization.fontSettings.smallSize,
                Typeface.NORMAL,
                customization.colorScheme.lightTextColor
            )
            
            canvas.drawText(
                customization.headerFooter.headerText,
                customization.margins.left,
                customization.margins.top - 10f,
                headerPaint
            )
        }
    }
    
    /**
     * Draw page footer
     */
    private fun drawFooter(
        canvas: Canvas,
        customization: PdfCustomization,
        pageWidth: Float,
        pageHeight: Float,
        pageNumber: Int
    ) {
        if (customization.headerFooter.showFooter) {
            val footerPaint = createCustomPaint(
                customization.fontSettings.smallSize,
                Typeface.NORMAL,
                customization.colorScheme.lightTextColor
            )
            
            val footerY = pageHeight - customization.margins.bottom + 20f
            
            // Footer text
            if (customization.headerFooter.footerText.isNotBlank()) {
                canvas.drawText(
                    customization.headerFooter.footerText,
                    customization.margins.left,
                    footerY,
                    footerPaint
                )
            }
            
            // Page number
            if (customization.headerFooter.pageNumbers) {
                val pageText = "Page $pageNumber"
                val textWidth = footerPaint.measureText(pageText)
                canvas.drawText(
                    pageText,
                    pageWidth - customization.margins.right - textWidth,
                    footerY,
                    footerPaint
                )
            }
            
            // Date stamp
            if (customization.headerFooter.dateStamp) {
                val dateText = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())
                val textWidth = footerPaint.measureText(dateText)
                canvas.drawText(
                    dateText,
                    pageWidth - customization.margins.right - textWidth,
                    footerY + 15f,
                    footerPaint
                )
            }
        }
    }
    
    /**
     * Draw resume header section
     */
    private fun drawResumeHeader(
        canvas: Canvas,
        resume: Resume,
        titlePaint: Paint,
        subtitlePaint: Paint,
        smallPaint: Paint,
        startY: Float,
        customization: PdfCustomization,
        contentWidth: Float
    ): Float {
        var currentY = startY
        
        // Avatar (if available and enabled)
        if (resume.personal.avatarUri != null && customization.pageSize != PageSize.A4) {
            try {
                val avatarBitmap = loadBitmapFromUri(resume.personal.avatarUri)
                if (avatarBitmap != null) {
                    val avatarSize = 80f
                    val avatarX = customization.margins.left + (contentWidth - avatarSize) / 2
                    val avatarRect = Rect(
                        avatarX.toInt(),
                        currentY.toInt(),
                        (avatarX + avatarSize).toInt(),
                        (currentY + avatarSize).toInt()
                    )
                    
                    val circularBitmap = createCircularBitmap(avatarBitmap, avatarSize.toInt())
                    canvas.drawBitmap(circularBitmap, null, avatarRect, null)
                    currentY += avatarSize + 10f
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        // Name
        val nameWidth = titlePaint.measureText(resume.personal.fullName)
        val nameX = customization.margins.left + (contentWidth - nameWidth) / 2
        canvas.drawText(resume.personal.fullName, nameX, currentY, titlePaint)
        currentY += titlePaint.textSize + 5f
        
        // Title
        if (resume.personal.title.isNotBlank()) {
            val titleWidth = subtitlePaint.measureText(resume.personal.title)
            val titleX = customization.margins.left + (contentWidth - titleWidth) / 2
            canvas.drawText(resume.personal.title, titleX, currentY, subtitlePaint)
            currentY += subtitlePaint.textSize + 10f
        }
        
        // Contact information
        val contactInfo = mutableListOf<String>()
        if (resume.personal.email.isNotBlank()) contactInfo.add(resume.personal.email)
        if (resume.personal.phone.isNotBlank()) contactInfo.add(resume.personal.phone)
        if (resume.personal.location.isNotBlank()) contactInfo.add(resume.personal.location)
        
        if (contactInfo.isNotEmpty()) {
            val contactLine = contactInfo.joinToString(" • ")
            val contactWidth = smallPaint.measureText(contactLine)
            val contactX = customization.margins.left + (contentWidth - contactWidth) / 2
            canvas.drawText(contactLine, contactX, currentY, smallPaint)
            currentY += smallPaint.textSize + 5f
        }
        
        // Links
        val links = mutableListOf<String>()
        if (resume.personal.linkedin.isNotBlank()) links.add(resume.personal.linkedin)
        if (resume.personal.github.isNotBlank()) links.add(resume.personal.github)
        if (resume.personal.website.isNotBlank()) links.add(resume.personal.website)
        
        if (links.isNotEmpty()) {
            val linksLine = links.joinToString(" • ")
            val linksWidth = smallPaint.measureText(linksLine)
            val linksX = customization.margins.left + (contentWidth - linksWidth) / 2
            canvas.drawText(linksLine, linksX, currentY, smallPaint)
            currentY += smallPaint.textSize + 10f
        }
        
        // Draw separator line
        val linePaint = Paint().apply {
            color = customization.colorScheme.primaryColor.toArgb()
            strokeWidth = 2f
        }
        canvas.drawLine(
            customization.margins.left,
            currentY,
            customization.margins.left + contentWidth,
            currentY,
            linePaint
        )
        currentY += 10f
        
        return currentY
    }
    
    /**
     * Draw summary section
     */
    private fun drawSummarySection(
        canvas: Canvas,
        summary: String,
        sectionHeaderPaint: Paint,
        bodyPaint: Paint,
        startY: Float,
        customization: PdfCustomization,
        contentWidth: Float
    ): Float {
        var currentY = startY
        
        // Section header
        canvas.drawText("SUMMARY", customization.margins.left, currentY, sectionHeaderPaint)
        currentY += sectionHeaderPaint.textSize + 10f
        
        // Summary text
        val lines = summary.split("\n")
        for (line in lines) {
            if (line.isNotBlank()) {
                canvas.drawText(line.trim(), customization.margins.left, currentY, bodyPaint)
                currentY += bodyPaint.textSize + 5f
            }
        }
        
        return currentY
    }
    
    /**
     * Draw skills section
     */
    private fun drawSkillsSection(
        canvas: Canvas,
        skills: List<String>,
        sectionHeaderPaint: Paint,
        bodyPaint: Paint,
        startY: Float,
        customization: PdfCustomization,
        contentWidth: Float
    ): Float {
        var currentY = startY
        
        // Section header
        canvas.drawText("SKILLS", customization.margins.left, currentY, sectionHeaderPaint)
        currentY += sectionHeaderPaint.textSize + 10f
        
        // Skills text
        val skillsText = skills.joinToString(" • ")
        canvas.drawText(skillsText, customization.margins.left, currentY, bodyPaint)
        currentY += bodyPaint.textSize + 10f
        
        return currentY
    }
    
    /**
     * Draw experience section
     */
    private fun drawExperienceSection(
        canvas: Canvas,
        experience: List<fm.mrc.resumebuilder.data.model.Experience>,
        sectionHeaderPaint: Paint,
        bodyPaint: Paint,
        smallPaint: Paint,
        startY: Float,
        customization: PdfCustomization,
        contentWidth: Float,
        checkNewPage: (Float) -> Float
    ): Float {
        var currentY = startY
        
        // Section header
        canvas.drawText("EXPERIENCE", customization.margins.left, currentY, sectionHeaderPaint)
        currentY += sectionHeaderPaint.textSize + 10f
        
        for (exp in experience) {
            // Job title and company
            val titleText = "${exp.role} at ${exp.company}"
            canvas.drawText(titleText, customization.margins.left, currentY, bodyPaint)
            currentY += bodyPaint.textSize + 5f
            
            // Duration
            val durationText = "${exp.start} - ${exp.end}"
            canvas.drawText(durationText, customization.margins.left, currentY, smallPaint)
            currentY += smallPaint.textSize + 5f
            
            // Bullets
            for (bullet in exp.bullets) {
                if (bullet.isNotBlank()) {
                    canvas.drawText("• $bullet", customization.margins.left, currentY, bodyPaint)
                    currentY += bodyPaint.textSize + 3f
                }
            }
            currentY += 10f
        }
        
        return currentY
    }
    
    /**
     * Draw education section
     */
    private fun drawEducationSection(
        canvas: Canvas,
        education: List<fm.mrc.resumebuilder.data.model.Education>,
        sectionHeaderPaint: Paint,
        bodyPaint: Paint,
        smallPaint: Paint,
        startY: Float,
        customization: PdfCustomization,
        contentWidth: Float,
        checkNewPage: (Float) -> Float
    ): Float {
        var currentY = startY
        
        // Section header
        canvas.drawText("EDUCATION", customization.margins.left, currentY, sectionHeaderPaint)
        currentY += sectionHeaderPaint.textSize + 10f
        
        for (edu in education) {
            // Degree and institution
            val degreeText = "${edu.degree} from ${edu.institution}"
            canvas.drawText(degreeText, customization.margins.left, currentY, bodyPaint)
            currentY += bodyPaint.textSize + 5f
            
            // Duration
            val durationText = "${edu.start} - ${edu.end}"
            canvas.drawText(durationText, customization.margins.left, currentY, smallPaint)
            currentY += smallPaint.textSize + 5f
            
            // Details
            if (edu.details.isNotBlank()) {
                val lines = edu.details.split("\n")
                for (line in lines) {
                    if (line.isNotBlank()) {
                        canvas.drawText(line.trim(), customization.margins.left, currentY, bodyPaint)
                        currentY += bodyPaint.textSize + 3f
                    }
                }
            }
            
            currentY += 10f
        }
        
        return currentY
    }
    
    /**
     * Draw projects section
     */
    private fun drawProjectsSection(
        canvas: Canvas,
        projects: List<fm.mrc.resumebuilder.data.model.Project>,
        sectionHeaderPaint: Paint,
        bodyPaint: Paint,
        smallPaint: Paint,
        startY: Float,
        customization: PdfCustomization,
        contentWidth: Float,
        checkNewPage: (Float) -> Float
    ): Float {
        var currentY = startY
        
        // Section header
        canvas.drawText("PROJECTS", customization.margins.left, currentY, sectionHeaderPaint)
        currentY += sectionHeaderPaint.textSize + 10f
        
        for (project in projects) {
            // Project title
            canvas.drawText(project.title, customization.margins.left, currentY, bodyPaint)
            currentY += bodyPaint.textSize + 5f
            
            // Tech stack
            if (project.tech.isNotEmpty()) {
                val techText = "Tech: ${project.tech.joinToString(", ")}"
                canvas.drawText(techText, customization.margins.left, currentY, smallPaint)
                currentY += smallPaint.textSize + 5f
            }
            
            // Description
            if (project.description.isNotBlank()) {
                val lines = project.description.split("\n")
                for (line in lines) {
                    if (line.isNotBlank()) {
                        canvas.drawText(line.trim(), customization.margins.left, currentY, bodyPaint)
                        currentY += bodyPaint.textSize + 3f
                    }
                }
            }
            
            // Link
            if (project.link.isNotBlank()) {
                canvas.drawText(project.link, customization.margins.left, currentY, smallPaint)
                currentY += smallPaint.textSize + 5f
            }
            
            currentY += 10f
        }
        
        return currentY
    }
    
    private fun loadBitmapFromUri(uriString: String): Bitmap? {
        // Implementation for loading bitmap from URI
        return null
    }
    
    private fun createCircularBitmap(bitmap: Bitmap, size: Int): Bitmap {
        // Implementation for creating circular bitmap
        return bitmap
    }
    
    private fun getTimestamp(): String {
        return SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    }
}
