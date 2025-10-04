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
import fm.mrc.resumebuilder.utils.XmlTemplateRenderer
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import android.net.Uri
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
    var showShareDialog by remember { mutableStateOf(false) }
    var pdfUri by remember { mutableStateOf<Uri?>(null) }
    
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
                title = { 
                    Text(
                        text = "Resume Preview",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToEdit) {
                        Icon(
                            Icons.Default.Edit, 
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
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
                                        val generatedPdfUri = pdfExporter.exportResumeToPdf(context, resume)
                                        pdfUri = generatedPdfUri
                                        showShareDialog = true
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
                        resume = createResumeFromState(),
                        template = uiState.template
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

    // Share dialog
    if (showShareDialog && pdfUri != null) {
        ShareDialog(
            pdfUri = pdfUri!!,
            resumeName = uiState.personal.fullName.ifBlank { "Resume" },
            onDismiss = { showShareDialog = false },
            pdfExporter = pdfExporter
        )
    }
}

@Composable
private fun ResumePreviewContent(
    resume: Resume,
    template: String = "simple",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Check if this is an XML template
    val isXmlTemplate = template.endsWith("_xml")
    
    if (isXmlTemplate) {
        // Render XML template
        AndroidView(
            factory = { context ->
                XmlTemplateRenderer.renderResumeToView(context, resume, template)
            },
            modifier = modifier.fillMaxSize()
        )
    } else {
        // Render Compose template based on template selection
        when (template) {
            "simple" -> SimpleTemplatePreview(resume = resume, modifier = modifier)
            "modern" -> ModernTemplatePreview(resume = resume, modifier = modifier)
            "creative" -> CreativeTemplatePreview(resume = resume, modifier = modifier)
            else -> ComposeResumePreview(resume = resume, modifier = modifier)
        }
    }
}

@Composable
private fun ComposeResumePreview(
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

@Composable
private fun ShareDialog(
    pdfUri: Uri,
    resumeName: String,
    onDismiss: () -> Unit,
    pdfExporter: PdfExporter
) {
    val context = LocalContext.current
    val sharingOptions = remember { pdfExporter.getAvailableSharingOptions(context) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Share Resume") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sharingOptions) { option ->
                    SharingOptionItem(
                        option = option,
                        onClick = {
                            when (option.name) {
                                "General Share" -> pdfExporter.shareResumePdf(context, pdfUri, resumeName)
                                "WhatsApp" -> pdfExporter.shareResumeViaWhatsApp(context, pdfUri, resumeName)
                                "LinkedIn" -> pdfExporter.shareResumeViaLinkedIn(context, pdfUri, resumeName)
                                "Telegram" -> pdfExporter.shareResumeViaTelegram(context, pdfUri, resumeName)
                                "Gmail" -> pdfExporter.shareResumeViaEmail(context, pdfUri, resumeName)
                            }
                            onDismiss()
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun SharingOptionItem(
    option: PdfExporter.SharingOption,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = option.icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = option.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = option.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Simple Template - Clean and minimal design
@Composable
private fun SimpleTemplatePreview(
    resume: Resume,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header Section - Minimal
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = resume.personal.fullName,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (resume.personal.title.isNotBlank()) {
                Text(
                    text = resume.personal.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Contact info in simple horizontal layout
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.wrapContentWidth()
            ) {
                if (resume.personal.email.isNotBlank()) {
                    Text(
                        text = resume.personal.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (resume.personal.phone.isNotBlank()) {
                    Text(
                        text = resume.personal.phone,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (resume.personal.location.isNotBlank()) {
                    Text(
                        text = resume.personal.location,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        Divider(color = MaterialTheme.colorScheme.outline)
        
        // Summary Section
        if (resume.summary.isNotBlank()) {
            SimpleSection(
                title = "Summary",
                content = {
                    Text(
                        text = resume.summary,
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.4
                    )
                }
            )
        }
        
        // Skills Section
        if (resume.skills.isNotEmpty()) {
            SimpleSection(
                title = "Skills",
                content = {
                    Text(
                        text = resume.skills.joinToString(" • "),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            )
        }
        
        // Experience Section
        if (resume.experience.isNotEmpty()) {
            SimpleSection(
                title = "Experience",
                content = {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        resume.experience.forEach { experience ->
                            SimpleExperienceItem(experience = experience)
                        }
                    }
                }
            )
        }
        
        // Education Section
        if (resume.education.isNotEmpty()) {
            SimpleSection(
                title = "Education",
                content = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        resume.education.forEach { education ->
                            SimpleEducationItem(education = education)
                        }
                    }
                }
            )
        }
        
        // Projects Section
        if (resume.projects.isNotEmpty()) {
            SimpleSection(
                title = "Projects",
                content = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        resume.projects.forEach { project ->
                            SimpleProjectItem(project = project)
                        }
                    }
                }
            )
        }
    }
}

// Modern Template - Contemporary design with cards
@Composable
private fun ModernTemplatePreview(
    resume: Resume,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header Section with accent color
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = resume.personal.fullName,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                if (resume.personal.title.isNotBlank()) {
                    Text(
                        text = resume.personal.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Contact info in grid
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    if (resume.personal.email.isNotBlank()) {
                        item {
                            ModernContactItem(
                                icon = Icons.Default.Email,
                                text = resume.personal.email
                            )
                        }
                    }
                    if (resume.personal.phone.isNotBlank()) {
                        item {
                            ModernContactItem(
                                icon = Icons.Default.Phone,
                                text = resume.personal.phone
                            )
                        }
                    }
                    if (resume.personal.location.isNotBlank()) {
                        item {
                            ModernContactItem(
                                icon = Icons.Default.LocationOn,
                                text = resume.personal.location
                            )
                        }
                    }
                }
            }
        }
        
        // Summary Section
        if (resume.summary.isNotBlank()) {
            ModernSection(
                title = "Professional Summary",
                content = {
                    Text(
                        text = resume.summary,
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.4
                    )
                }
            )
        }
        
        // Skills Section
        if (resume.skills.isNotEmpty()) {
            ModernSection(
                title = "Skills",
                content = {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        resume.skills.forEach { skill ->
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                                    )
                                ) {
                                    Text(
                                        text = skill,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            )
        }
        
        // Experience Section
        if (resume.experience.isNotEmpty()) {
            ModernSection(
                title = "Work Experience",
                content = {
                    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        resume.experience.forEach { experience ->
                            ModernExperienceItem(experience = experience)
                        }
                    }
                }
            )
        }
        
        // Education Section
        if (resume.education.isNotEmpty()) {
            ModernSection(
                title = "Education",
                content = {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        resume.education.forEach { education ->
                            ModernEducationItem(education = education)
                        }
                    }
                }
            )
        }
        
        // Projects Section
        if (resume.projects.isNotEmpty()) {
            ModernSection(
                title = "Projects",
                content = {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        resume.projects.forEach { project ->
                            ModernProjectItem(project = project)
                        }
                    }
                }
            )
        }
    }
}

// Creative Template - Artistic and bold design
@Composable
private fun CreativeTemplatePreview(
    resume: Resume,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header Section with creative styling
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = resume.personal.fullName,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                if (resume.personal.title.isNotBlank()) {
                    Text(
                        text = resume.personal.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Contact info with icons
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (resume.personal.email.isNotBlank()) {
                        CreativeContactItem(
                            icon = Icons.Default.Email,
                            text = resume.personal.email
                        )
                    }
                    if (resume.personal.phone.isNotBlank()) {
                        CreativeContactItem(
                            icon = Icons.Default.Phone,
                            text = resume.personal.phone
                        )
                    }
                    if (resume.personal.location.isNotBlank()) {
                        CreativeContactItem(
                            icon = Icons.Default.LocationOn,
                            text = resume.personal.location
                        )
                    }
                }
            }
        }
        
        // Summary Section
        if (resume.summary.isNotBlank()) {
            CreativeSection(
                title = "About Me",
                content = {
                    Text(
                        text = resume.summary,
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.4
                    )
                }
            )
        }
        
        // Skills Section
        if (resume.skills.isNotEmpty()) {
            CreativeSection(
                title = "Skills & Expertise",
                content = {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        resume.skills.forEach { skill ->
                            item {
                                Surface(
                                    modifier = Modifier.clip(RoundedCornerShape(20.dp)),
                                    color = MaterialTheme.colorScheme.tertiaryContainer
                                ) {
                                    Text(
                                        text = skill,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            )
        }
        
        // Experience Section
        if (resume.experience.isNotEmpty()) {
            CreativeSection(
                title = "Professional Experience",
                content = {
                    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        resume.experience.forEach { experience ->
                            CreativeExperienceItem(experience = experience)
                        }
                    }
                }
            )
        }
        
        // Education Section
        if (resume.education.isNotEmpty()) {
            CreativeSection(
                title = "Education",
                content = {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        resume.education.forEach { education ->
                            CreativeEducationItem(education = education)
                        }
                    }
                }
            )
        }
        
        // Projects Section
        if (resume.projects.isNotEmpty()) {
            CreativeSection(
                title = "Featured Projects",
                content = {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        resume.projects.forEach { project ->
                            CreativeProjectItem(project = project)
                        }
                    }
                }
            )
        }
    }
}
