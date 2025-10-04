package fm.mrc.resumebuilder.data.template

import androidx.compose.ui.graphics.Color
import fm.mrc.resumebuilder.data.model.*

/**
 * Manager for resume templates
 */
object TemplateManager {
    
    /**
     * Get all available templates
     */
    fun getAllTemplates(): List<ResumeTemplate> {
        return listOf(
            // XML Layout Templates
            createSimpleXmlTemplate(),
            createModernXmlTemplate(),
            createCreativeXmlTemplate(),
            
            // Modern Templates
            createModernTemplate(),
            createMinimalistTemplate(),
            createTechTemplate(),
            
            // Classic Templates
            createClassicTemplate(),
            createProfessionalTemplate(),
            createBusinessTemplate(),
            
            // Creative Templates
            createCreativeTemplate(),
            createDesignTemplate(),
            
            // Industry Specific
            createHealthcareTemplate(),
            createAcademicTemplate()
        )
    }
    
    /**
     * Get templates by category
     */
    fun getTemplatesByCategory(category: TemplateCategory): List<ResumeTemplate> {
        return getAllTemplates().filter { it.category == category }
    }
    
    /**
     * Get free templates only
     */
    fun getFreeTemplates(): List<ResumeTemplate> {
        return getAllTemplates().filter { !it.isPremium }
    }
    
    /**
     * Get premium templates only
     */
    fun getPremiumTemplates(): List<ResumeTemplate> {
        return getAllTemplates().filter { it.isPremium }
    }
    
    /**
     * Get template by ID
     */
    fun getTemplateById(id: String): ResumeTemplate? {
        return getAllTemplates().find { it.id == id }
    }
    
    // Template Definitions
    
    // XML Layout Templates
    private fun createSimpleXmlTemplate(): ResumeTemplate {
        return ResumeTemplate(
            id = "simple_xml",
            name = "Simple XML",
            description = "Clean and simple XML layout template",
            category = TemplateCategory.CLASSIC,
            previewColor = Color(0xFF424242),
            isPremium = false,
            layoutConfig = LayoutConfig(
                headerStyle = HeaderStyle(
                    showAvatar = false,
                    avatarSize = 0f,
                    nameStyle = TextStyle.LARGE_BOLD,
                    titleStyle = TextStyle.MEDIUM,
                    contactLayout = ContactLayout.HORIZONTAL
                ),
                sectionStyle = SectionStyle(
                    showIcons = false,
                    sectionSpacing = 20f,
                    itemSpacing = 12f,
                    bulletStyle = BulletStyle.DOT
                ),
                colorScheme = ColorScheme(
                    primary = Color(0xFF424242),
                    secondary = Color(0xFF616161),
                    background = Color(0xFFFFFFFF),
                    surface = Color(0xFFFFFFFF),
                    onPrimary = Color(0xFFFFFFFF),
                    onSecondary = Color(0xFFFFFFFF),
                    onBackground = Color(0xFF212121),
                    onSurface = Color(0xFF424242)
                ),
                typography = TypographyConfig(
                    fontFamily = "Arial",
                    headingSize = 14f,
                    bodySize = 12f,
                    smallSize = 10f
                ),
                spacing = SpacingConfig(
                    margin = 16f,
                    padding = 12f,
                    lineSpacing = 1.2f
                )
            )
        )
    }
    
    private fun createModernXmlTemplate(): ResumeTemplate {
        return ResumeTemplate(
            id = "modern_xml",
            name = "Modern XML",
            description = "Contemporary XML layout with blue accent",
            category = TemplateCategory.MODERN,
            previewColor = Color(0xFF2196F3),
            isPremium = false,
            layoutConfig = LayoutConfig(
                headerStyle = HeaderStyle(
                    showAvatar = false,
                    avatarSize = 0f,
                    nameStyle = TextStyle.LARGE_BOLD,
                    titleStyle = TextStyle.MEDIUM,
                    contactLayout = ContactLayout.GRID
                ),
                sectionStyle = SectionStyle(
                    showIcons = false,
                    sectionSpacing = 24f,
                    itemSpacing = 16f,
                    bulletStyle = BulletStyle.DOT
                ),
                colorScheme = ColorScheme(
                    primary = Color(0xFF2196F3),
                    secondary = Color(0xFF1976D2),
                    background = Color(0xFFFFFFFF),
                    surface = Color(0xFFF5F5F5),
                    onPrimary = Color(0xFFFFFFFF),
                    onSecondary = Color(0xFFFFFFFF),
                    onBackground = Color(0xFF212121),
                    onSurface = Color(0xFF424242)
                ),
                typography = TypographyConfig(
                    fontFamily = "Roboto",
                    headingSize = 16f,
                    bodySize = 13f,
                    smallSize = 11f
                ),
                spacing = SpacingConfig(
                    margin = 20f,
                    padding = 16f,
                    lineSpacing = 1.4f
                )
            )
        )
    }
    
