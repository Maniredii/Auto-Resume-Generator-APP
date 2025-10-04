package fm.mrc.resumebuilder.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fm.mrc.resumebuilder.data.model.ResumeTemplate
import fm.mrc.resumebuilder.data.model.TemplateCategory
import fm.mrc.resumebuilder.data.template.TemplateManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateSelectionScreen(
    onNavigateBack: () -> Unit,
    onTemplateSelected: (ResumeTemplate) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf<TemplateCategory?>(null) }
    var showPremiumOnly by remember { mutableStateOf(false) }
    
    val templates = remember(selectedCategory, showPremiumOnly) {
        val allTemplates = TemplateManager.getAllTemplates()
        when {
            selectedCategory != null -> TemplateManager.getTemplatesByCategory(selectedCategory!!)
            showPremiumOnly -> TemplateManager.getPremiumTemplates()
            else -> allTemplates
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Choose Template") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    FilterChip(
                        onClick = { showPremiumOnly = !showPremiumOnly },
                        label = { Text("Premium") },
                        selected = showPremiumOnly
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Category Filter
            CategoryFilter(
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it }
            )
            
            // Templates Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(templates) { template ->
                    TemplateCard(
                        template = template,
                        onClick = { onTemplateSelected(template) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryFilter(
    selectedCategory: TemplateCategory?,
    onCategorySelected: (TemplateCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf(
        null to "All",
        TemplateCategory.MODERN to "Modern",
        TemplateCategory.CLASSIC to "Classic",
        TemplateCategory.CREATIVE to "Creative",
        TemplateCategory.MINIMALIST to "Minimalist",
        TemplateCategory.PROFESSIONAL to "Professional",
        TemplateCategory.TECH to "Tech",
        TemplateCategory.BUSINESS to "Business",
        TemplateCategory.HEALTHCARE to "Healthcare",
        TemplateCategory.ACADEMIC to "Academic"
    )
    
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { (category, name) ->
            FilterChip(
                onClick = { onCategorySelected(category) },
                label = { Text(name) },
                selected = selectedCategory == category
            )
        }
    }
}

@Composable
private fun TemplateCard(
    template: ResumeTemplate,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Template Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(template.previewColor.copy(alpha = 0.1f))
                    .border(
                        width = 2.dp,
                        color = template.previewColor,
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Simple preview representation
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(template.previewColor)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(template.previewColor)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(template.previewColor.copy(alpha = 0.7f))
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Template Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = template.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = template.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (template.isPremium) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Premium",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Category Badge
            Surface(
                color = template.previewColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = template.category.name.lowercase().replaceFirstChar { it.uppercase() },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = template.previewColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
