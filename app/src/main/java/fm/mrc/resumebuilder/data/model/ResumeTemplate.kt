package fm.mrc.resumebuilder.data.model

import androidx.compose.ui.graphics.Color

/**
 * Resume template data class
 */
data class ResumeTemplate(
    val id: String,
    val name: String,
    val description: String,
    val category: TemplateCategory,
    val previewColor: Color,
    val isPremium: Boolean = false,
    val layoutConfig: LayoutConfig
)

/**
 * Template categories
 */
enum class TemplateCategory {
    MODERN,
    CLASSIC,
    CREATIVE,
    MINIMALIST,
    PROFESSIONAL,
    TECH,
    CREATIVE_DESIGN,
    BUSINESS,
    HEALTHCARE,
    ACADEMIC
}

/**
 * Layout configuration for templates
 */
data class LayoutConfig(
    val headerStyle: HeaderStyle,
    val sectionStyle: SectionStyle,
    val colorScheme: ColorScheme,
    val typography: TypographyConfig,
    val spacing: SpacingConfig
)

data class HeaderStyle(
    val showAvatar: Boolean = true,
    val avatarSize: Float = 100f,
    val nameStyle: TextStyle = TextStyle.LARGE_BOLD,
    val titleStyle: TextStyle = TextStyle.MEDIUM,
    val contactLayout: ContactLayout = ContactLayout.HORIZONTAL
)

data class SectionStyle(
    val showIcons: Boolean = true,
    val sectionSpacing: Float = 24f,
    val itemSpacing: Float = 16f,
    val bulletStyle: BulletStyle = BulletStyle.DOT
)

data class ColorScheme(
    val primary: Color,
    val secondary: Color,
    val background: Color,
    val surface: Color,
    val onPrimary: Color,
    val onSecondary: Color,
    val onBackground: Color,
    val onSurface: Color
)

data class TypographyConfig(
    val fontFamily: String = "Roboto",
    val headingSize: Float = 18f,
    val bodySize: Float = 12f,
    val smallSize: Float = 10f
)

data class SpacingConfig(
    val margin: Float = 24f,
    val padding: Float = 16f,
    val lineSpacing: Float = 1.4f
)

enum class TextStyle {
    LARGE_BOLD,
    MEDIUM,
    SMALL,
    TINY
}

enum class ContactLayout {
    HORIZONTAL,
    VERTICAL,
    GRID
}

enum class BulletStyle {
    DOT,
    DASH,
    ARROW,
    NUMBER
}