    private fun createCreativeXmlTemplate(): ResumeTemplate {
        return ResumeTemplate(
            id = "creative_xml",
            name = "Creative XML",
            description = "Bold and artistic XML layout design",
            category = TemplateCategory.CREATIVE,
            previewColor = Color(0xFFE91E63),
            isPremium = false,
            layoutConfig = LayoutConfig(
                headerStyle = HeaderStyle(
                    showAvatar = false,
                    avatarSize = 0f,
                    nameStyle = TextStyle.LARGE_BOLD,
                    titleStyle = TextStyle.MEDIUM,
                    contactLayout = ContactLayout.GRID
                ),
                sectionStyle = SectionStyle(
                    showIcons = true,
                    sectionSpacing = 26f,
                    itemSpacing = 18f,
                    bulletStyle = BulletStyle.ARROW
                ),
                colorScheme = ColorScheme(
                    primary = Color(0xFFE91E63),
                    secondary = Color(0xFFC2185B),
                    background = Color(0xFFFFFFFF),
                    surface = Color(0xFFFCE4EC),
                    onPrimary = Color(0xFFFFFFFF),
                    onSecondary = Color(0xFFFFFFFF),
                    onBackground = Color(0xFF212121),
                    onSurface = Color(0xFF424242)
                ),
                typography = TypographyConfig(
                    fontFamily = "Montserrat",
                    headingSize = 18f,
                    bodySize = 13f,
                    smallSize = 11f
                ),
                spacing = SpacingConfig(
                    margin = 20f,
                    padding = 16f,
                    lineSpacing = 1.5f
                )
            )
        )
    }
    
    private fun createModernTemplate(): ResumeTemplate {
        return ResumeTemplate(
            id = "modern",
            name = "Modern",
            description = "Clean and contemporary design with bold typography",
            category = TemplateCategory.MODERN,
            previewColor = Color(0xFF2196F3),
            isPremium = false,
            layoutConfig = LayoutConfig(
                headerStyle = HeaderStyle(
                    showAvatar = true,
                    avatarSize = 100f,
                    nameStyle = TextStyle.LARGE_BOLD,
                    titleStyle = TextStyle.MEDIUM,
                    contactLayout = ContactLayout.HORIZONTAL
                ),
                sectionStyle = SectionStyle(
                    showIcons = true,
                    sectionSpacing = 24f,
                    itemSpacing = 16f,
                    bulletStyle = BulletStyle.DOT
                ),
                colorScheme = ColorScheme(
                    primary = Color(0xFF2196F3),
                    secondary = Color(0xFF1976D2),
                    background = Color(0xFFFFFFFF),
                    surface = Color(0xFFF5F5F5),
                    onPrimary = Color(0xFFFFFFFF),
                    onSecondary = Color(0xFFFFFFFF),
                    onBackground = Color(0xFF212121),
                    onSurface = Color(0xFF424242)
                ),
                typography = TypographyConfig(
                    fontFamily = "Roboto",
                    headingSize = 18f,
                    bodySize = 12f,
                    smallSize = 10f
                ),
                spacing = SpacingConfig(
                    margin = 24f,
                    padding = 16f,
                    lineSpacing = 1.4f
                )
            )
        )
    }
    
    private fun createClassicTemplate(): ResumeTemplate {
        return ResumeTemplate(
            id = "classic",
            name = "Classic",
            description = "Traditional professional layout with timeless appeal",
            category = TemplateCategory.CLASSIC,
            previewColor = Color(0xFF424242),
            isPremium = false,
            layoutConfig = LayoutConfig(
                headerStyle = HeaderStyle(
                    showAvatar = true,
                    avatarSize = 80f,
                    nameStyle = TextStyle.LARGE_BOLD,
                    titleStyle = TextStyle.MEDIUM,
                    contactLayout = ContactLayout.VERTICAL
                ),
                sectionStyle = SectionStyle(
                    showIcons = false,
                    sectionSpacing = 20f,
                    itemSpacing = 12f,
                    bulletStyle = BulletStyle.DASH
                ),
                colorScheme = ColorScheme(
                    primary = Color(0xFF424242),
                    secondary = Color(0xFF616161),
                    background = Color(0xFFFFFFFF),
                    surface = Color(0xFFFFFFFF),
                    onPrimary = Color(0xFFFFFFFF),
                    onSecondary = Color(0xFFFFFFFF),
                    onBackground = Color(0xFF212121),
                    onSurface = Color(0xFF424242)
                ),
                typography = TypographyConfig(
                    fontFamily = "Times New Roman",
                    headingSize = 16f,
                    bodySize = 11f,
                    smallSize = 9f
                ),
                spacing = SpacingConfig(
                    margin = 20f,
                    padding = 12f,
                    lineSpacing = 1.2f
                )
            )
        )
    }
    
