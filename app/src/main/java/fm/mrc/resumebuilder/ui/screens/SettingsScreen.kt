package fm.mrc.resumebuilder.ui.screens
//Developed by Manideep Reddy Eevuri
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import fm.mrc.resumebuilder.data.backup.JsonBackupManager
import fm.mrc.resumebuilder.data.db.ResumeDatabase
import fm.mrc.resumebuilder.data.repo.ResumeRepositoryImpl
import fm.mrc.resumebuilder.settings.DataStoreManager
import fm.mrc.resumebuilder.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val database = remember { ResumeDatabase.getDatabase(context) }
    val viewModel: SettingsViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return SettingsViewModel.Factory(context)(database) as T
            }
        }
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // File picker for import
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.importResumes(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            // Appearance Section
            SettingsSection(title = "Appearance") {
                SettingsToggleItem(
                    icon = Icons.Default.Settings,
                    title = "Dark Theme",
                    subtitle = "Use dark theme throughout the app",
                    checked = uiState.isDarkTheme,
                    onCheckedChange = viewModel::updateDarkTheme
                )
            }

            // Data & Sync Section
            SettingsSection(title = "Data & Sync") {
                SettingsToggleItem(
                    icon = Icons.Default.Settings,
                    title = "Cloud Sync",
                    subtitle = "Sync your resumes across devices",
                    checked = uiState.isSyncOptIn,
                    onCheckedChange = viewModel::updateSyncOptIn
                )
                
                SettingsToggleItem(
                    icon = Icons.Default.Settings,
                    title = "Auto Save",
                    subtitle = "Automatically save changes while editing",
                    checked = uiState.isAutoSaveEnabled,
                    onCheckedChange = viewModel::updateAutoSaveEnabled
                )
            }

            // Notifications Section
            SettingsSection(title = "Notifications") {
                SettingsToggleItem(
                    icon = Icons.Default.Notifications,
                    title = "Push Notifications",
                    subtitle = "Receive updates and reminders",
                    checked = uiState.isNotificationsEnabled,
                    onCheckedChange = viewModel::updateNotificationsEnabled
                )
            }

            // Premium Section
            SettingsSection(title = "Premium") {
                if (uiState.isPremiumUser) {
                    SettingsItem(
                        icon = Icons.Default.Star,
                        title = "Premium Active",
                        subtitle = "Thank you for supporting Resume Builder!",
                        onClick = { /* Navigate to premium management */ }
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "ACTIVE",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    SettingsItem(
                        icon = Icons.Default.Star,
                        title = "Upgrade to Premium",
                        subtitle = "Unlock advanced templates and features",
                        onClick = { 
                            // In a real app, this would navigate to purchase flow
                            viewModel.updatePremiumFlag(true)
                        }
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "UPGRADE",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // About Section
            SettingsSection(title = "About") {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "App Version",
                    subtitle = "1.0.0",
                    onClick = { /* Show version info */ }
                )
                
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "Help & Support",
                    subtitle = "Get help with using the app",
                    onClick = { /* Navigate to help */ }
                )
                
                SettingsItem(
                    icon = Icons.Default.Lock,
                    title = "Privacy Policy",
                    subtitle = "Learn how we protect your data",
                    onClick = { /* Navigate to privacy policy */ }
                )
                
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "Terms of Service",
                    subtitle = "Read our terms and conditions",
                    onClick = { /* Navigate to terms */ }
                )
            }

            // Data Management Section
            SettingsSection(title = "Data Management") {
                SettingsItem(
                    icon = Icons.Default.Share,
                    title = "Export as JSON",
                    subtitle = "Download all your resume data as JSON file",
                    onClick = viewModel::exportResumes
                )
                
                SettingsItem(
                    icon = Icons.Default.Share,
                    title = "Import JSON",
                    subtitle = "Restore resumes from JSON backup file",
                    onClick = { filePickerLauncher.launch("application/json") }
                )
                
                SettingsItem(
                    icon = Icons.Default.Delete,
                    title = "Clear All Data",
                    subtitle = "Delete all resumes and settings",
                    onClick = { /* Show confirmation dialog */ },
                    isDestructive = true
                )
            }

            // Bottom padding
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
    
    // Success and error dialogs
    if (uiState.showExportSuccess) {
        AlertDialog(
            onDismissRequest = viewModel::dismissExportSuccess,
            title = { Text("Export Successful") },
            text = { Text("Your resumes have been exported successfully!") },
            confirmButton = {
                TextButton(onClick = viewModel::dismissExportSuccess) {
                    Text("OK")
                }
            }
        )
    }
    
    if (uiState.showImportSuccess) {
        AlertDialog(
            onDismissRequest = viewModel::dismissImportSuccess,
            title = { Text("Import Successful") },
            text = { Text("Successfully imported ${uiState.importedCount} resume(s)!") },
            confirmButton = {
                TextButton(onClick = viewModel::dismissImportSuccess) {
                    Text("OK")
                }
            }
        )
    }
    
    if (uiState.showClearDataSuccess) {
        AlertDialog(
            onDismissRequest = viewModel::dismissClearDataSuccess,
            title = { Text("Data Cleared") },
            text = { Text("All data has been cleared successfully!") },
            confirmButton = {
                TextButton(onClick = viewModel::dismissClearDataSuccess) {
                    Text("OK")
                }
            }
        )
    }
    
    uiState.errorMessage?.let { errorMessage ->
        AlertDialog(
            onDismissRequest = viewModel::dismissError,
            title = { Text("Error") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = viewModel::dismissError) {
                    Text("OK")
                }
            }
        )
    }
    
    // Loading overlay
    if (uiState.isExporting || uiState.isImporting) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (uiState.isExporting) "Exporting..." else "Importing...",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(4.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDestructive: Boolean = false,
    trailing: @Composable (() -> Unit)? = null
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (isDestructive) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (isDestructive) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (trailing != null) {
                Spacer(modifier = Modifier.width(8.dp))
                trailing()
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SettingsToggleItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}
