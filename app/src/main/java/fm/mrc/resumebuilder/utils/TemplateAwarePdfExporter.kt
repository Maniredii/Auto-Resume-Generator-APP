package fm.mrc.resumebuilder.utils

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.FileProvider
import fm.mrc.resumebuilder.data.model.*
import fm.mrc.resumebuilder.data.template.TemplateManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Template-aware PDF exporter that uses selected resume template layout
 */
class TemplateAwarePdfExporter {
    
    companion object {
        private const val PAGE_WIDTH = 595 // A4 width in points
        private const val PAGE_HEIGHT = 842 // A4 height in points
        private const val MIN_MARGIN = 36f // Minimum margin for print safety
        private const val PRINT_SAFE_MARGIN = 18f // Additional margin for print safety
    }
    
    /**
     * Export resume to PDF using the selected template
     */
    suspend fun exportResumeToPdf(
        context: Context, 
        resume: Resume,
        templateId: String = "simple"
    ): Uri {
        return withContext(Dispatchers.IO) {
            val template = TemplateManager.getTemplateById(templateId) ?: TemplateManager.getTemplateById("simple")!!
            val layoutConfig = template.layoutConfig
            
            // Ensure print-safe margins
            val effectiveMargin = maxOf(layoutConfig.spacing.margin, MIN_MARGIN)
            val effectiveContentWidth = PAGE_WIDTH - (effectiveMargin * 2)
            
            val pdfDocument = PdfDocument()
            var currentPage = 1
            var currentY = effectiveMargin
            
            // Start first page
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, currentPage).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas
            
            // Create paint objects based on template configuration
            val paints = createTemplatePaints(layoutConfig)
            
            // Helper function to check if we need a new page
            fun checkNewPage(neededHeight: Float): Pair<PdfDocument.Page, Canvas> {
                if (currentY + neededHeight > PAGE_HEIGHT - effectiveMargin) {
                    pdfDocument.finishPage(page)
                    currentPage++
                    val newPageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, currentPage).create()
                    page = pdfDocument.startPage(newPageInfo)
                    canvas = page.canvas
                    currentY = effectiveMargin
                }
                return Pair(page, canvas)
            }
            
            // Draw header section based on template
            currentY = drawTemplateHeader(
                context, canvas, resume, template, paints, currentY, effectiveMargin, effectiveContentWidth
            )
            currentY += layoutConfig.sectionStyle.sectionSpacing
            
            // Draw summary section
            if (resume.summary.isNotBlank()) {
                val (newPage, newCanvas) = checkNewPage(60f)
                page = newPage
                canvas = newCanvas
                currentY = drawTemplateSection(
                    canvas, "PROFESSIONAL SUMMARY", resume.summary, 
                    template, paints, currentY, effectiveMargin, effectiveContentWidth
                )
                currentY += layoutConfig.sectionStyle.sectionSpacing
            }
            
            // Draw skills section
            if (resume.skills.isNotEmpty()) {
                val (newPage, newCanvas) = checkNewPage(80f)
                page = newPage
                canvas = newCanvas
                currentY = drawTemplateSkillsSection(
                    canvas, resume.skills, template, paints, currentY, effectiveMargin, effectiveContentWidth
                )
                currentY += layoutConfig.sectionStyle.sectionSpacing
            }
            
            // Draw experience section
            if (resume.experience.isNotEmpty()) {
                val (newPage, newCanvas) = checkNewPage(100f)
                page = newPage
                canvas = newCanvas
                currentY = drawTemplateExperienceSection(
                    canvas, resume.experience, template, paints, currentY, effectiveMargin, effectiveContentWidth
                ) { height ->
                    val (p, c) = checkNewPage(height)
                    page = p
                    canvas = c
                    currentY = effectiveMargin
                    currentY
                }
                currentY += layoutConfig.sectionStyle.sectionSpacing
            }
            
