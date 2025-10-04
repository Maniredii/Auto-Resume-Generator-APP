package fm.mrc.resumebuilder.utils

import android.content.Context
import android.content.Intent
import fm.mrc.resumebuilder.data.model.Resume
import fm.mrc.resumebuilder.ui.viewmodel.ResumeUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Helper class for PDF export functionality with enhanced error handling
 */
class PdfExportHelper {
    
    /**
     * Exports resume to PDF with proper error handling
     */
    suspend fun exportToPdf(
        context: Context,
        pdfExporter: PdfExporter,
        resume: Resume,
        uiState: ResumeUiState,
        templateId: String = "simple",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            try {
                val pdfUri = pdfExporter.exportResumeToPdf(context, resume, templateId)
                
                // Try to open the PDF directly in a PDF viewer
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(pdfUri, "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                
                try {
                    context.startActivity(intent)
                    onSuccess()
                } catch (e: Exception) {
                    // Fallback to share if no PDF viewer available
                    pdfExporter.shareResumePdf(
                        context, 
                        pdfUri, 
                        resume.personal.fullName.ifBlank { "Resume" }
                    )
                    onSuccess()
                }
            } catch (e: Exception) {
                onError(e.message ?: "Failed to export PDF")
            }
        }
    }
}
