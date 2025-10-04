package fm.mrc.resumebuilder.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import fm.mrc.resumebuilder.data.model.*
import fm.mrc.resumebuilder.ui.viewmodel.ResumeViewModel
import fm.mrc.resumebuilder.utils.PdfExporter
import fm.mrc.resumebuilder.utils.PdfExportHelper
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResumePreviewScreen(
    resumeId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: () -> Unit,
    viewModel: ResumeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val pdfExporter = remember { PdfExporter() }
    val pdfExportHelper = remember { PdfExportHelper() }
    
    var isExporting by remember { mutableStateOf(false) }
    var exportError by remember { mutableStateOf<String?>(null) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    
    // Helper function to create Resume from UI state
    fun createResumeFromState(): Resume {
        return Resume(
            id = uiState.id,
            metadata = ResumeMetadata(
                createdAt = uiState.createdAt,
                updatedAt = uiState.updatedAt,
                template = uiState.template
            ),
            personal = uiState.personal,
            summary = uiState.summary,
            skills = uiState.skills,
            education = uiState.education,
            experience = uiState.experience,
            projects = uiState.projects
        )
    }
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, proceed with PDF export
            scope.launch {
                pdfExportHelper.exportToPdf(
                    context, 
                    pdfExporter, 
                    createResumeFromState(), 
                    uiState,
                    onSuccess = { 
                        isExporting = false
                        showSuccessDialog = true
                    },
                    onError = { error ->
                        isExporting = false
                        exportError = error
                    }
                )
            }
        } else {
            isExporting = false
            exportError = "Storage permission is required to save PDF files"
        }
    }
    
    // Helper function to check and request permissions
    fun checkPermissionsAndExport() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11+, we don't need WRITE_EXTERNAL_STORAGE
            scope.launch {
                isExporting = true
                exportError = null
                pdfExportHelper.exportToPdf(
                    context, 
                    pdfExporter, 
                    createResumeFromState(), 
                    uiState,
                    onSuccess = { 
                        isExporting = false
                        showSuccessDialog = true
                    },
                    onError = { error ->
                        isExporting = false
                        exportError = error
                    }
                )
            }
        } else {
            // For Android 10 and below, check WRITE_EXTERNAL_STORAGE permission
            when (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                PackageManager.PERMISSION_GRANTED -> {
                    scope.launch {
                        isExporting = true
                        exportError = null
                        pdfExportHelper.exportToPdf(
                            context, 
                            pdfExporter, 
                            createResumeFromState(), 
                            uiState,
                            onSuccess = { 
                                isExporting = false
                                showSuccessDialog = true
                            },
                            onError = { error ->
                                isExporting = false
                                exportError = error
                            }
                        )
                    }
                }
                else -> {
                    permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }
        }
    }

    // Load resume
    LaunchedEffect(resumeId) {
        viewModel.loadResume(resumeId)
    }

    // Show error message if any
    errorMessage?.let { message ->
        LaunchedEffect(message) {
            // You can show a snackbar here
            viewModel.clearError()
        }
    }
    
    // Show export error if any
    exportError?.let { message ->
        LaunchedEffect(message) {
            // You can show a snackbar here
            exportError = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resume Preview") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (!isExporting && uiState.id.isNotBlank()) {
                                scope.launch {
                                    isExporting = true
                                    exportError = null
                                    try {
                                        val resume = createResumeFromState()
                                        val pdfUri = pdfExporter.exportResumeToPdf(context, resume)
                                        pdfExporter.shareResumePdf(
                                            context, 
                                            pdfUri, 
                                            resume.personal.fullName.ifBlank { "Resume" }
                                        )
                                    } catch (e: Exception) {
                                        exportError = e.message
                                    } finally {
                                        isExporting = false
                                    }
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isExporting && uiState.id.isNotBlank()
                    ) {
                        if (isExporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Share")
                    }
                        Button(
                            onClick = {
                                if (!isExporting && uiState.id.isNotBlank()) {
                                    checkPermissionsAndExport()
                                }
                            },
                        modifier = Modifier.weight(1f),
                        enabled = !isExporting && uiState.id.isNotBlank()
                    ) {
                        if (isExporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export PDF")
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.id.isBlank() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Resume not found",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = onNavigateBack) {
                            Text("Go Back")
                        }
                    }
                }
                else -> {
                    ResumePreviewContent(
                        resume = createResumeFromState()
                    )
                }
            }
        }
    }
    
    // Success dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("PDF Exported Successfully") },
            text = { Text("Your resume has been exported as PDF and saved to your device.") },
            confirmButton = {
                TextButton(onClick = { showSuccessDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
    
    // Error dialog
    exportError?.let { error ->
        AlertDialog(
            onDismissRequest = { exportError = null },
            title = { Text("Export Error") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { exportError = null }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun ResumePreviewContent(
    resume: Resume,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header Section
        ResumeHeader(personal = resume.personal)

        // Summary Section
        if (resume.summary.isNotBlank()) {
            ResumeSection(title = "Professional Summary") {
                Text(
                    text = resume.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.4
                )
            }
        }

        // Skills Section
        if (resume.skills.isNotEmpty()) {
            ResumeSection(title = "Skills") {
                SkillsGrid(skills = resume.skills)
            }
        }

        // Experience Section
        if (resume.experience.isNotEmpty()) {
            ResumeSection(title = "Work Experience") {
                Column(
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    resume.experience.forEach { experience ->
                        ExperiencePreviewItem(experience = experience)
                    }
                }
            }
        }

        // Education Section
        if (resume.education.isNotEmpty()) {
            ResumeSection(title = "Education") {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    resume.education.forEach { education ->
                        EducationPreviewItem(education = education)
                    }
                }
            }
        }

        // Projects Section
        if (resume.projects.isNotEmpty()) {
            ResumeSection(title = "Projects") {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    resume.projects.forEach { project ->
                        ProjectPreviewItem(project = project)
                    }
                }
            }
        }

        // Bottom padding
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun ResumeHeader(
    personal: PersonalInfo,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar
        if (personal.avatarUri != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(personal.avatarUri)
                    .crossfade(true)
                    .build(),
                contentDescription = "Profile Avatar",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Name
        Text(
            text = personal.fullName,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )

        // Title
        if (personal.title.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = personal.title,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Contact Information
        ContactInfoGrid(personal = personal)

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 16.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        )
    }
}

@Composable
private fun ContactInfoGrid(
    personal: PersonalInfo,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (personal.email.isNotBlank()) {
                ContactInfoItem(
                    icon = Icons.Default.Email,
                    text = personal.email
                )
            }
            if (personal.phone.isNotBlank()) {
                ContactInfoItem(
                    icon = Icons.Default.Phone,
                    text = personal.phone
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (personal.location.isNotBlank()) {
                ContactInfoItem(
                    icon = Icons.Default.LocationOn,
                    text = personal.location
                )
            }
            if (personal.linkedin.isNotBlank()) {
                ContactInfoItem(
                    icon = Icons.Default.Person,
                    text = personal.linkedin
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (personal.github.isNotBlank()) {
                ContactInfoItem(
                    icon = Icons.Default.Person,
                    text = personal.github
                )
            }
            if (personal.website.isNotBlank()) {
                ContactInfoItem(
                    icon = Icons.Default.Person,
                    text = personal.website
                )
            }
        }
    }
}

@Composable
private fun ContactInfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ResumeSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun SkillsGrid(
    skills: List<String>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        skills.chunked(3).forEach { rowSkills ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowSkills.forEach { skill ->
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = skill,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                // Fill remaining space if row is not complete
                repeat(3 - rowSkills.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ExperiencePreviewItem(
    experience: Experience,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Company and Role
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = experience.role,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = experience.company,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Text(
                text = "${experience.start} - ${experience.end}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }

        if (experience.bullets.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                experience.bullets.forEach { bullet ->
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = bullet,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.3
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EducationPreviewItem(
    education: Education,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = education.degree,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = education.institution,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Text(
                text = "${education.start} - ${education.end}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }

        if (education.details.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = education.details,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.3
            )
        }
    }
}

@Composable
private fun ProjectPreviewItem(
    project: Project,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = project.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        
        if (project.link.isNotBlank()) {
            Text(
                text = project.link,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }

        if (project.description.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = project.description,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.3
            )
        }

        if (project.tech.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tech:",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                project.tech.take(5).forEach { tech ->
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = tech,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                if (project.tech.size > 5) {
                    Text(
                        text = "+${project.tech.size - 5} more",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