    private fun createMinimalistTemplate(): ResumeTemplate {
        return ResumeTemplate(
            id = "minimalist",
            name = "Minimalist",
            description = "Ultra-clean design with maximum white space",
            category = TemplateCategory.MINIMALIST,
            previewColor = Color(0xFF000000),
            isPremium = true,
            layoutConfig = LayoutConfig(
                headerStyle = HeaderStyle(
                    showAvatar = false,
                    avatarSize = 0f,
                    nameStyle = TextStyle.LARGE_BOLD,
                    titleStyle = TextStyle.MEDIUM,
                    contactLayout = ContactLayout.HORIZONTAL
                ),
                sectionStyle = SectionStyle(
                    showIcons = false,
                    sectionSpacing = 32f,
                    itemSpacing = 20f,
                    bulletStyle = BulletStyle.DOT
                ),
                colorScheme = ColorScheme(
                    primary = Color(0xFF000000),
                    secondary = Color(0xFF424242),
                    background = Color(0xFFFFFFFF),
                    surface = Color(0xFFFFFFFF),
                    onPrimary = Color(0xFFFFFFFF),
                    onSecondary = Color(0xFFFFFFFF),
                    onBackground = Color(0xFF000000),
                    onSurface = Color(0xFF424242)
                ),
                typography = TypographyConfig(
                    fontFamily = "Helvetica",
                    headingSize = 20f,
                    bodySize = 13f,
                    smallSize = 11f
                ),
                spacing = SpacingConfig(
                    margin = 32f,
                    padding = 20f,
                    lineSpacing = 1.6f
                )
            )
        )
    }
    
    private fun createTechTemplate(): ResumeTemplate {
        return ResumeTemplate(
            id = "tech",
            name = "Tech",
            description = "Modern design perfect for technology professionals",
            category = TemplateCategory.TECH,
            previewColor = Color(0xFF00BCD4),
            isPremium = false,
            layoutConfig = LayoutConfig(
                headerStyle = HeaderStyle(
                    showAvatar = true,
                    avatarSize = 90f,
                    nameStyle = TextStyle.LARGE_BOLD,
                    titleStyle = TextStyle.MEDIUM,
                    contactLayout = ContactLayout.GRID
                ),
                sectionStyle = SectionStyle(
                    showIcons = true,
                    sectionSpacing = 22f,
                    itemSpacing = 14f,
                    bulletStyle = BulletStyle.ARROW
                ),
                colorScheme = ColorScheme(
                    primary = Color(0xFF00BCD4),
                    secondary = Color(0xFF0097A7),
                    background = Color(0xFFFFFFFF),
                    surface = Color(0xFFF8F9FA),
                    onPrimary = Color(0xFFFFFFFF),
                    onSecondary = Color(0xFFFFFFFF),
                    onBackground = Color(0xFF212121),
                    onSurface = Color(0xFF424242)
                ),
                typography = TypographyConfig(
                    fontFamily = "Roboto Mono",
                    headingSize = 17f,
                    bodySize = 12f,
                    smallSize = 10f
                ),
                spacing = SpacingConfig(
                    margin = 22f,
                    padding = 14f,
                    lineSpacing = 1.3f
                )
            )
        )
    }
    
