package fm.mrc.resumebuilder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import fm.mrc.resumebuilder.data.db.ResumeDatabase
import fm.mrc.resumebuilder.navigation.AppNavHost
import fm.mrc.resumebuilder.navigation.navigateToSettings
import fm.mrc.resumebuilder.ui.theme.ResumeBuilderTheme
import fm.mrc.resumebuilder.data.template.TemplateManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            ResumeBuilderTheme {
                ResumeBuilderApp()
            }
        }
    }
}

@Composable
fun ResumeBuilderApp() {
    val navController = rememberNavController()
    val context = androidx.compose.ui.platform.LocalContext.current
    val database = remember { ResumeDatabase.getDatabase(context) }
    
    // Template selection state
    var selectedTemplate by remember { mutableStateOf("simple") }
    var showTemplateDialog by remember { mutableStateOf(false) }
    
    AppNavHost(
        navController = navController,
        database = database,
        selectedTemplate = selectedTemplate,
        onTemplateSelected = { template ->
            selectedTemplate = template
            showTemplateDialog = false
        },
        onShowTemplateDialog = { showTemplateDialog = true },
        modifier = Modifier.fillMaxSize()
    )
    
    // Template selection dialog
    if (showTemplateDialog) {
        TemplateSelectionDialog(
            selectedTemplate = selectedTemplate,
            onTemplateSelected = { template ->
                selectedTemplate = template
                showTemplateDialog = false
            },
            onDismiss = { showTemplateDialog = false }
        )
    }
}

@Composable
private fun TemplateSelectionDialog(
    selectedTemplate: String,
    onTemplateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val availableTemplates = remember { TemplateManager.getAllTemplates() }
    var currentSelection by remember { mutableStateOf(selectedTemplate) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Choose Template",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(availableTemplates) { template ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { currentSelection = template.id }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentSelection == template.id,
                            onClick = { currentSelection = template.id }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        // Template preview color
                        Surface(
                            modifier = Modifier.size(32.dp),
                            shape = RoundedCornerShape(6.dp),
                            color = template.previewColor
                        ) {
                            Box(
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = template.name.take(1).uppercase(),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = androidx.compose.ui.graphics.Color.White
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = template.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = template.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onTemplateSelected(currentSelection)
                }
            ) {
                Text("Select")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