            // Draw education section
            if (resume.education.isNotEmpty()) {
                val (newPage, newCanvas) = checkNewPage(80f)
                page = newPage
                canvas = newCanvas
                currentY = drawTemplateEducationSection(
                    canvas, resume.education, template, paints, currentY, effectiveMargin, effectiveContentWidth
                ) { height ->
                    val (p, c) = checkNewPage(height)
                    page = p
                    canvas = c
                    currentY = effectiveMargin
                    currentY
                }
                currentY += layoutConfig.sectionStyle.sectionSpacing
            }
            
            // Draw projects section
            if (resume.projects.isNotEmpty()) {
                val (newPage, newCanvas) = checkNewPage(100f)
                page = newPage
                canvas = newCanvas
                currentY = drawTemplateProjectsSection(
                    canvas, resume.projects, template, paints, currentY, effectiveMargin, effectiveContentWidth
                ) { height ->
                    val (p, c) = checkNewPage(height)
                    page = p
                    canvas = c
                    currentY = effectiveMargin
                    currentY
                }
            }
            
            // Finish the last page
            pdfDocument.finishPage(page)
            
            // Save to file
            val fileName = "${resume.personal.fullName.ifBlank { "Resume" }}_${template.name}_${
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
     * Create paint objects based on template configuration
     */
    private fun createTemplatePaints(layoutConfig: LayoutConfig): TemplatePaints {
        val typography = layoutConfig.typography
        val colorScheme = layoutConfig.colorScheme
        
        // Ensure minimum readable sizes for print
        val headingSize = maxOf(typography.headingSize, 14f)
        val bodySize = maxOf(typography.bodySize, 10f)
        val smallSize = maxOf(typography.smallSize, 8f)
        
        return TemplatePaints(
            titlePaint = createPaint(
                headingSize + 4f, 
                Typeface.BOLD,
                colorScheme.onBackground
            ),
            subtitlePaint = createPaint(
                bodySize + 2f, 
                Typeface.NORMAL,
                colorScheme.onSurface
            ),
            sectionHeaderPaint = createPaint(
                headingSize, 
                Typeface.BOLD,
                colorScheme.primary
            ),
            bodyPaint = createPaint(
                bodySize, 
                Typeface.NORMAL,
                colorScheme.onBackground
            ),
            boldBodyPaint = createPaint(
                bodySize, 
                Typeface.BOLD,
                colorScheme.onBackground
            ),
            smallPaint = createPaint(
                smallSize, 
                Typeface.NORMAL,
                colorScheme.onSurface
            ),
            accentPaint = createPaint(
                bodySize, 
                Typeface.NORMAL,
                colorScheme.primary
            )
        )
    }
    
