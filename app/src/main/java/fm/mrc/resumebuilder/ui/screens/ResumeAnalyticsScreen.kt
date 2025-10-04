package fm.mrc.resumebuilder.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Info
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
import fm.mrc.resumebuilder.data.analytics.ResumeAnalytics
import fm.mrc.resumebuilder.data.model.Resume

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResumeAnalyticsScreen(
    resume: Resume,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val analytics = remember(resume) {
        ResumeAnalytics.calculateResumeScore(resume)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resume Analytics") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            // Overall Score Card
            OverallScoreCard(analytics = analytics)
            
            // Score Breakdown
            ScoreBreakdownCard(analytics = analytics)
            
            // Strengths
            if (analytics.strengths.isNotEmpty()) {
                StrengthsCard(strengths = analytics.strengths)
            }
            
            // Weaknesses
            if (analytics.weaknesses.isNotEmpty()) {
                WeaknessesCard(weaknesses = analytics.weaknesses)
            }
            
            // Suggestions
            if (analytics.suggestions.isNotEmpty()) {
                SuggestionsCard(suggestions = analytics.suggestions)
            }
        }
    }
}

@Composable
private fun OverallScoreCard(
    analytics: ResumeAnalytics.ResumeScore,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Score Circle
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(getScoreColor(analytics.overallScore))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${analytics.overallScore}",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Score",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Score Description
            Text(
                text = ResumeAnalytics.getScoreColor(analytics.overallScore),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = getScoreTextColor(analytics.overallScore)
            )
            
            Text(
                text = ResumeAnalytics.getScoreEmoji(analytics.overallScore),
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}

@Composable
private fun ScoreBreakdownCard(
    analytics: ResumeAnalytics.ResumeScore,
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
                text = "Score Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Score items
            ScoreItem(
                label = "Completeness",
                score = analytics.completenessScore,
                icon = Icons.Default.CheckCircle
            )
            
            ScoreItem(
                label = "Content Quality",
                score = analytics.contentScore,
                icon = Icons.Default.Star
            )
            
            ScoreItem(
                label = "Formatting",
                score = analytics.formattingScore,
                icon = Icons.Default.Info
            )
            
            ScoreItem(
                label = "ATS Optimization",
                score = analytics.atsScore,
                icon = Icons.Default.Star
            )
        }
    }
}

@Composable
private fun ScoreItem(
    label: String,
    score: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = getScoreColor(score),
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = "$score%",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = getScoreTextColor(score)
        )
    }
}

@Composable
private fun StrengthsCard(
    strengths: List<String>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Strengths",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            strengths.forEach { strength ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "• ",
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = strength,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun WeaknessesCard(
    weaknesses: List<String>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Areas for Improvement",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            weaknesses.forEach { weakness ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "• ",
                        color = Color(0xFFFF9800),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = weakness,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun SuggestionsCard(
    suggestions: List<String>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Suggestions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            suggestions.forEachIndexed { index, suggestion ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "${index + 1}. ",
                        color = Color(0xFF2196F3),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = suggestion,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

// Helper functions for colors
private fun getScoreColor(score: Int): Color {
    return when {
        score >= 90 -> Color(0xFF4CAF50) // Green
        score >= 80 -> Color(0xFF8BC34A) // Light Green
        score >= 70 -> Color(0xFFFFC107) // Amber
        score >= 60 -> Color(0xFFFF9800) // Orange
        else -> Color(0xFFF44336) // Red
    }
}

private fun getScoreTextColor(score: Int): Color {
    return when {
        score >= 90 -> Color(0xFF4CAF50)
        score >= 80 -> Color(0xFF8BC34A)
        score >= 70 -> Color(0xFFFFC107)
        score >= 60 -> Color(0xFFFF9800)
        else -> Color(0xFFF44336)
    }
}
