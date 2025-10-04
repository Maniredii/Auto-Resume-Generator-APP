package fm.mrc.resumebuilder.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import fm.mrc.resumebuilder.data.model.*
import fm.mrc.resumebuilder.data.template.TemplateManager
import fm.mrc.resumebuilder.ui.components.ImagePicker
import fm.mrc.resumebuilder.ui.components.ValidatedEmailField
import fm.mrc.resumebuilder.ui.components.ValidatedMultilineField
import fm.mrc.resumebuilder.ui.components.ValidatedPhoneField
import fm.mrc.resumebuilder.ui.components.ValidatedTextField
import fm.mrc.resumebuilder.ui.components.ValidatedUrlField
import fm.mrc.resumebuilder.ui.responsive.ResponsiveContainer
import fm.mrc.resumebuilder.ui.responsive.ResponsiveLayout
import fm.mrc.resumebuilder.ui.validation.ValidationState
import fm.mrc.resumebuilder.ui.validation.ValidationType
import fm.mrc.resumebuilder.ui.viewmodel.ResumeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResumeEditScreen(
    resumeId: String?,
    onNavigateBack: () -> Unit,
    onNavigateToPreview: (String) -> Unit,
    onNavigateToTemplateSelection: () -> Unit,
    viewModel: ResumeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    
    // Validation state
    val validationState = remember { ValidationState() }

    // Load resume or create new one
    LaunchedEffect(resumeId) {
        if (resumeId != null) {
            viewModel.loadResume(resumeId)
        } else {
            viewModel.createNewResume()
        }
    }

    // Show error message if any
    errorMessage?.let { message ->
        LaunchedEffect(message) {
            // You can show a snack bar here
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isNewResume) "New Resume" else "Edit Resume") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToTemplateSelection) {
                        Icon(Icons.Default.Settings, contentDescription = "Change Template")
                    }
                    if (!uiState.isNewResume) {
                        IconButton(
                            onClick = { onNavigateToPreview(uiState.id) }
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Preview")
                        }
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
                        onClick = onNavigateBack,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            // Validate form before saving
                            validationState.validatePersonalInfo(uiState.personal)
                            validationState.validateSummary(uiState.summary)
                            validationState.validateSkills(uiState.skills)
                            
                            if (validationState.errors.isEmpty()) {
                                viewModel.saveResume()
                                // Navigate to preview after saving
                                if (!uiState.isNewResume) {
                                    onNavigateToPreview(uiState.id)
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Save")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        ResponsiveContainer(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = ResponsiveLayout.getResponsivePadding(),
                verticalArrangement = Arrangement.spacedBy(ResponsiveLayout.getResponsiveSpacing())
            ) {
            // Personal Information Section
            item {
                PersonalInfoSection(
                    personalInfo = uiState.personal,
                    onPersonalInfoChange = viewModel::updatePersonalInfo,
                    validationState = validationState
                )
            }

            // Summary Section
            item {
                SummarySection(
                    summary = uiState.summary,
                    onSummaryChange = viewModel::updateSummary,
                    validationState = validationState
                )
            }

            // Skills Section
            item {
                SkillsSection(
                    skills = uiState.skills,
                    onAddSkill = viewModel::addSkill,
                    onRemoveSkill = viewModel::removeSkill
                )
            }

            // Education Section
            item {
                EducationSection(
                    education = uiState.education,
                    onEducationChange = viewModel::updateEducation,
                    onAddEducation = viewModel::addEducation,
                    onRemoveEducation = viewModel::removeEducation
                )
            }

            // Experience Section
            item {
                ExperienceSection(
                    experience = uiState.experience,
                    onExperienceChange = viewModel::updateExperience,
                    onAddExperience = viewModel::addExperience,
                    onRemoveExperience = viewModel::removeExperience
                )
            }

            // Projects Section
            item {
                ProjectsSection(
                    projects = uiState.projects,
                    onProjectsChange = viewModel::updateProjects,
                    onAddProject = viewModel::addProject,
                    onRemoveProject = viewModel::removeProject
                )
            }

            // Bottom padding for floating action button
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun PersonalInfoSection(
    personalInfo: PersonalInfo,
    onPersonalInfoChange: (PersonalInfo) -> Unit,
    validationState: ValidationState
) {
    SectionCard(title = "Personal Information") {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Avatar picker
            ImagePicker(
                currentImageUri = personalInfo.avatarUri?.let { android.net.Uri.parse(it) },
                onImageSelected = { uri ->
                    onPersonalInfoChange(personalInfo.copy(avatarUri = uri?.toString()))
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            
            ValidatedTextField(
                value = personalInfo.fullName,
                onValueChange = { onPersonalInfoChange(personalInfo.copy(fullName = it)) },
                label = "Full Name",
                validationState = validationState,
                fieldName = "fullName",
                validationType = ValidationType.REQUIRED,
                modifier = Modifier.fillMaxWidth()
            )
            ValidatedTextField(
                value = personalInfo.title,
                onValueChange = { onPersonalInfoChange(personalInfo.copy(title = it)) },
                label = "Professional Title",
                validationState = validationState,
                fieldName = "title",
                validationType = ValidationType.MIN_LENGTH,
                isRequired = false,
                modifier = Modifier.fillMaxWidth()
            )
            ValidatedEmailField(
                value = personalInfo.email,
                onValueChange = { onPersonalInfoChange(personalInfo.copy(email = it)) },
                validationState = validationState,
                fieldName = "email",
                modifier = Modifier.fillMaxWidth()
            )
            ValidatedPhoneField(
                value = personalInfo.phone,
                onValueChange = { onPersonalInfoChange(personalInfo.copy(phone = it)) },
                validationState = validationState,
                fieldName = "phone",
                modifier = Modifier.fillMaxWidth()
            )
            ValidatedTextField(
                value = personalInfo.location,
                onValueChange = { onPersonalInfoChange(personalInfo.copy(location = it)) },
                label = "Location",
                validationState = validationState,
                fieldName = "location",
                validationType = ValidationType.MIN_LENGTH,
                isRequired = false,
                modifier = Modifier.fillMaxWidth()
            )
            ValidatedUrlField(
                value = personalInfo.linkedin,
                onValueChange = { onPersonalInfoChange(personalInfo.copy(linkedin = it)) },
                validationState = validationState,
                fieldName = "linkedin",
                label = "LinkedIn",
                placeholder = "https://linkedin.com/in/yourprofile",
                modifier = Modifier.fillMaxWidth()
            )
            ValidatedUrlField(
                value = personalInfo.github,
                onValueChange = { onPersonalInfoChange(personalInfo.copy(github = it)) },
                validationState = validationState,
                fieldName = "github",
                label = "GitHub",
                placeholder = "https://github.com/yourusername",
                modifier = Modifier.fillMaxWidth()
            )
            ValidatedUrlField(
                value = personalInfo.website,
                onValueChange = { onPersonalInfoChange(personalInfo.copy(website = it)) },
                validationState = validationState,
                fieldName = "website",
                label = "Website",
                placeholder = "https://yourwebsite.com",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SummarySection(
    summary: String,
    onSummaryChange: (String) -> Unit,
    validationState: ValidationState
) {
    SectionCard(title = "Professional Summary") {
        ValidatedMultilineField(
            value = summary,
            onValueChange = onSummaryChange,
            validationState = validationState,
            fieldName = "summary",
            label = "Summary",
            placeholder = "Write a brief professional summary highlighting your key achievements and skills...",
            maxLines = 4,
            maxLength = 500
        )
    }
}

@Composable
private fun SkillsSection(
    skills: List<String>,
    onAddSkill: (String) -> Unit,
    onRemoveSkill: (String) -> Unit
) {
    var newSkill by remember { mutableStateOf("") }

    SectionCard(title = "Skills") {
        // Add skill input
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newSkill,
                onValueChange = { newSkill = it },
                label = { Text("Add skill") },
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = {
                    if (newSkill.isNotBlank()) {
                        onAddSkill(newSkill)
                        newSkill = ""
                    }
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add skill")
            }
        }

        if (skills.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            // Skills chips
            LazyColumn(
                modifier = Modifier.heightIn(max = 200.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(skills) { _, skill ->
                    SkillChip(
                        skill = skill,
                        onRemove = { onRemoveSkill(skill) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SkillChip(
    skill: String,
    onRemove: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = skill,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove skill",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun EducationSection(
    education: List<Education>,
    onEducationChange: (List<Education>) -> Unit,
    onAddEducation: () -> Unit,
    onRemoveEducation: (String) -> Unit
) {
    SectionCard(title = "Education") {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            education.forEach { edu ->
                EducationItem(
                    education = edu,
                    onEducationChange = { updatedEdu ->
                        val updatedList = education.map { 
                            if (it.id == updatedEdu.id) updatedEdu else it 
                        }
                        onEducationChange(updatedList)
                    },
                    onRemove = { onRemoveEducation(edu.id) }
                )
            }
            
            OutlinedButton(
                onClick = onAddEducation,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Education")
            }
        }
    }
}

@Composable
private fun EducationItem(
    education: Education,
    onEducationChange: (Education) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Education Entry",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Remove education",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            OutlinedTextField(
                value = education.institution,
                onValueChange = { onEducationChange(education.copy(institution = it)) },
                label = { Text("Institution") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = education.degree,
                onValueChange = { onEducationChange(education.copy(degree = it)) },
                label = { Text("Degree") },
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = education.start,
                    onValueChange = { onEducationChange(education.copy(start = it)) },
                    label = { Text("Start Year") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = education.end,
                    onValueChange = { onEducationChange(education.copy(end = it)) },
                    label = { Text("End Year") },
                    modifier = Modifier.weight(1f)
                )
            }
            OutlinedTextField(
                value = education.details,
                onValueChange = { onEducationChange(education.copy(details = it)) },
                label = { Text("Details (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
        }
    }
}

@Composable
private fun ExperienceSection(
    experience: List<Experience>,
    onExperienceChange: (List<Experience>) -> Unit,
    onAddExperience: () -> Unit,
    onRemoveExperience: (String) -> Unit
) {
    SectionCard(title = "Work Experience") {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            experience.forEach { exp ->
                ExperienceItem(
                    experience = exp,
                    onExperienceChange = { updatedExp ->
                        val updatedList = experience.map { 
                            if (it.id == updatedExp.id) updatedExp else it 
                        }
                        onExperienceChange(updatedList)
                    },
                    onRemove = { onRemoveExperience(exp.id) }
                )
            }
            
            OutlinedButton(
                onClick = onAddExperience,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Experience")
            }
        }
    }
}

@Composable
private fun ExperienceItem(
    experience: Experience,
    onExperienceChange: (Experience) -> Unit,
    onRemove: () -> Unit
) {
    var newBullet by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Work Experience",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Remove experience",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            OutlinedTextField(
                value = experience.company,
                onValueChange = { onExperienceChange(experience.copy(company = it)) },
                label = { Text("Company") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = experience.role,
                onValueChange = { onExperienceChange(experience.copy(role = it)) },
                label = { Text("Role/Position") },
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = experience.start,
                    onValueChange = { onExperienceChange(experience.copy(start = it)) },
                    label = { Text("Start Date") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = experience.end,
                    onValueChange = { onExperienceChange(experience.copy(end = it)) },
                    label = { Text("End Date") },
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Bullet points
            Text(
                text = "Key Achievements/Responsibilities:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            // Add bullet input
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newBullet,
                    onValueChange = { newBullet = it },
                    label = { Text("Add bullet point") },
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = {
                        if (newBullet.isNotBlank()) {
                            val updatedBullets = experience.bullets + newBullet
                            onExperienceChange(experience.copy(bullets = updatedBullets))
                            newBullet = ""
                        }
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add bullet")
                }
            }
            
            // Display bullets
            experience.bullets.forEach { bullet ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "• $bullet",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            val updatedBullets = experience.bullets - bullet
                            onExperienceChange(experience.copy(bullets = updatedBullets))
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Remove bullet",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectsSection(
    projects: List<Project>,
    onProjectsChange: (List<Project>) -> Unit,
    onAddProject: () -> Unit,
    onRemoveProject: (String) -> Unit
) {
    SectionCard(title = "Projects") {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            projects.forEach { project ->
                ProjectItem(
                    project = project,
                    onProjectChange = { updatedProject ->
                        val updatedList = projects.map { 
                            if (it.id == updatedProject.id) updatedProject else it 
                        }
                        onProjectsChange(updatedList)
                    },
                    onRemove = { onRemoveProject(project.id) }
                )
            }
            
            OutlinedButton(
                onClick = onAddProject,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Project")
            }
        }
    }
}

@Composable
private fun ProjectItem(
    project: Project,
    onProjectChange: (Project) -> Unit,
    onRemove: () -> Unit
) {
    var newTech by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Project",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Remove project",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            OutlinedTextField(
                value = project.title,
                onValueChange = { onProjectChange(project.copy(title = it)) },
                label = { Text("Project Title") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = project.description,
                onValueChange = { onProjectChange(project.copy(description = it)) },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
            OutlinedTextField(
                value = project.link,
                onValueChange = { onProjectChange(project.copy(link = it)) },
                label = { Text("Link (optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            
            // Tech stack
            Text(
                text = "Technologies:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            // Add tech input
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newTech,
                    onValueChange = { newTech = it },
                    label = { Text("Add technology") },
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = {
                        if (newTech.isNotBlank() && !project.tech.contains(newTech)) {
                            val updatedTech = project.tech + newTech
                            onProjectChange(project.copy(tech = updatedTech))
                            newTech = ""
                        }
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add technology")
                }
            }
            
            // Display tech chips
            if (project.tech.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    itemsIndexed(project.tech) { _, tech ->
                        SkillChip(
                            skill = tech,
                            onRemove = {
                                val updatedTech = project.tech - tech
                                onProjectChange(project.copy(tech = updatedTech))
                            }
                        )
                    }
                }
            }
        }
    }
}
