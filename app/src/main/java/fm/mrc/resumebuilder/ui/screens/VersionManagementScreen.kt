package fm.mrc.resumebuilder.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import fm.mrc.resumebuilder.data.model.ResumeVersionSummary
import fm.mrc.resumebuilder.data.version.VersionManager
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VersionManagementScreen(
    resumeId: String,
    onNavigateBack: () -> Unit,
    onVersionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val versionManager = remember { VersionManager() }
    var versions by remember { mutableStateOf<List<ResumeVersionSummary>>(emptyList()) }
    var showCreateDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(resumeId) {
        versions = versionManager.getVersionSummaries(resumeId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resume Versions") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Create Version")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Version")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (versions.isEmpty()) {
                EmptyVersionsState(
                    onCreateVersion = { showCreateDialog = true }
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(versions) { version ->
                        VersionCard(
                            version = version,
                            onVersionClick = { onVersionSelected(version.id) },
                            onSetActive = {
                                versionManager.setActiveVersion(resumeId, version.id)
                                versions = versionManager.getVersionSummaries(resumeId)
                            },
                            onDelete = {
                                versionManager.deleteVersion(resumeId, version.id)
                                versions = versionManager.getVersionSummaries(resumeId)
                            }
                        )
                    }
                }
            }
        }
    }
    
    // Create Version Dialog
    if (showCreateDialog) {
        CreateVersionDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, description ->
                // Create new version logic would go here
                showCreateDialog = false
                versions = versionManager.getVersionSummaries(resumeId)
            }
        )
    }
}

@Composable
private fun EmptyVersionsState(
    onCreateVersion: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.History,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No Versions Yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Create your first version to start tracking changes",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(onClick = onCreateVersion) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create Version")
        }
    }
}

@Composable
private fun VersionCard(
    version: ResumeVersionSummary,
    onVersionClick: () -> Unit,
    onSetActive: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onVersionClick() },
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (version.isActive) 8.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = version.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        if (version.isActive) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "ACTIVE",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    
                    if (version.description != null) {
                        Text(
                            text = version.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "v${version.versionNumber}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Text(
                            text = version.createdAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Text(
                            text = "${version.changeCount} changes",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        if (!version.isActive) {
                            DropdownMenuItem(
                                text = { Text("Set as Active") },
                                onClick = {
                                    onSetActive()
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Star, contentDescription = null)
                                }
                            )
                        }
                        
                        DropdownMenuItem(
                            text = { Text("Compare") },
                            onClick = {
                                // Navigate to comparison screen
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.CompareArrows, contentDescription = null)
                            }
                        )
                        
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                onDelete()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateVersionDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var versionName by remember { mutableStateOf("") }
    var versionDescription by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Version") },
        text = {
            Column {
                OutlinedTextField(
                    value = versionName,
                    onValueChange = { versionName = it },
                    label = { Text("Version Name") },
                    placeholder = { Text("e.g., Updated for Software Engineer role") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = versionDescription,
                    onValueChange = { versionDescription = it },
                    label = { Text("Description (Optional)") },
                    placeholder = { Text("Describe the changes made...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(versionName, versionDescription.takeIf { it.isNotBlank() }) },
                enabled = versionName.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
