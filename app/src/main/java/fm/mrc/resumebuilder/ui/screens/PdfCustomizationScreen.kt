package fm.mrc.resumebuilder.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fm.mrc.resumebuilder.data.pdf.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfCustomizationScreen(
    onNavigateBack: () -> Unit,
    onCustomizeComplete: (PdfCustomization) -> Unit,
    modifier: Modifier = Modifier
) {
    var customization by remember { mutableStateOf(PdfCustomization()) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PDF Customization") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { onCustomizeComplete(customization) }) {
                        Text("Apply")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Page Settings
            PageSettingsCard(
                customization = customization,
                onCustomizationChange = { customization = it }
            )
            
            // Font Settings
            FontSettingsCard(
                customization = customization,
                onCustomizationChange = { customization = it }
            )
            
            // Color Settings
            ColorSettingsCard(
                customization = customization,
                onCustomizationChange = { customization = it }
            )
            
            // Header/Footer Settings
            HeaderFooterSettingsCard(
                customization = customization,
                onCustomizationChange = { customization = it }
            )
            
            // Watermark Settings
            WatermarkSettingsCard(
                customization = customization,
                onCustomizationChange = { customization = it }
            )
        }
    }
}

@Composable
private fun PageSettingsCard(
    customization: PdfCustomization,
    onCustomizationChange: (PdfCustomization) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Page Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Page Size
            Text(
                text = "Page Size",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PageSize.values().forEach { pageSize ->
                    FilterChip(
                        onClick = { 
                            onCustomizationChange(customization.copy(pageSize = pageSize))
                        },
                        label = { Text(pageSize.name) },
                        selected = customization.pageSize == pageSize
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Orientation
            Text(
                text = "Orientation",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PageOrientation.values().forEach { orientation ->
                    FilterChip(
                        onClick = { 
                            onCustomizationChange(customization.copy(orientation = orientation))
                        },
                        label = { Text(orientation.name) },
                        selected = customization.orientation == orientation
                    )
                }
            }
        }
    }
}

@Composable
private fun FontSettingsCard(
    customization: PdfCustomization,
    onCustomizationChange: (PdfCustomization) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Font Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Font Family
            OutlinedTextField(
                value = customization.fontSettings.primaryFont,
                onValueChange = { 
                    onCustomizationChange(
                        customization.copy(
                            fontSettings = customization.fontSettings.copy(primaryFont = it)
                        )
                    )
                },
                label = { Text("Primary Font") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Font Sizes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = customization.fontSettings.titleSize.toString(),
                    onValueChange = { 
                        it.toFloatOrNull()?.let { size ->
                            onCustomizationChange(
                                customization.copy(
                                    fontSettings = customization.fontSettings.copy(titleSize = size)
                                )
                            )
                        }
                    },
                    label = { Text("Title Size") },
                    modifier = Modifier.weight(1f)
                )
                
                OutlinedTextField(
                    value = customization.fontSettings.bodySize.toString(),
                    onValueChange = { 
                        it.toFloatOrNull()?.let { size ->
                            onCustomizationChange(
                                customization.copy(
                                    fontSettings = customization.fontSettings.copy(bodySize = size)
                                )
                            )
                        }
                    },
                    label = { Text("Body Size") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ColorSettingsCard(
    customization: PdfCustomization,
    onCustomizationChange: (PdfCustomization) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Color Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Color preview
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ColorPreview(
                    color = customization.colorScheme.primaryColor,
                    label = "Primary"
                )
                ColorPreview(
                    color = customization.colorScheme.secondaryColor,
                    label = "Secondary"
                )
                ColorPreview(
                    color = customization.colorScheme.accentColor,
                    label = "Accent"
                )
            }
        }
    }
}

@Composable
private fun ColorPreview(
    color: Color,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color, MaterialTheme.shapes.medium)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun HeaderFooterSettingsCard(
    customization: PdfCustomization,
    onCustomizationChange: (PdfCustomization) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Header & Footer",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Show Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Show Header")
                Switch(
                    checked = customization.headerFooter.showHeader,
                    onCheckedChange = { 
                        onCustomizationChange(
                            customization.copy(
                                headerFooter = customization.headerFooter.copy(showHeader = it)
                            )
                        )
                    }
                )
            }
            
            // Show Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Show Footer")
                Switch(
                    checked = customization.headerFooter.showFooter,
                    onCheckedChange = { 
                        onCustomizationChange(
                            customization.copy(
                                headerFooter = customization.headerFooter.copy(showFooter = it)
                            )
                        )
                    }
                )
            }
            
            // Page Numbers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Page Numbers")
                Switch(
                    checked = customization.headerFooter.pageNumbers,
                    onCheckedChange = { 
                        onCustomizationChange(
                            customization.copy(
                                headerFooter = customization.headerFooter.copy(pageNumbers = it)
                            )
                        )
                    }
                )
            }
            
            // Footer Text
            if (customization.headerFooter.showFooter) {
                OutlinedTextField(
                    value = customization.headerFooter.footerText,
                    onValueChange = { 
                        onCustomizationChange(
                            customization.copy(
                                headerFooter = customization.headerFooter.copy(footerText = it)
                            )
                        )
                    },
                    label = { Text("Footer Text") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun WatermarkSettingsCard(
    customization: PdfCustomization,
    onCustomizationChange: (PdfCustomization) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Watermark",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Enable Watermark
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Enable Watermark")
                Switch(
                    checked = customization.watermark.enabled,
                    onCheckedChange = { 
                        onCustomizationChange(
                            customization.copy(
                                watermark = customization.watermark.copy(enabled = it)
                            )
                        )
                    }
                )
            }
            
            // Watermark Text
            if (customization.watermark.enabled) {
                OutlinedTextField(
                    value = customization.watermark.text,
                    onValueChange = { 
                        onCustomizationChange(
                            customization.copy(
                                watermark = customization.watermark.copy(text = it)
                            )
                        )
                    },
                    label = { Text("Watermark Text") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
