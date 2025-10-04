package fm.mrc.resumebuilder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import fm.mrc.resumebuilder.data.db.ResumeDatabase
import fm.mrc.resumebuilder.navigation.AppNavHost
import fm.mrc.resumebuilder.navigation.navigateToSettings
import fm.mrc.resumebuilder.ui.theme.ResumeBuilderTheme

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
    
    AppNavHost(
        navController = navController,
        database = database,
        modifier = Modifier.fillMaxSize()
    )
}