    private fun createProfessionalTemplate(): ResumeTemplate {
        return ResumeTemplate(
            id = "professional",
            name = "Professional",
            description = "Corporate-friendly design for business professionals",
            category = TemplateCategory.PROFESSIONAL,
            previewColor = Color(0xFF3F51B5),
            isPremium = false,
            layoutConfig = LayoutConfig(
                headerStyle = HeaderStyle(
                    showAvatar = true,
                    avatarSize = 85f,
                    nameStyle = TextStyle.LARGE_BOLD,
                    titleStyle = TextStyle.MEDIUM,
                    contactLayout = ContactLayout.HORIZONTAL
                ),
                sectionStyle = SectionStyle(
                    showIcons = false,
                    sectionSpacing = 20f,
                    itemSpacing = 14f,
                    bulletStyle = BulletStyle.DOT
                ),
                colorScheme = ColorScheme(
                    primary = Color(0xFF3F51B5),
                    secondary = Color(0xFF303F9F),
                    background = Color(0xFFFFFFFF),
                    surface = Color(0xFFFFFFFF),
                    onPrimary = Color(0xFFFFFFFF),
                    onSecondary = Color(0xFFFFFFFF),
                    onBackground = Color(0xFF212121),
                    onSurface = Color(0xFF424242)
                ),
                typography = TypographyConfig(
                    fontFamily = "Arial",
                    headingSize = 16f,
                    bodySize = 11f,
                    smallSize = 9f
                ),
                spacing = SpacingConfig(
                    margin = 20f,
                    padding = 14f,
                    lineSpacing = 1.3f
                )
            )
        )
    }
    
    private fun createCreativeTemplate(): ResumeTemplate {
        return ResumeTemplate(
            id = "creative",
            name = "Creative",
            description = "Bold and artistic design for creative professionals",
            category = TemplateCategory.CREATIVE,
            previewColor = Color(0xFFE91E63),
            isPremium = true,
            layoutConfig = LayoutConfig(
                headerStyle = HeaderStyle(
                    showAvatar = true,
                    avatarSize = 110f,
                    nameStyle = TextStyle.LARGE_BOLD,
                    titleStyle = TextStyle.MEDIUM,
                    contactLayout = ContactLayout.GRID
                ),
                sectionStyle = SectionStyle(
                    showIcons = true,
                    sectionSpacing = 26f,
                    itemSpacing = 18f,
                    bulletStyle = BulletStyle.ARROW
                ),
                colorScheme = ColorScheme(
                    primary = Color(0xFFE91E63),
                    secondary = Color(0xFFC2185B),
                    background = Color(0xFFFFFFFF),
                    surface = Color(0xFFFCE4EC),
                    onPrimary = Color(0xFFFFFFFF),
                    onSecondary = Color(0xFFFFFFFF),
                    onBackground = Color(0xFF212121),
                    onSurface = Color(0xFF424242)
                ),
                typography = TypographyConfig(
                    fontFamily = "Montserrat",
                    headingSize = 19f,
                    bodySize = 13f,
                    smallSize = 11f
                ),
                spacing = SpacingConfig(
                    margin = 26f,
                    padding = 18f,
                    lineSpacing = 1.5f
                )
            )
        )
    }
    
    private fun createDesignTemplate(): ResumeTemplate {
        return ResumeTemplate(
            id = "design",
            name = "Design",
            description = "Visual-focused template for designers and artists",
            category = TemplateCategory.CREATIVE_DESIGN,
            previewColor = Color(0xFFFF9800),
            isPremium = true,
            layoutConfig = LayoutConfig(
                headerStyle = HeaderStyle(
                    showAvatar = true,
                    avatarSize = 120f,
                    nameStyle = TextStyle.LARGE_BOLD,
                    titleStyle = TextStyle.MEDIUM,
                    contactLayout = ContactLayout.HORIZONTAL
                ),
                sectionStyle = SectionStyle(
                    showIcons = true,
                    sectionSpacing = 28f,
                    itemSpacing = 20f,
                    bulletStyle = BulletStyle.DOT
                ),
                colorScheme = ColorScheme(
                    primary = Color(0xFFFF9800),
                    secondary = Color(0xFFF57C00),
                    background = Color(0xFFFFFFFF),
                    surface = Color(0xFFFFF3E0),
                    onPrimary = Color(0xFFFFFFFF),
                    onSecondary = Color(0xFFFFFFFF),
                    onBackground = Color(0xFF212121),
                    onSurface = Color(0xFF424242)
                ),
                typography = TypographyConfig(
                    fontFamily = "Open Sans",
                    headingSize = 20f,
                    bodySize = 14f,
                    smallSize = 12f
                ),
                spacing = SpacingConfig(
                    margin = 28f,
                    padding = 20f,
                    lineSpacing = 1.6f
                )
            )
        )
    }
    
