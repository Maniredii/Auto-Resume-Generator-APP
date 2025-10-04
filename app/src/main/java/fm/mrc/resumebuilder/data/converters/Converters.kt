package fm.mrc.resumebuilder.data.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import fm.mrc.resumebuilder.data.model.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Room TypeConverters for complex data types
 */
class Converters {
    
    private val gson = Gson()
    
    // LocalDateTime converters
    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): String? {
        return dateTime?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }
    
    @TypeConverter
    fun toLocalDateTime(dateTimeString: String?): LocalDateTime? {
        return dateTimeString?.let {
            LocalDateTime.parse(it, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        }
    }
    
    // String List converters
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return value?.let {
            val listType = object : TypeToken<List<String>>() {}.type
            gson.fromJson(it, listType)
        }
    }
    
    // ResumeMetadata converters
    @TypeConverter
    fun fromResumeMetadata(metadata: ResumeMetadata?): String? {
        return gson.toJson(metadata)
    }
    
    @TypeConverter
    fun toResumeMetadata(value: String?): ResumeMetadata? {
        return value?.let {
            gson.fromJson(it, ResumeMetadata::class.java)
        }
    }
    
    // PersonalInfo converters
    @TypeConverter
    fun fromPersonalInfo(personalInfo: PersonalInfo?): String? {
        return gson.toJson(personalInfo)
    }
    
    @TypeConverter
    fun toPersonalInfo(value: String?): PersonalInfo? {
        return value?.let {
            gson.fromJson(it, PersonalInfo::class.java)
        }
    }
    
    // Education List converters
    @TypeConverter
    fun fromEducationList(educationList: List<Education>?): String? {
        return gson.toJson(educationList)
    }
    
    @TypeConverter
    fun toEducationList(value: String?): List<Education>? {
        return value?.let {
            val listType = object : TypeToken<List<Education>>() {}.type
            gson.fromJson(it, listType)
        }
    }
    
    // Experience List converters
    @TypeConverter
    fun fromExperienceList(experienceList: List<Experience>?): String? {
        return gson.toJson(experienceList)
    }
    
    @TypeConverter
    fun toExperienceList(value: String?): List<Experience>? {
        return value?.let {
            val listType = object : TypeToken<List<Experience>>() {}.type
            gson.fromJson(it, listType)
        }
    }
    
    // Project List converters
    @TypeConverter
    fun fromProjectList(projectList: List<Project>?): String? {
        return gson.toJson(projectList)
    }
    
    @TypeConverter
    fun toProjectList(value: String?): List<Project>? {
        return value?.let {
            val listType = object : TypeToken<List<Project>>() {}.type
            gson.fromJson(it, listType)
        }
    }
}
