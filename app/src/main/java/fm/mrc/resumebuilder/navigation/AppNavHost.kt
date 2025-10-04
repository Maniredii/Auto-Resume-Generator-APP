package fm.mrc.resumebuilder.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import fm.mrc.resumebuilder.data.db.ResumeDatabase
import fm.mrc.resumebuilder.data.repo.ResumeRepositoryImpl
import fm.mrc.resumebuilder.ui.screens.*
import fm.mrc.resumebuilder.ui.viewmodel.ResumeListViewModel
import fm.mrc.resumebuilder.ui.viewmodel.ResumeViewModel

/**
 * Navigation routes for the app
 */
object AppRoutes {
    const val RESUME_LIST = "resume_list"
    const val RESUME_EDIT = "resume_edit"
    const val RESUME_EDIT_WITH_ID = "resume_edit/{resumeId}"
    const val RESUME_PREVIEW = "resume_preview/{resumeId}"
    const val SETTINGS = "settings"
    const val TEMPLATE_SELECTION = "template_selection"
    
    fun resumeEdit(resumeId: String? = null): String {
        return if (resumeId != null) {
            "resume_edit/$resumeId"
        } else {
            RESUME_EDIT
        }
    }
    
    fun resumePreview(resumeId: String): String {
        return "resume_preview/$resumeId"
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    database: ResumeDatabase,
    selectedTemplate: String = "simple",
    onTemplateSelected: (String) -> Unit = {},
    onShowTemplateDialog: () -> Unit = {}
) {
    val repository = ResumeRepositoryImpl(database.resumeDao())
    
    NavHost(
        navController = navController,
        startDestination = AppRoutes.RESUME_LIST,
        modifier = modifier
    ) {
        // Resume List Screen
        composable(AppRoutes.RESUME_LIST) {
            val viewModel: ResumeListViewModel = viewModel(
                factory = ResumeListViewModel.Factory(repository)
            )
            
            ResumeListScreen(
                onNavigateToEdit = { resumeId ->
                    if (resumeId != null) {
                        navController.navigate(AppRoutes.resumeEdit(resumeId))
                    } else {
                        navController.navigate(AppRoutes.RESUME_EDIT)
                    }
                },
                onNavigateToPreview = { resumeId ->
                    navController.navigate(AppRoutes.resumePreview(resumeId))
                },
                viewModel = viewModel
            )
        }
        
        // Resume Edit Screen (New Resume)
        composable(AppRoutes.RESUME_EDIT) {
            val viewModel: ResumeViewModel = viewModel(
                factory = ResumeViewModel.Factory(repository)
            )
            
            ResumeEditScreen(
                resumeId = null,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToPreview = { resumeId ->
                    navController.navigate(AppRoutes.resumePreview(resumeId)) {
                        // Clear the edit screen from back stack
                        popUpTo(AppRoutes.RESUME_LIST)
                    }
                },
                onNavigateToTemplateSelection = {
                    navController.navigate(AppRoutes.TEMPLATE_SELECTION)
                },
                viewModel = viewModel
            )
        }
        
        // Resume Edit Screen (Edit Existing)
        composable(
            route = AppRoutes.RESUME_EDIT_WITH_ID,
            arguments = listOf(
                navArgument("resumeId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val resumeId = backStackEntry.arguments?.getString("resumeId")
            val viewModel: ResumeViewModel = viewModel(
                factory = ResumeViewModel.Factory(repository)
            )
            
            ResumeEditScreen(
                resumeId = resumeId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToPreview = { resumeId ->
                    navController.navigate(AppRoutes.resumePreview(resumeId)) {
                        // Replace the edit screen with preview
                        popUpTo(AppRoutes.RESUME_LIST)
                    }
                },
                onNavigateToTemplateSelection = {
                    navController.navigate(AppRoutes.TEMPLATE_SELECTION)
                },
                viewModel = viewModel
            )
        }
        
        // Resume Preview Screen
        composable(
            route = AppRoutes.RESUME_PREVIEW,
            arguments = listOf(
                navArgument("resumeId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val resumeId = backStackEntry.arguments?.getString("resumeId") ?: ""
            val viewModel: ResumeViewModel = viewModel(
                factory = ResumeViewModel.Factory(repository)
            )
            
            ResumePreviewScreen(
                resumeId = resumeId,
                template = selectedTemplate,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEdit = {
                    navController.navigate(AppRoutes.resumeEdit(resumeId))
                },
                viewModel = viewModel
            )
        }
        
        // Settings Screen
        composable(AppRoutes.SETTINGS) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // Template Selection Screen
        composable(AppRoutes.TEMPLATE_SELECTION) {
            TemplateSelectionScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onTemplateSelected = { template ->
                    // Navigate back to edit screen with selected template
                    navController.popBackStack()
                    // The template will be handled by the edit screen
                }
            )
        }
    }
}

/**
 * Extension functions for easier navigation
 */
fun NavHostController.navigateToResumeEdit(resumeId: String? = null) {
    navigate(AppRoutes.resumeEdit(resumeId))
}

fun NavHostController.navigateToResumePreview(resumeId: String) {
    navigate(AppRoutes.resumePreview(resumeId))
}

fun NavHostController.navigateToSettings() {
    navigate(AppRoutes.SETTINGS)
}