    private fun createBusinessTemplate(): ResumeTemplate {
        return ResumeTemplate(
            id = "business",
            name = "Business",
            description = "Executive-level template for business leaders",
            category = TemplateCategory.BUSINESS,
            previewColor = Color(0xFF795548),
            isPremium = false,
            layoutConfig = LayoutConfig(
                headerStyle = HeaderStyle(
                    showAvatar = true,
                    avatarSize = 80f,
                    nameStyle = TextStyle.LARGE_BOLD,
                    titleStyle = TextStyle.MEDIUM,
                    contactLayout = ContactLayout.VERTICAL
                ),
                sectionStyle = SectionStyle(
                    showIcons = false,
                    sectionSpacing = 18f,
                    itemSpacing = 12f,
                    bulletStyle = BulletStyle.DASH
                ),
                colorScheme = ColorScheme(
                    primary = Color(0xFF795548),
                    secondary = Color(0xFF5D4037),
                    background = Color(0xFFFFFFFF),
                    surface = Color(0xFFF5F5F5),
                    onPrimary = Color(0xFFFFFFFF),
                    onSecondary = Color(0xFFFFFFFF),
                    onBackground = Color(0xFF212121),
                    onSurface = Color(0xFF424242)
                ),
                typography = TypographyConfig(
                    fontFamily = "Georgia",
                    headingSize = 16f,
                    bodySize = 11f,
                    smallSize = 9f
                ),
                spacing = SpacingConfig(
                    margin = 18f,
                    padding = 12f,
                    lineSpacing = 1.2f
                )
            )
        )
    }
    
    private fun createHealthcareTemplate(): ResumeTemplate {
        return ResumeTemplate(
            id = "healthcare",
            name = "Healthcare",
            description = "Professional template for healthcare workers",
            category = TemplateCategory.HEALTHCARE,
            previewColor = Color(0xFF4CAF50),
            isPremium = false,
            layoutConfig = LayoutConfig(
                headerStyle = HeaderStyle(
                    showAvatar = true,
                    avatarSize = 85f,
                    nameStyle = TextStyle.LARGE_BOLD,
                    titleStyle = TextStyle.MEDIUM,
                    contactLayout = ContactLayout.HORIZONTAL
                ),
                sectionStyle = SectionStyle(
                    showIcons = true,
                    sectionSpacing = 20f,
                    itemSpacing = 14f,
                    bulletStyle = BulletStyle.DOT
                ),
                colorScheme = ColorScheme(
                    primary = Color(0xFF4CAF50),
                    secondary = Color(0xFF388E3C),
                    background = Color(0xFFFFFFFF),
                    surface = Color(0xFFE8F5E8),
                    onPrimary = Color(0xFFFFFFFF),
                    onSecondary = Color(0xFFFFFFFF),
                    onBackground = Color(0xFF212121),
                    onSurface = Color(0xFF424242)
                ),
                typography = TypographyConfig(
                    fontFamily = "Arial",
                    headingSize = 16f,
                    bodySize = 11f,
                    smallSize = 9f
                ),
                spacing = SpacingConfig(
                    margin = 20f,
                    padding = 14f,
                    lineSpacing = 1.3f
                )
            )
        )
    }
    
    private fun createAcademicTemplate(): ResumeTemplate {
        return ResumeTemplate(
            id = "academic",
            name = "Academic",
            description = "Scholarly template for academic professionals",
            category = TemplateCategory.ACADEMIC,
            previewColor = Color(0xFF9C27B0),
            isPremium = false,
            layoutConfig = LayoutConfig(
                headerStyle = HeaderStyle(
                    showAvatar = true,
                    avatarSize = 75f,
                    nameStyle = TextStyle.LARGE_BOLD,
                    titleStyle = TextStyle.MEDIUM,
                    contactLayout = ContactLayout.VERTICAL
                ),
                sectionStyle = SectionStyle(
                    showIcons = false,
                    sectionSpacing = 18f,
                    itemSpacing = 12f,
                    bulletStyle = BulletStyle.NUMBER
                ),
                colorScheme = ColorScheme(
                    primary = Color(0xFF9C27B0),
                    secondary = Color(0xFF7B1FA2),
                    background = Color(0xFFFFFFFF),
                    surface = Color(0xFFFFFFFF),
                    onPrimary = Color(0xFFFFFFFF),
                    onSecondary = Color(0xFFFFFFFF),
                    onBackground = Color(0xFF212121),
                    onSurface = Color(0xFF424242)
                ),
                typography = TypographyConfig(
                    fontFamily = "Times New Roman",
                    headingSize = 15f,
                    bodySize = 10f,
                    smallSize = 8f
                ),
                spacing = SpacingConfig(
                    margin = 18f,
                    padding = 12f,
                    lineSpacing = 1.1f
                )
            )
        )
    }
}
