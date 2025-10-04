package fm.mrc.resumebuilder.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import fm.mrc.resumebuilder.R
import fm.mrc.resumebuilder.data.model.*

/**
 * Utility class for rendering resume data into XML templates
 */
object XmlTemplateRenderer {

    /**
     * Render resume data into the specified XML template
     */
    fun renderResumeToView(
        context: Context,
        resume: Resume,
        templateType: String
    ): View {
        val layoutId = when (templateType.lowercase()) {
            "simple" -> R.layout.resume_template_simple
            "modern" -> R.layout.resume_template_modern
            "creative" -> R.layout.resume_template_creative
            else -> R.layout.resume_template_simple
        }

        val view = LayoutInflater.from(context).inflate(layoutId, null)
        populateResumeData(view, resume)
        return view
    }

    /**
     * Populate the template view with resume data
     */
    private fun populateResumeData(view: View, resume: Resume) {
        // Set personal information
        view.findViewById<TextView>(R.id.tv_name)?.text = resume.personal.fullName
        view.findViewById<TextView>(R.id.tv_title)?.text = resume.personal.title
        view.findViewById<TextView>(R.id.tv_email)?.text = resume.personal.email
        view.findViewById<TextView>(R.id.tv_phone)?.text = resume.personal.phone
        view.findViewById<TextView>(R.id.tv_location)?.text = resume.personal.location

        // Set summary
        view.findViewById<TextView>(R.id.tv_summary)?.text = resume.summary

        // Set skills
        view.findViewById<TextView>(R.id.tv_skills)?.text = resume.skills.joinToString(", ")

        // Populate experience
        val experienceContainer = view.findViewById<LinearLayout>(R.id.ll_experience_container)
        experienceContainer?.let { container ->
            container.removeAllViews()
            resume.experience.forEach { exp ->
                val expView = createExperienceView(view.context, exp)
                container.addView(expView)
            }
        }

        // Populate education
        val educationContainer = view.findViewById<LinearLayout>(R.id.ll_education_container)
        educationContainer?.let { container ->
            container.removeAllViews()
            resume.education.forEach { edu ->
                val eduView = createEducationView(view.context, edu)
                container.addView(eduView)
            }
        }

        // Populate projects
        val projectsContainer = view.findViewById<LinearLayout>(R.id.ll_projects_container)
        projectsContainer?.let { container ->
            container.removeAllViews()
            resume.projects.forEach { project ->
                val projectView = createProjectView(view.context, project)
                container.addView(projectView)
            }
        }
    }

    /**
     * Create experience item view
     */
    private fun createExperienceView(context: Context, experience: Experience): View {
        val view = LayoutInflater.from(context).inflate(R.layout.resume_experience_item, null)
        
        view.findViewById<TextView>(R.id.tv_job_title)?.text = experience.role
        view.findViewById<TextView>(R.id.tv_company)?.text = experience.company
        view.findViewById<TextView>(R.id.tv_duration)?.text = "${experience.start} - ${experience.end}"
        
        val bulletsContainer = view.findViewById<LinearLayout>(R.id.ll_bullets)
        bulletsContainer?.let { container ->
            container.removeAllViews()
            experience.bullets.forEach { bullet ->
                val bulletView = createBulletView(context, bullet)
                container.addView(bulletView)
            }
        }
        
        return view
    }

    /**
     * Create education item view
     */
    private fun createEducationView(context: Context, education: Education): View {
        val view = LayoutInflater.from(context).inflate(R.layout.resume_education_item, null)
        
        view.findViewById<TextView>(R.id.tv_degree)?.text = education.degree
        view.findViewById<TextView>(R.id.tv_institution)?.text = education.institution
        view.findViewById<TextView>(R.id.tv_duration)?.text = "${education.start} - ${education.end}"
        view.findViewById<TextView>(R.id.tv_details)?.text = education.details
        
        return view
    }

    /**
     * Create project item view
     */
    private fun createProjectView(context: Context, project: Project): View {
        val view = LayoutInflater.from(context).inflate(R.layout.resume_project_item, null)
        
        view.findViewById<TextView>(R.id.tv_project_title)?.text = project.title
        view.findViewById<TextView>(R.id.tv_project_link)?.text = project.link
        view.findViewById<TextView>(R.id.tv_project_description)?.text = project.description
        view.findViewById<TextView>(R.id.tv_project_tech)?.text = project.tech.joinToString(", ")
        
        return view
    }

    /**
     * Create bullet point view
     */
    private fun createBulletView(context: Context, bulletText: String): View {
        val view = LayoutInflater.from(context).inflate(R.layout.resume_bullet_item, null)
        view.findViewById<TextView>(R.id.tv_bullet_text)?.text = bulletText
        return view
    }
}