    /**
     * Draw template-specific header
     */
    private fun drawTemplateHeader(
        context: Context,
        canvas: Canvas,
        resume: Resume,
        template: ResumeTemplate,
        paints: TemplatePaints,
        startY: Float,
        margin: Float,
        contentWidth: Float
    ): Float {
        val layoutConfig = template.layoutConfig
        val headerStyle = layoutConfig.headerStyle
        val colorScheme = layoutConfig.colorScheme
        var currentY = startY
        
        // Draw background if needed (for templates with colored headers)
        if (template.category == TemplateCategory.MODERN || template.category == TemplateCategory.CREATIVE) {
            val headerRect = RectF(
                margin - 10f,
                currentY - 10f,
                PAGE_WIDTH - margin + 10f,
                currentY + 120f
            )
            val headerPaint = Paint().apply {
                color = colorScheme.primary.toArgb()
                isAntiAlias = true
            }
            canvas.drawRoundRect(headerRect, 8f, 8f, headerPaint)
        }
        
        // Avatar (if enabled in template)
        if (headerStyle.showAvatar && resume.personal.avatarUri != null) {
            try {
                val avatarBitmap = loadBitmapFromUri(context, resume.personal.avatarUri)
                if (avatarBitmap != null) {
                    val avatarSize = headerStyle.avatarSize
                    val avatarX = margin + (contentWidth - avatarSize) / 2
                    val avatarRect = RectF(
                        avatarX,
                        currentY,
                        avatarX + avatarSize,
                        currentY + avatarSize
                    )
                    
                    // Create circular avatar
                    val circularBitmap = createCircularBitmap(avatarBitmap, avatarSize.toInt())
                    canvas.drawBitmap(circularBitmap, null, avatarRect, null)
                    currentY += avatarSize + 10f
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        // Name
        val namePaint = if (template.category == TemplateCategory.MODERN || template.category == TemplateCategory.CREATIVE) {
            createPaint(layoutConfig.typography.headingSize + 4f, Typeface.BOLD, colorScheme.onPrimary)
        } else {
            paints.titlePaint
        }
        
        val nameWidth = namePaint.measureText(resume.personal.fullName)
        val nameX = margin + (contentWidth - nameWidth) / 2
        canvas.drawText(resume.personal.fullName, nameX, currentY, namePaint)
        currentY += namePaint.textSize + 5f
        
        // Title
        if (resume.personal.title.isNotBlank()) {
            val titlePaint = if (template.category == TemplateCategory.MODERN || template.category == TemplateCategory.CREATIVE) {
                createPaint(layoutConfig.typography.bodySize + 2f, Typeface.NORMAL, colorScheme.onPrimary)
            } else {
                paints.subtitlePaint
            }
            
            val titleWidth = titlePaint.measureText(resume.personal.title)
            val titleX = margin + (contentWidth - titleWidth) / 2
            canvas.drawText(resume.personal.title, titleX, currentY, titlePaint)
            currentY += titlePaint.textSize + 10f
        }
        
        // Contact information based on layout
        currentY = drawContactInfo(canvas, resume, template, paints, currentY, margin, contentWidth)
        
        // Draw separator line
        currentY += 10f
        val separatorPaint = Paint().apply {
            color = colorScheme.primary.toArgb()
            strokeWidth = 2f
            isAntiAlias = true
        }
        canvas.drawLine(
            margin,
            currentY,
            PAGE_WIDTH - margin,
            currentY,
            separatorPaint
        )
        currentY += 10f
        
        return currentY
    }
    
    /**
     * Draw contact information based on template layout
     */
    private fun drawContactInfo(
        canvas: Canvas,
        resume: Resume,
        template: ResumeTemplate,
        paints: TemplatePaints,
        startY: Float,
        margin: Float,
        contentWidth: Float
    ): Float {
        val layoutConfig = template.layoutConfig
        val contactLayout = layoutConfig.headerStyle.contactLayout
        val colorScheme = layoutConfig.colorScheme
        var currentY = startY
        
        val contactInfo = mutableListOf<String>()
        if (resume.personal.email.isNotBlank()) contactInfo.add(resume.personal.email)
        if (resume.personal.phone.isNotBlank()) contactInfo.add(resume.personal.phone)
        if (resume.personal.location.isNotBlank()) contactInfo.add(resume.personal.location)
        if (resume.personal.linkedin.isNotBlank()) contactInfo.add(resume.personal.linkedin)
        if (resume.personal.github.isNotBlank()) contactInfo.add(resume.personal.github)
        if (resume.personal.website.isNotBlank()) contactInfo.add(resume.personal.website)
        
        val contactPaint = if (template.category == TemplateCategory.MODERN || template.category == TemplateCategory.CREATIVE) {
            createPaint(layoutConfig.typography.smallSize, Typeface.NORMAL, colorScheme.onPrimary)
        } else {
            paints.smallPaint
        }
        
        when (contactLayout) {
            ContactLayout.HORIZONTAL -> {
                val contactLine = contactInfo.joinToString(" • ")
                val contactWidth = contactPaint.measureText(contactLine)
                val contactX = margin + (contentWidth - contactWidth) / 2
                canvas.drawText(contactLine, contactX, currentY, contactPaint)
                currentY += contactPaint.textSize + 5f
            }
            ContactLayout.VERTICAL -> {
                contactInfo.forEach { info ->
                    val infoWidth = contactPaint.measureText(info)
                    val infoX = margin + (contentWidth - infoWidth) / 2
                    canvas.drawText(info, infoX, currentY, contactPaint)
                    currentY += contactPaint.textSize + 3f
                }
            }
            ContactLayout.GRID -> {
                val itemsPerRow = 3
                val itemWidth = contentWidth / itemsPerRow
                contactInfo.chunked(itemsPerRow).forEach { rowItems ->
                    rowItems.forEachIndexed { index, info ->
                        val infoX = margin + index * itemWidth
                        canvas.drawText(info, infoX, currentY, contactPaint)
                    }
                    currentY += contactPaint.textSize + 5f
                }
            }
        }
        
        return currentY
    }
    
    /**
     * Draw template-specific section
     */
    private fun drawTemplateSection(
        canvas: Canvas,
        title: String,
        content: String,
        template: ResumeTemplate,
        paints: TemplatePaints,
        startY: Float,
        margin: Float,
        contentWidth: Float
    ): Float {
        val layoutConfig = template.layoutConfig
        var currentY = startY
        
        // Section header with template-specific styling
        val sectionTitle = when (template.category) {
            TemplateCategory.CREATIVE -> "◆ $title"
            TemplateCategory.MODERN -> title
            else -> title
        }
        
        canvas.drawText(sectionTitle, margin, currentY, paints.sectionHeaderPaint)
        currentY += paints.sectionHeaderPaint.textSize + layoutConfig.sectionStyle.itemSpacing
        
        // Content with proper line spacing
        val lines = wrapText(content, paints.bodyPaint, contentWidth.toInt())
        for (line in lines) {
            canvas.drawText(line, margin, currentY, paints.bodyPaint)
            currentY += paints.bodyPaint.textSize * layoutConfig.spacing.lineSpacing
        }
        
        return currentY
    }
    
    /**
     * Draw template-specific skills section
     */
    private fun drawTemplateSkillsSection(
        canvas: Canvas,
        skills: List<String>,
        template: ResumeTemplate,
        paints: TemplatePaints,
        startY: Float,
        margin: Float,
        contentWidth: Float
    ): Float {
        val layoutConfig = template.layoutConfig
        var currentY = startY
        
        // Section header
        val sectionTitle = when (template.category) {
            TemplateCategory.CREATIVE -> "◆ SKILLS & EXPERTISE"
            TemplateCategory.MODERN -> "SKILLS"
            else -> "SKILLS"
        }
        
        canvas.drawText(sectionTitle, margin, currentY, paints.sectionHeaderPaint)
        currentY += paints.sectionHeaderPaint.textSize + layoutConfig.sectionStyle.itemSpacing
        
        // Skills based on template style
        when (template.category) {
            TemplateCategory.MODERN, TemplateCategory.CREATIVE -> {
                // Display skills in a more visual way
                val skillsText = skills.joinToString(" • ")
                val lines = wrapText(skillsText, paints.bodyPaint, contentWidth.toInt())
                for (line in lines) {
                    canvas.drawText(line, margin, currentY, paints.bodyPaint)
                    currentY += paints.bodyPaint.textSize * layoutConfig.spacing.lineSpacing
                }
            }
            else -> {
                // Simple comma-separated list
                val skillsText = skills.joinToString(", ")
                val lines = wrapText(skillsText, paints.bodyPaint, contentWidth.toInt())
                for (line in lines) {
                    canvas.drawText(line, margin, currentY, paints.bodyPaint)
                    currentY += paints.bodyPaint.textSize * layoutConfig.spacing.lineSpacing
                }
            }
        }
        
        return currentY
    }
    
    /**
     * Draw template-specific experience section
     */
    private fun drawTemplateExperienceSection(
        canvas: Canvas,
        experiences: List<Experience>,
        template: ResumeTemplate,
        paints: TemplatePaints,
        startY: Float,
        margin: Float,
        contentWidth: Float,
        checkNewPage: (Float) -> Float
    ): Float {
        val layoutConfig = template.layoutConfig
        var currentY = startY
        
        // Section header
        val sectionTitle = when (template.category) {
            TemplateCategory.CREATIVE -> "◆ PROFESSIONAL EXPERIENCE"
            TemplateCategory.MODERN -> "WORK EXPERIENCE"
            else -> "WORK EXPERIENCE"
        }
        
        canvas.drawText(sectionTitle, margin, currentY, paints.sectionHeaderPaint)
        currentY += paints.sectionHeaderPaint.textSize + layoutConfig.sectionStyle.itemSpacing
        
        for (experience in experiences) {
            // Check if we need space for this experience entry
            val estimatedHeight = paints.boldBodyPaint.textSize * 2 + paints.smallPaint.textSize + 
                    (experience.bullets.size * paints.bodyPaint.textSize) + 40f
            if (currentY + estimatedHeight > PAGE_HEIGHT - margin) {
                currentY = checkNewPage(estimatedHeight)
            }
            
            // Role
            canvas.drawText(experience.role, margin, currentY, paints.boldBodyPaint)
            currentY += paints.boldBodyPaint.textSize + 3f
            
            // Company and dates
            val companyDate = "${experience.company} • ${experience.start} - ${experience.end}"
            canvas.drawText(companyDate, margin, currentY, paints.smallPaint)
            currentY += paints.smallPaint.textSize + layoutConfig.sectionStyle.itemSpacing
            
            // Bullets with template-specific styling
            for (bullet in experience.bullets) {
                val bulletChar = when (template.layoutConfig.sectionStyle.bulletStyle) {
                    BulletStyle.DOT -> "•"
                    BulletStyle.DASH -> "–"
                    BulletStyle.ARROW -> "▶"
                    BulletStyle.NUMBER -> "•"
                }
                
                val bulletText = "$bulletChar $bullet"
                val lines = wrapText(bulletText, paints.bodyPaint, (contentWidth - 20).toInt())
                for ((index, line) in lines.withIndex()) {
                    val x = if (index == 0) margin else margin + 20f
                    canvas.drawText(line, x, currentY, paints.bodyPaint)
                    currentY += paints.bodyPaint.textSize * layoutConfig.spacing.lineSpacing
                }
            }
            
            currentY += layoutConfig.sectionStyle.itemSpacing
        }
        
        return currentY
    }
    
    /**
     * Draw template-specific education section
     */
    private fun drawTemplateEducationSection(
        canvas: Canvas,
        educations: List<Education>,
        template: ResumeTemplate,
        paints: TemplatePaints,
        startY: Float,
        margin: Float,
        contentWidth: Float,
        checkNewPage: (Float) -> Float
    ): Float {
        val layoutConfig = template.layoutConfig
        var currentY = startY
        
        // Section header
        val sectionTitle = when (template.category) {
            TemplateCategory.CREATIVE -> "◆ EDUCATION"
            else -> "EDUCATION"
        }
        
        canvas.drawText(sectionTitle, margin, currentY, paints.sectionHeaderPaint)
        currentY += paints.sectionHeaderPaint.textSize + layoutConfig.sectionStyle.itemSpacing
        
        for (education in educations) {
            // Check if we need space for this education entry
            val estimatedHeight = paints.boldBodyPaint.textSize + paints.smallPaint.textSize + 
                    paints.bodyPaint.textSize + 30f
            if (currentY + estimatedHeight > PAGE_HEIGHT - margin) {
                currentY = checkNewPage(estimatedHeight)
            }
            
            // Degree
            canvas.drawText(education.degree, margin, currentY, paints.boldBodyPaint)
            currentY += paints.boldBodyPaint.textSize + 3f
            
            // Institution and dates
            val institutionDate = "${education.institution} • ${education.start} - ${education.end}"
            canvas.drawText(institutionDate, margin, currentY, paints.smallPaint)
            currentY += paints.smallPaint.textSize + 3f
            
            // Details
            if (education.details.isNotBlank()) {
                val lines = wrapText(education.details, paints.bodyPaint, contentWidth.toInt())
                for (line in lines) {
                    canvas.drawText(line, margin, currentY, paints.bodyPaint)
                    currentY += paints.bodyPaint.textSize * layoutConfig.spacing.lineSpacing
                }
            }
            
            currentY += layoutConfig.sectionStyle.itemSpacing
        }
        
        return currentY
    }
    
    /**
     * Draw template-specific projects section
     */
    private fun drawTemplateProjectsSection(
        canvas: Canvas,
        projects: List<Project>,
        template: ResumeTemplate,
        paints: TemplatePaints,
        startY: Float,
        margin: Float,
        contentWidth: Float,
        checkNewPage: (Float) -> Float
    ): Float {
        val layoutConfig = template.layoutConfig
        var currentY = startY
        
        // Section header
        val sectionTitle = when (template.category) {
            TemplateCategory.CREATIVE -> "◆ FEATURED PROJECTS"
            TemplateCategory.MODERN -> "PROJECTS"
            else -> "PROJECTS"
        }
        
        canvas.drawText(sectionTitle, margin, currentY, paints.sectionHeaderPaint)
        currentY += paints.sectionHeaderPaint.textSize + layoutConfig.sectionStyle.itemSpacing
        
        for (project in projects) {
            // Check if we need space for this project entry
            val estimatedHeight = paints.boldBodyPaint.textSize + paints.smallPaint.textSize + 
                    paints.bodyPaint.textSize * 3 + 40f
            if (currentY + estimatedHeight > PAGE_HEIGHT - margin) {
                currentY = checkNewPage(estimatedHeight)
            }
            
            // Project title
            canvas.drawText(project.title, margin, currentY, paints.boldBodyPaint)
            currentY += paints.boldBodyPaint.textSize + 3f
            
            // Link
            if (project.link.isNotBlank()) {
                canvas.drawText(project.link, margin, currentY, paints.accentPaint)
                currentY += paints.smallPaint.textSize + 3f
            }
            
            // Description
            if (project.description.isNotBlank()) {
                val lines = wrapText(project.description, paints.bodyPaint, contentWidth.toInt())
                for (line in lines) {
                    canvas.drawText(line, margin, currentY, paints.bodyPaint)
                    currentY += paints.bodyPaint.textSize * layoutConfig.spacing.lineSpacing
                }
            }
            
            // Technologies
            if (project.tech.isNotEmpty()) {
                val techText = "Technologies: ${project.tech.joinToString(", ")}"
                val lines = wrapText(techText, paints.smallPaint, contentWidth.toInt())
                for (line in lines) {
                    canvas.drawText(line, margin, currentY, paints.smallPaint)
                    currentY += paints.smallPaint.textSize * layoutConfig.spacing.lineSpacing
                }
            }
            
            currentY += layoutConfig.sectionStyle.itemSpacing
        }
        
        return currentY
    }
    
    /**
     * Create paint with specified properties
     */
    private fun createPaint(textSize: Float, typeface: Int, color: androidx.compose.ui.graphics.Color): Paint {
        return Paint().apply {
            this.textSize = textSize
            this.typeface = Typeface.create(Typeface.DEFAULT, typeface)
            this.color = color.toArgb()
            isAntiAlias = true
            isSubpixelText = true
            isLinearText = true
            // Ensure good print quality
            setHinting(Paint.HINTING_ON)
        }
    }
    
    /**
     * Wrap text to fit within specified width
     */
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
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        
        // Scale and draw the original bitmap
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, size, size, true)
        canvas.drawBitmap(scaledBitmap, 0f, 0f, paint)
        
        return circularBitmap
    }
    
    /**
     * Data class to hold all paint objects for a template
     */
    private data class TemplatePaints(
        val titlePaint: Paint,
        val subtitlePaint: Paint,
        val sectionHeaderPaint: Paint,
        val bodyPaint: Paint,
        val boldBodyPaint: Paint,
        val smallPaint: Paint,
        val accentPaint: Paint
    )
}
