package fm.mrc.resumebuilder.ui.responsive

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Responsive layout utilities
 */
object ResponsiveLayout {
    
    /**
     * Breakpoints for different screen sizes
     */
    object Breakpoints {
        const val MOBILE = 600
        const val TABLET = 840
        const val DESKTOP = 1200
    }
    
    /**
     * Get current screen type based on width
     */
    @Composable
    fun getScreenType(): ScreenType {
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp
        
        return when {
            screenWidth < Breakpoints.MOBILE -> ScreenType.MOBILE
            screenWidth < Breakpoints.TABLET -> ScreenType.TABLET
            screenWidth < Breakpoints.DESKTOP -> ScreenType.DESKTOP
            else -> ScreenType.LARGE_DESKTOP
        }
    }
    
    /**
     * Get responsive padding based on screen type
     */
    @Composable
    fun getResponsivePadding(): PaddingValues {
        val screenType = getScreenType()
        return when (screenType) {
            ScreenType.MOBILE -> PaddingValues(16.dp)
            ScreenType.TABLET -> PaddingValues(24.dp)
            ScreenType.DESKTOP -> PaddingValues(32.dp)
            ScreenType.LARGE_DESKTOP -> PaddingValues(48.dp)
        }
    }
    
    /**
     * Get responsive spacing based on screen type
     */
    @Composable
    fun getResponsiveSpacing(): Dp {
        val screenType = getScreenType()
        return when (screenType) {
            ScreenType.MOBILE -> 16.dp
            ScreenType.TABLET -> 20.dp
            ScreenType.DESKTOP -> 24.dp
            ScreenType.LARGE_DESKTOP -> 32.dp
        }
    }
    
    /**
     * Get responsive column count for grids
     */
    @Composable
    fun getResponsiveColumnCount(): Int {
        val screenType = getScreenType()
        return when (screenType) {
            ScreenType.MOBILE -> 1
            ScreenType.TABLET -> 2
            ScreenType.DESKTOP -> 3
            ScreenType.LARGE_DESKTOP -> 4
        }
    }
    
    /**
     * Get responsive max width for content
     */
    @Composable
    fun getResponsiveMaxWidth(): Dp {
        val screenType = getScreenType()
        return when (screenType) {
            ScreenType.MOBILE -> 600.dp
            ScreenType.TABLET -> 800.dp
            ScreenType.DESKTOP -> 1000.dp
            ScreenType.LARGE_DESKTOP -> 1200.dp
        }
    }
}

/**
 * Screen types
 */
enum class ScreenType {
    MOBILE,
    TABLET,
    DESKTOP,
    LARGE_DESKTOP
}

/**
 * Responsive container that adapts to screen size
 */
@Composable
fun ResponsiveContainer(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val screenType = ResponsiveLayout.getScreenType()
    val maxWidth = ResponsiveLayout.getResponsiveMaxWidth()
    val padding = ResponsiveLayout.getResponsivePadding()
    
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .let { 
                    if (screenType != ScreenType.MOBILE) {
                        it.widthIn(max = maxWidth)
                    } else {
                        it
                    }
                }
                .padding(padding)
                .align(Alignment.Center),
            content = content
        )
    }
}

/**
 * Responsive grid that adapts column count based on screen size
 */
@Composable
fun ResponsiveGrid(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val columnCount = ResponsiveLayout.getResponsiveColumnCount()
    val spacing = ResponsiveLayout.getResponsiveSpacing()
    
    when (columnCount) {
        1 -> {
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(spacing)
            ) {
                content()
            }
        }
        else -> {
            // For multiple columns, you would use LazyVerticalGrid
            // This is a simplified version
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(spacing)
            ) {
                content()
            }
        }
    }
}

/**
 * Responsive card that adapts padding and elevation
 */
@Composable
fun ResponsiveCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val screenType = ResponsiveLayout.getScreenType()
    val padding = when (screenType) {
        ScreenType.MOBILE -> PaddingValues(16.dp)
        ScreenType.TABLET -> PaddingValues(20.dp)
        ScreenType.DESKTOP -> PaddingValues(24.dp)
        ScreenType.LARGE_DESKTOP -> PaddingValues(32.dp)
    }
    
    val elevation = when (screenType) {
        ScreenType.MOBILE -> CardDefaults.cardElevation(defaultElevation = 2.dp)
        ScreenType.TABLET -> CardDefaults.cardElevation(defaultElevation = 4.dp)
        ScreenType.DESKTOP -> CardDefaults.cardElevation(defaultElevation = 6.dp)
        ScreenType.LARGE_DESKTOP -> CardDefaults.cardElevation(defaultElevation = 8.dp)
    }
    
    Card(
        modifier = modifier,
        elevation = elevation
    ) {
        Column(
            modifier = Modifier.padding(padding),
            content = content
        )
    }
}
